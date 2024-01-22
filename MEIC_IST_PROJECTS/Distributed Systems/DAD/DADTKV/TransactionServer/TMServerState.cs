using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using Google.Protobuf.WellKnownTypes;
using Grpc.Core;
using Grpc.Net.Client;
using TransactionServer.Exceptions;
using TransNs;

namespace TransactionManager
{
  public class TMServerState
  {
    /// <summary>
    /// Identifier of this Transaction Manager
    /// </summary>
    private string id;

    /// <summary>
    /// Server that is running this Transaction Manager
    /// </summary>
    private Server server;

    /// <summary>
    /// The current time slot
    /// </summary>
    private int currentTimeslot = 0;

    /// <summary>
    /// Data structure to store the DadInts in the system
    /// </summary>
    private Dictionary<string, int> dadInts = new Dictionary<string, int>();

    /// <summary>
    /// Service to propagate the transactions executed to the other Transaction Managers
    /// </summary>
    private TransPropagationSendService propagationSender;

    /// <summary>
    /// Keeps the time slots in which this TM is TRULY crashed
    /// </summary>
    private List<int> timeslotsIAmCrashed = new List<int>();

    /// <summary>
    /// Keeps the TMs this server suspects are crashed, keeping their identifiers
    /// </summary>
    private Dictionary<int, List<string>> tMsSusCrashed = new Dictionary<int, List<string>>();

    /// <summary>
    /// Keeps the Transaction Managers that were excluded due to suspicion of being crashed
    /// </summary>
    private List<string> tMsExcluded = new List<string>();

    /// <summary>
    /// Keeps the Transaction Managers that are to be excluded due to suspicion of being crashed
    /// </summary>
    private List<string> tMsToBeExcluded = new List<string>();

    /// <summary>
    /// Keeps the TMs I suspected to be crashed last round. 
    /// If I still suspect them to be crashed, I issue a Kick to the TM cluster  
    /// </summary>
    private List<string> tMsIsuspectedLastTimeslot = new List<string>();

    private ExclusionRequester exclusionRequester;

    /// <summary>
    /// Holds the URL of all the other Transaction Managers
    /// </summary>
    private Dictionary<string, string> tMUrls = new Dictionary<string, string>();

    /// <summary>
    /// Service to ask the LMs for the leases
    /// </summary>
    private LeaseRequestSendService leaseRequester;

    /// <summary>
    /// Data structure that represents the state of leases all around the all the TMs
    /// </summary>
    private Dictionary<string, List<string>> leasesTable = new Dictionary<string, List<string>>();

    /// <summary>
    /// Keeps the URLs of all the Lease Managers
    /// </summary>
    private Dictionary<string, string> lMUrls = new Dictionary<string, string>();


    public TMServerState(string id, Dictionary<string, string> TMs, Dictionary<string, string> LMs, 
      List<int> timeslotsIAmCrashed, Dictionary<int, List<string>> tMsSusCrashed)
    {
      this.id = id;
      this.tMUrls = TMs;
      this.LMUrls = LMs;
      this.timeslotsIAmCrashed = timeslotsIAmCrashed;
      this.tMsSusCrashed = tMsSusCrashed;
      this.UpdateCurrentTimeslot(1);
      this.propagationSender = new TransPropagationSendService(this);
      this.leaseRequester = new LeaseRequestSendService(this);
      this.exclusionRequester = new ExclusionRequester(this);
    }

    public string Id
    {
      get { return id; }
    }

    public Server Server
    { 
      get { return server; } 
      set { server = value; }
    }

    public int CurrentTimeslot
    {
      get { return currentTimeslot; }
      set { currentTimeslot = value; }
    }

    public Dictionary<string, string> TMUrls
    {
      get { return tMUrls; }
      set {  tMUrls = value; }
    }

    public Dictionary<string, string> LMUrls
    {
      get { return lMUrls; }
      set {  lMUrls = value; }
    }

    public List<string> TMsExcluded
    {
      get { return tMsExcluded; }
    }

    public List<string> TMsToBeExcluded
    {
      get { return tMsToBeExcluded; }
    }

    public Dictionary<string, List<string>> LeasesTable
    {
      get { return leasesTable; }
    }

    /// <summary>
    /// Returns true if this TM is actually crashed
    /// </summary>
    public bool IsCrashed()
    {
      lock (this)
      {
        return timeslotsIAmCrashed.Contains(this.currentTimeslot);
      }
           
    }

    /// <summary>
    /// Returns true if this TM suspects the TM with the identifier @id to be crashed
    /// </summary>
    public bool ISuspectCrashed(string id)
    {
      lock(this)
      {
        if (tMsSusCrashed.ContainsKey(this.CurrentTimeslot))
        {
          return tMsSusCrashed[this.CurrentTimeslot].Contains(id);
        }
      }
      return false;
    }

    /// <summary>
    /// Executes the read operations of a transaction
    /// </summary>
    public List<KeyValuePair<string, int?>> ExecuteReadOperations(TransNs.Transaction tx)
    {
      List<KeyValuePair<string, int?>> result = new List<KeyValuePair<string, int?>>();
      lock (this)
      {
        foreach (string DadIntId in tx.EntriesToRead)
        {
          if (dadInts.ContainsKey(DadIntId))
          {
            result.Add(new KeyValuePair<string, int?>(DadIntId, dadInts[DadIntId]));
          } else
          {
            result.Add(new KeyValuePair<string, int?>(DadIntId, null));
          }
        }
      }
      return result;
    }

    /// <summary>
    /// Executes the write operations of a transaction.
    /// </summary>
    public void ExecuteWriteOperations(TransNs.Transaction tx)
    {
      lock (this)
      {
        foreach (KeyValuePair<string, int> dadInt in tx.EntriesToWrite)
        {
          dadInts[dadInt.Key] = dadInt.Value;
        }
      }
    }

    /// <summary>
    /// Propagates a transaction to all other TMs. 
    /// For effieciency, only propagates the write operations.
    /// </summary>
    public void StatePropagateTransaction(TransNs.Transaction tx)
    {
      if (tx.EntriesToWrite.Count != 0)
      {
        // only propagates the write operations
        propagationSender.PropagateTransaction(tx);
      }
    }

    /// <summary>
    /// Verifys if it is safe for this TM to execute a certain transaction, by making sure
    /// it has all the leases for all the DadInts to accesed during the transaction.
    /// </summary>
    public bool CheckLeasesForTransaction(TransNs.Transaction tx)
    {
      lock (this)
      {
        foreach (string lease in tx.getAllDadIntsIds())
        {
          if (leasesTable.ContainsKey(lease))
          {
            if (leasesTable[lease][0] != this.Id)
            {
              return false;
            }
          }
          else
          {
            leasesTable[lease] = new List<string> { this.Id };
            return IAmFirst();
          }
        }
      }
      return true;
    }

    public bool IAmFirst()
    {
        SortedSet<string> sortedTmIds = new SortedSet<string>();
        foreach (string id in tMUrls.Values)
            {
                sortedTmIds.Add(id);
            }
        return sortedTmIds.First() == this.Id;
    }

    /// <summary>
    /// Updates the lease table after receiving the reply of the Lease Managers decision.
    /// Tries to execute all the transactions that were on the queue.
    /// </summary>
    /// <param name="leases"> For each lease, has the order the TMs that can have it </param> 
    public void UpdateLeasesTable(Dictionary<string, List<string>> leases)
    {
      foreach (KeyValuePair<string, List<string>> lease in leases)
      {
        //List<string> tMIds = new List<string>();
        if (!leasesTable.ContainsKey(lease.Key))
        {
            this.leasesTable[lease.Key] = new List<string>();
        }
        foreach (string tMId in lease.Value)
        {
            this.leasesTable[lease.Key].Add(tMId);
        }
        //this.leasesTable[lease.Key] = tMIds;
      }
    }

    /// <summary>
    /// Removes the TM that held the lease for all the DadInts of the transaction from the leaseTable.
    /// This remotion is only executed if other TMs want the lease.
    /// </summary>
    /// <param name="tx"></param>
    public void UpdateLeaseTableAfterTransaction(TransNs.Transaction tx)
    {
      lock (this)
      {
        foreach (string DadIntId in tx.getAllDadIntsIds())
        {
          if (this.LeasesTable.ContainsKey(DadIntId) && this.LeasesTable[DadIntId].Count > 1)
            this.LeasesTable[DadIntId].RemoveAt(0);
        }
      }
    }

    /// <summary>
    /// Asks the LeaseRequester service to ask the LMs for the leases.
    /// </summary>
    /// <param name="tx"></param>
    public bool RequestLeases(TransNs.Transaction tx)
    {
      return leaseRequester.RequestLeaseToAllLMs(tx.getAllDadIntsIds());
    }

    /// <summary>
    /// Updates the current time slot to the epoch received with the leases assignment.
    /// Verifys if the epoch is lower or equal to the currentTimeslot, if so, it is ignored.
    /// If this TM is supposed to be crashed, then it crashes.
    /// We assume it is impossible for a TM to be crashed at the first time slot.
    /// 
    /// If this is the second timeslot I suspect a TM to be crashed, I issue a exclusion request for it.
    /// </summary>
    /// <param name="epoch"></param>
    public bool UpdateCurrentTimeslot(int epoch)
    {
      lock (this)
      {
        if (this.tMsSusCrashed.ContainsKey(this.CurrentTimeslot))
        {
          foreach (string tmId in this.tMsSusCrashed[this.CurrentTimeslot])
          {
            tMsIsuspectedLastTimeslot.Add(tmId);
          }
        }
        if (epoch > this.CurrentTimeslot)
        {
          this.CurrentTimeslot = epoch;
          if (this.tMsSusCrashed.ContainsKey(this.CurrentTimeslot))
          {
            foreach (string tmId in this.tMsSusCrashed[this.CurrentTimeslot])
            {
              if (tMsIsuspectedLastTimeslot.Contains(tmId))
              {
                try
                {
                  exclusionRequester.ExcludeTM(tmId);
                  ExcludeTM(tmId);
                } catch (TMQuorumNotAchieved ex)
                {
                  Console.WriteLine(ex.Message + $"Could Not Exclude the Suspected TM: {tmId}");
                }
              }
            }
          }
          if (this.IsCrashed())
          {
            Console.WriteLine("OH NO!! I am Crasheeedd!!!");
            // Kill myself
            server.ShutdownAsync().Wait();
            Process.GetCurrentProcess().CloseMainWindow();
          }
          return true;
        }
        return false;
      }
      
    }

    /// <summary>
    /// This function gives the number of replys from TMs that are required to have a majority quorum.
    /// It gets the number of TMs at the beginning and subtracts the number of TMs that are suspected to be crashed
    /// at the current time slot.
    /// </summary>
    /// <returns></returns>
    public decimal QuorumSize()
    {
      int numSusCrashed;
      lock (this)
      {
        numSusCrashed = this.tMsSusCrashed.ContainsKey(this.CurrentTimeslot) ?
          this.tMsSusCrashed[this.CurrentTimeslot].Count : 0;
      }
      decimal d = (this.tMUrls.Count - numSusCrashed) / 2;
      return Math.Floor(d) + 1;
    }

    public void AddATMToBeExcluded(string tMId)
    {
      lock (this)
      {
        this.tMsToBeExcluded.Add(tMId);
      }
    }

    /// <summary>
    /// Removes the TM from the cluster
    /// </summary>
    /// <param name="tMId"></param>
    public void ExcludeTM(string tMId)
    {
      lock(this)
      {
        this.tMsExcluded.Add(tMId);
        this.tMUrls.Remove(tMId);
      }
    }

    /// <summary>
    /// This function is called when a client requests the servers status to be shown. 
    /// </summary>
    public void ShowStatus()
    {
      Console.WriteLine("--------- Status ---------");
      lock (this)
      {
        Console.WriteLine($"-- Current Time Slot: {this.CurrentTimeslot}");

        Console.Write($"-- I suspect the following TMs to be crashed: ");
        if (this.tMsSusCrashed.ContainsKey(this.CurrentTimeslot))
        {
          foreach (string suspected in this.tMsSusCrashed[this.CurrentTimeslot])
          {
            Console.Write(suspected + ", ");
          }
        }
        Console.WriteLine();

        Console.WriteLine("-- My lease table is as such: ");
        foreach (KeyValuePair<string, List<string>> leaseEntry in this.leasesTable)
        {
          Console.Write($"Lease {leaseEntry.Key}: ");
          foreach (string tm in leaseEntry.Value)
          { 
            Console.Write(tm + ", ");
          }
        }
        Console.WriteLine();

        Console.WriteLine("-- My data is as follows: ");
        foreach (KeyValuePair<string, int> dadInt in dadInts)
        {
          Console.Write($"({dadInt.Key}, {dadInt.Value})");
        }
      }
      Console.WriteLine();
      Console.WriteLine("--------- ------ ---------");
    }


  }
}

