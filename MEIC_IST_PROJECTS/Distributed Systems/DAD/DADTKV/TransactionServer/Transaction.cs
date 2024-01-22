using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TransactionManager;

namespace TransNs
{
  public class Transaction
  {
    /// <summary>
    /// This is the identifier of the TM that received the transaction request from the client
    /// </summary>
    private string requestedTMId;

    /// <summary>
    /// Id of the client that sent the transaction request
    /// </summary>
    private string? clientId;

    /// <summary>
    /// URL of the client that sent the transaction request
    /// </summary>
    //private string clientURL = null;

    /// <summary>
    /// Set of DadInt identifiers to be read
    /// </summary>
    private List<string> entriesToRead = new List<string>();

    /// <summary>
    /// Set of DadInts to write
    /// </summary>
    private List<KeyValuePair<string, int>> entriesToWrite = new List<KeyValuePair<string, int>>();

    public Transaction(string TMId, List<string> read, List<KeyValuePair<string, int>> write)
    {
      this.clientId = null;
      this.requestedTMId = TMId;
      this.entriesToRead = read;
      this.entriesToWrite = write;
    }

    public Transaction(string clientId, string TMId, List<string> read, List<KeyValuePair<string, int>> write)
    {
      this.clientId = clientId;
      //this.clientURL = clientURL;
      this.requestedTMId = TMId;
      this.entriesToRead = read;
      this.entriesToWrite = write;
    }
    
    public string? ClientId
    {
      get { return clientId; }
      set { clientId = value; }
    }
    
    public string RequestedTMId
    {
      get { return requestedTMId; }
      set { requestedTMId = value; }
    }

    /*public string ClientURL
    { 
      get { return clientURL; } 
      set { clientURL = value; }
    }*/

    public List<string> EntriesToRead
    {
      get { return entriesToRead; }
      set { this.entriesToRead = value; }
    }

    public List<KeyValuePair<string, int>> EntriesToWrite
    {
      get { return entriesToWrite; }
      set { this.entriesToWrite = value; }
    }

    public List<string> getAllDadIntsIds()
    {
      List<string> writeIds = new List<string>();
      foreach (KeyValuePair<string, int> entry in this.EntriesToWrite)
      {
        writeIds.Add(entry.Key);
      }
      return Enumerable.Union(this.EntriesToRead, writeIds).ToList(); ;
    }

    /// <summary>
    /// Asks the TM that issues the execution to perform the read and write operations,
    /// as well as updating the leases table
    /// </summary>
    /// <param name="TMState"> The TM state that issues the execution of this transaction </param>
    /// <returns> The DadInts read by transaction and an ACK saying exec was good </returns>
    public List<KeyValuePair<string, int?>> Execute(TMServerState TMState)
    {
      List<KeyValuePair<string, int?>> result;
      result = TMState.ExecuteReadOperations(this);
      TMState.ExecuteWriteOperations(this);
      // this update is necessary, however not like this.
      // We must remove the TM that first received the transaction from the lease table
      TMState.UpdateLeaseTableAfterTransaction(this);
      return result;
    }

     /// <summary>
     /// Transforms a normal transaction to one (from the TM-TM.proto) to be sent through Grpc.
     /// Only sends the write operations
     /// </summary>
    public Transaction2 TransformToGrpc()
    {
      Transaction2 result = new Transaction2 { };
      /*
      foreach (string entryToRead in entriesToRead)
      {
        result.ReadOperations.Add(new ReadOperation2{ DadIntId = entryToRead });
      }
      */
    List<WriteOperation2> writeOps = new List<WriteOperation2>();
      foreach (KeyValuePair<string, int> entryToWrite in entriesToWrite)
      {
        result.WriteOperations.Add(new WriteOperation2 { DadInt = new DadIntType2 
            { Key = entryToWrite.Key, Value = entryToWrite.Value } });
      }
      return result;
    }

  }
}
