using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Grpc.Core;
using Grpc.Net.Client;
using System.Timers;
using Google.Protobuf.Collections;

namespace LeaseManager
{
    public class LMServerState
    {
        /// <summary>
        /// Identifier of this Lease Manager
        /// </summary>
        private string id;

        /// <summary>
        /// Amount of Lease Managers at the beginning of the execution
        /// </summary>
        //private int numberLMs;

        /// <summary>
        /// The current time slot
        /// </summary>
        private int currentTimeslot = 1;

        // Time variables
        private int timeslotDuration;
        private int numTimeslots;
        private DateTime wallTime;

        /// <summary>
        /// Holds the URL of all the other Transaction Managers
        /// </summary>
        private Dictionary<string, string> tMUrls = new Dictionary<string, string>();

        /// <summary>
        /// Holds the order that the Transaction Managers want a certain lease.
        /// The key is the lease (dadInt key) and the value is the TM order.
        /// This will be the value sent in the Paxos algorithym.
        /// </summary>
        private Dictionary<string, List<string>> leasesTable = new Dictionary<string, List<string>>();

        /// <summary>
        /// Keeps the URLs of all the Lease Managers
        /// </summary>
        private Dictionary<string, string> lMUrls = new Dictionary<string, string>();

        /// <summary>
        /// Keeps the time slots in which this LM is TRULY crashed.
        /// </summary>
        private List<int> timeSlotsIAmCrashed = new List<int>();

        /// <summary>
        /// Keeps the Lease Managers this server suspects are crashed at a given time slot.
        /// Keeping their identifiers.
        /// </summary>
        private Dictionary<int, List<string>> lMsSusCrashed = new Dictionary<int, List<string>>();

        // Paxos
        private int highestAccept;
        private int highestPrepare;
        private int currentLeader = 0;

        /// <summary>
        /// Keeps the Last Lease Table Consented during paxos
        /// </summary>
        private Dictionary<string, List<string>>? lastConsensus = new Dictionary<string, List<string>>();

        /// <summary>
        /// Holds the leases to share with the other Lease Managaer for when I'm the leader 
        /// </summary>
        private Dictionary<string, List<string>> leaderLeases = new Dictionary<string, List<string>>();

        private System.Timers.Timer timer;

        private PaxosPrepareSender prepareSender;

        public LMServerState(string id, Dictionary<string, string> TM, Dictionary<string, string> LM, List<int> timeSlotsIAmCrashed,
          Dictionary<int, List<string>> lMsSusCrashed, int timeSlotDuration, int numTimeSlots, string wallTime)
        {
            this.id = id;
            this.tMUrls = TM;
            this.lMUrls = LM;
            this.timeSlotsIAmCrashed = timeSlotsIAmCrashed;
            this.lMsSusCrashed = lMsSusCrashed;
            this.timeslotDuration = timeSlotDuration;
            this.numTimeslots = numTimeSlots;
            this.lastConsensus = null;
            string[] aux = wallTime.Split(":");
            this.prepareSender = new PaxosPrepareSender(this);
            this.wallTime = new DateTime(DateTime.Now.Year, DateTime.Now.Month, DateTime.Now.Day,
            Int32.Parse(aux[0]), Int32.Parse(aux[1]), Int32.Parse(aux[2]));
            this.timer = new System.Timers.Timer();
            this.timer.Elapsed += new ElapsedEventHandler(PaxosEvent);
            this.timer.Interval = timeslotDuration;
            this.timer.Enabled = true;
            this.highestAccept = 1;
            this.highestPrepare = 1;
        }

        public string Id
        {
            get { return id; }
        }

        public int CurrentTimeslot
        {
            get { return currentTimeslot; }
            set { currentTimeslot = value; }
        }

        public Dictionary<string, string> TMUrls
        {
            get { return tMUrls; }
        }

        public Dictionary<string, string> LMUrls
        {
            get { return lMUrls; }
        }

        public Dictionary<string, List<string>> LeasesTable
        {
            get { return leasesTable; }
            set { leasesTable = value; }
        }

        public Dictionary<string, List<string>>? LastConsensus
        {
            get { return lastConsensus; }
            set { lastConsensus = value; }
        }

        public int HighestAccept
        {
            get { return highestAccept; }
            set { highestAccept = value; }
        }

        public int HighestPrepare
        {
            get { return highestPrepare; }
            set { highestPrepare = value; }
        }

        /// <summary>
        /// Returns true if this LM is the first leader of a Paxos execution.
        /// All the Lease Managers have this same lMUrls therefore this operation is deterministic
        /// </summary>
        /// <returns></returns>
        public bool iAmLeader()
        {
            SortedSet<string> sortedLMIds = new SortedSet<string>();
            foreach (string id in lMUrls.Keys)
            {
                sortedLMIds.Add(id);
            }
            return sortedLMIds.ElementAt(currentLeader) == this.Id;
        }

        public bool susLeader()
        {
            SortedSet<string> sortedLMIds = new SortedSet<string>();
            foreach (string id in lMUrls.Keys)
            {
                sortedLMIds.Add(id);
            }
            string leader = sortedLMIds.ElementAt(currentLeader);
            Console.WriteLine("O LIDER É O " + leader);
            if (lMsSusCrashed.ContainsKey(currentTimeslot))
            {
                return lMsSusCrashed[currentTimeslot].Contains(leader);
            }

            return false;
        }

        /// <summary>
        /// Returns true if this Lease Manager is truly crashed in the time slot.
        /// </summary>
        /// <returns></returns>
        public bool iAmCrashed()
        {
            return timeSlotsIAmCrashed.Contains(this.CurrentTimeslot);
        }

        /// <summary>
        /// Returns true if this Lease Manager suspects that Lease Manager with certain Id is crashed.
        /// </summary>
        /// <param name="id"></param>
        /// <returns></returns>
        public bool ISuspectCrashed(string id)
        {
            if (lMsSusCrashed.ContainsKey(currentTimeslot))
            {
                return lMsSusCrashed[this.CurrentTimeslot].Contains(id);
            }
            return false;
        }

        /// <summary>
        /// Adds the Transaction Manager whose request was received from to the leasesTable
        /// </summary>
        /// <param name="tMId"></param>
        /// <param name="dadIntKeys"></param>
        public void AddLeaseForPaxos(string tMId, Google.Protobuf.Collections.RepeatedField<string> dadIntKeys)
        {
            lock (this)
            {
                foreach (string lease in dadIntKeys)
                {
                    if (leasesTable.ContainsKey(lease))
                    {
                        leasesTable[lease].Add(tMId);
                    }
                    else
                    {
                        leasesTable[lease] = new List<string>();
                        leasesTable[lease].Add(tMId);
                    }
                }
            }
        }

        private async void PaxosEvent(object source, ElapsedEventArgs e)
        {
            Console.WriteLine("A COMECAR O PAXOS VAMOSSSS");

            if (susLeader())
            {
                if (currentTimeslot == 1)
                {
                    Console.WriteLine("LIDER REPLACED A MANDAR ACCEPT");
                    this.prepareSender.AcceptorSenderVar.sendAccept();
                }
                else
                {
                    highestPrepare += 1;
                    Console.WriteLine("LIDER REPLACED A MANDAR PREPARE");
                    this.prepareSender.SendPrepareRequest(highestPrepare + 1);
                }
            }

            if (iAmLeader())
            {
                if (currentTimeslot == 1)
                {
                    Console.WriteLine("LM STATE A MANDAR ACCEPT");
                    this.prepareSender.AcceptorSenderVar.sendAccept();
                    //this.prepareSender.SendPrepareRequest(currentTimeslot);
                }
                else
                {
                    highestPrepare += 1;
                    Console.WriteLine("LM STATE A MANDAR PREPARE");
                    this.prepareSender.SendPrepareRequest(highestPrepare);
                }
            }
            currentTimeslot += 1;
            currentLeader += 1;
            if (currentLeader >= LMUrls.Count)
            {
                currentLeader = 0;
            }
        }

        /// <summary>
        /// Converts the LeaseTable to be sent trough Grpc
        /// </summary>
        /// <returns></returns>
        public List<LeaseTableEntry> ConvertLeaseTableGrpc()
        {
            lock (this)
            {
                List<LeaseTableEntry> result = new List<LeaseTableEntry>();

                foreach (KeyValuePair<string, List<string>> pair in leasesTable)
                {
                    LeaseTableEntry entry = new LeaseTableEntry();
                    entry.DadIntKey = pair.Key;
                    foreach (string e in pair.Value)
                    {
                        entry.TMIds.Add(e);
                    }
                    result.Add(entry);
                }
                return result;
            }
        }

        public List<LeaseTableEntry> ConvertGrpcToList(RepeatedField<LeaseTableEntry> tableGrpc)
        {
            List<LeaseTableEntry> list = new List<LeaseTableEntry>();

            foreach (LeaseTableEntry entry in tableGrpc)
            {
                list.Add(entry);
            }

            return list;
        }

        public void PrintsTable(Dictionary<string, List<string>> table)
        {
            foreach (KeyValuePair<string, List<string>> pair in table)
            {
                Console.WriteLine($"DADINT = {pair.Key}");
                Console.WriteLine("ORDEM TMs");
                foreach (string e in pair.Value)
                {
                    Console.WriteLine(e);
                }
            }
        }
        /// <summary>
        /// Replace the Current Lease Table with the Table consented during Paxos
        /// </summary>
        /// <param name="request_table"></param>
        public void ReplaceConsensusLeasesTable(List<LeaseTableEntry> request_table)
        {
            lock (this)
            {
                Dictionary<string, List<string>> new_table = new Dictionary<string, List<string>>();
                foreach (LeaseTableEntry entry in request_table)
                {
                    foreach (string e in entry.TMIds)
                    {
                        if (new_table.ContainsKey(entry.DadIntKey))
                        {
                            new_table[entry.DadIntKey].Add(e);
                        }
                        else
                        {
                            new_table[entry.DadIntKey] = new List<string>();
                            new_table[entry.DadIntKey].Add(e);
                        }
                    }
                }
                //this.leasesTable = new_table;

                this.lastConsensus = new_table;
            }
        }


        /// <summary>
        /// Checks If the current state has more leases than the version it should accept now
        /// If this LM has more leases than the leader, save them for when he becomes leader
        /// </summary>
        /// <param name="dadInt"></param>
        /// <param name="tMid"></param>
        /// /// <returns></returns>
        public bool CheckIfHasLease(string dadInt, string tMid)
        {
            if (leasesTable.ContainsKey(dadInt))
            {
                return leasesTable[dadInt].Contains(tMid);

            }

            return false;
        }


        /// <summary>
        /// Checks If the current state has more leases than the version it should accept now
        /// If this LM has more leases than the leader, save them for when he becomes leader
        /// </summary>
        /// <param name="request_table"></param>
        public void CheckDiferenceLeases(List<LeaseTableEntry> request_table)
        {
            foreach (LeaseTableEntry entry in request_table)
            {
                foreach (string id in entry.TMIds)
                {
                    if (!CheckIfHasLease(entry.DadIntKey, id))
                    {
                        AddLeaseMissing(entry.DadIntKey, id);
                    }
                }
            }
        }

        /// <summary>
        /// Converts the LeaseTable to be sent trough Grpc
        /// </summary>
        /// <param name="dadint"></param>
        /// <param name="tM"></param>
        public void AddLeaseMissing(string dadint, string tM)
        {
            lock (this)
            {
                if (leasesTable.ContainsKey(dadint))
                {
                    leasesTable[dadint].Add(tM);
                }
                else
                {
                    leasesTable[dadint] = new List<string>();
                    leasesTable[dadint].Add(tM);
                }
            }
        }

        public void AddLastConsensus()
        {
            lock (this)
            {
                if (lastConsensus != null)
                {
                    foreach (KeyValuePair<string, List<string>> entry in lastConsensus)
                    {
                        foreach(string id in entry.Value)
                        {
                            if (!CheckIfHasLease(entry.Key, id))
                            {
                                AddLeaseMissing(entry.Key, id);
                            }
                        }
                    }
                }
            }
        }

        public void RemoveLastConsensus()
        {
            lock (this)
            {
                if (lastConsensus != null)
                {
                    foreach (KeyValuePair<string, List<string>> pair in lastConsensus)
                    {
                        if (leasesTable.ContainsKey(pair.Key))
                        {
                            foreach (string id in pair.Value)
                            {
                                Console.WriteLine($"VER SE A TABLE CONTEM O PAIR DADINT = {pair.Key} TM = {id}");
                                if (leasesTable[pair.Key].Contains(id))
                                {
                                    leasesTable[pair.Key].Remove(id);
                                }
                            }
                            if (leasesTable[pair.Key].Count == 0)
                            {
                                leasesTable.Remove(pair.Key);
                            }
                        }
                    }
                }

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
            decimal d;
            if (this.lMsSusCrashed.ContainsKey(this.currentTimeslot))
            {
                d = (this.lMUrls.Count - this.lMsSusCrashed[this.CurrentTimeslot].Count) / 2;
            }
            else
            {
                d = this.lMUrls.Count / 2;
            }
            return d;
        }

        /// <summary>
        /// This function is called when a client requests the servers status to be shown. 
        /// </summary>
        public void ShowStatus()
        {
            Console.WriteLine("--------- Status ---------");
            lock (this)
            {
                Console.WriteLine($"Current Time Slot: {this.CurrentTimeslot}");

                Console.Write($"I suspect the following TMs to be crashed: ");
                if (this.lMsSusCrashed.ContainsKey(this.CurrentTimeslot))
                {
                    foreach (string suspected in this.lMsSusCrashed[this.CurrentTimeslot])
                    {
                        Console.Write(suspected + ", ");
                    }
                }
                Console.WriteLine();

                Console.WriteLine("My lease table is as such: ");
                foreach (KeyValuePair<string, List<string>> leaseEntry in this.leasesTable)
                {
                    Console.Write($"For the lease {leaseEntry.Key} the TMs order is: ");
                    foreach (string tm in leaseEntry.Value)
                    {
                        Console.Write(tm + ", ");
                    }
                }
                Console.WriteLine();
            }
            Console.WriteLine("--------- ------ ---------");
        }
    }
}
