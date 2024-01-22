using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml;
using Grpc.Core;
using Grpc.Net.Client;

namespace LeaseManager
{

    /// <summary>
    /// This class is responsible for the Proposers Paxos Algorithm between LeaseManagers
    /// </summary>
    public class AcceptorReceiver : AcceptService.AcceptServiceBase
    {

        /// <summary>
        /// The state of the current LeaseManager
        /// </summary>
        LMServerState state;


        public AcceptorReceiver(LMServerState state) 
        {
            this.state = state;
        }

        public override Task<AcceptedReply> AcceptStep(AcceptRequest request, ServerCallContext context)
        {
            Console.WriteLine("VOU RECEBER ACCEPT");
            return Task.FromResult(acceptAux(request));
        }

        public AcceptedReply acceptAux(AcceptRequest request)
        {
            AcceptedReply reply = new AcceptedReply();
            if (this.state.iAmCrashed() || request.RequestId < this.state.HighestPrepare)
            {
                Console.WriteLine("NAO ACEITO ESTE ACCEPT VOU MAZE CAGAR");
            }
            else
            {
                this.state.ReplaceConsensusLeasesTable(this.state.ConvertGrpcToList(request.LeaseTable));
                this.state.AddLastConsensus();
                reply.RequestId = request.RequestId;
                foreach (LeaseTableEntry entry in request.LeaseTable)
                {
                    reply.LeaseTable.Add(entry);
                }
                Console.WriteLine("TABELA DE CONSENSUS");
                this.state.PrintsTable(this.state.LeasesTable);
            }
            return reply;
        }

    }

    /// <summary>
    /// This class is responsible for the Acceptors in the Paxos Algorithm 
    /// </summary>
    public class AcceptorSender : AcceptService.AcceptServiceClient
    {

        private LeaseRequestAssigner assigner;

        /// <summary>
        /// The state of the current LeaseManager
        /// </summary>
        private LMServerState state;

        private Dictionary<string, AcceptService.AcceptServiceClient> clients = new
            Dictionary<string, AcceptService.AcceptServiceClient>();

        public AcceptorSender(LMServerState state) 
        {
            this.state=state;
            this.assigner = new LeaseRequestAssigner(state);

            foreach (KeyValuePair<string, string> lM in state.LMUrls)
            {
                if (lM.Key != state.Id)
                {
                    AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
                    GrpcChannel channel = GrpcChannel.ForAddress(lM.Value);
                    this.clients[lM.Key] = (new AcceptService.AcceptServiceClient(channel));
                }
            }
        }

        public async Task sendAccept()
        {
            Console.WriteLine("VOU MANDAR ACCEPT");
            Dictionary<string, AcceptedReply> replys = new Dictionary<string, AcceptedReply>();
            //this.state.AddLeaseMissing("TOMAS", "TM1");
            foreach (KeyValuePair<string, AcceptService.AcceptServiceClient> client in this.clients)
            {
                if (!this.state.ISuspectCrashed(client.Key))
                {
                    try
                    {
                        AcceptRequest request = new AcceptRequest();
                        request.RequestId = this.state.HighestPrepare;
                        foreach (LeaseTableEntry entry in state.ConvertLeaseTableGrpc())
                        {
                            request.LeaseTable.Add(entry);
                        }
                        replys[client.Key] = await client.Value.AcceptStepAsync(request, new CallOptions(deadline: DateTime.UtcNow.AddSeconds(5)));
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine("Timeout exceeded: " + ex.Message);

                    }
                }
            }

            Console.WriteLine($"NUMERO DE RESPOSTAS DO ACCEPT RECEBIDAS = {replys.Count}");

            if (replys.Count + 1> this.state.QuorumSize())
            {
                Console.WriteLine("RECEBI MAIORIA VOU MANDAR ESTA TABELA PARA OS TMS");
                this.state.PrintsTable(this.state.LeasesTable);
                this.state.LastConsensus = this.state.LeasesTable;
                this.assigner.ReturnDecidedLeaseAssingmentOrder(this.state.LeasesTable);
            }
        }

    }
}