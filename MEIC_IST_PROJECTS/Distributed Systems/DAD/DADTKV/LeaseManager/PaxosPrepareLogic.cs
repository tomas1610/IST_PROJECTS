using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Grpc.Core;
using Grpc.Net.Client;
using Google.Protobuf.Collections;

namespace LeaseManager
{

  /// <summary>
  /// This class is responsible for handling the reception of lease requests by the Transaction Managers
  /// </summary>
  public class PaxosPrepareReceiver : PrepareService.PrepareServiceBase
    {
    private LMServerState state;

    public PaxosPrepareReceiver(LMServerState state)
    {
      this.state = state;
    }

    public override Task<PromiseReply> PrepareStep(PrepareRequest request, ServerCallContext context) 
    {
      Console.WriteLine("VOU RECEBER PREPARE");
      return Task.FromResult(RequestPrepareAux(request));
    }

    public PromiseReply RequestPrepareAux(PrepareRequest request)
    {
      PromiseReply reply = new PromiseReply();

      if (this.state.iAmCrashed() || request.RequestId < this.state.HighestPrepare) 
      {

      }
      else 
      {
        
        this.state.RemoveLastConsensus();

        this.state.HighestPrepare = request.RequestId;
        reply.WriteTimestamp = this.state.HighestPrepare;
        foreach (LeaseTableEntry entry in this.state.ConvertLeaseTableGrpc())
        {
            reply.LeaseTable.Add(entry);
        }
      }
      return reply;
    }
  }

  /// <summary>
  /// This class is responsible to send the result of the Paxos execution to the TMs.
  /// Also sends the epoch in which this consensus was run/decided.
  /// </summary>
  public class PaxosPrepareSender : PrepareService.PrepareServiceClient
    {
        private LMServerState state;

        private AcceptorSender acceptorSenderVar;

        /// <summary>
        /// Holds the channels for all the TMs. The key is a LM id.
        /// </summary>
        private Dictionary<string, PrepareService.PrepareServiceClient> clients = new
          Dictionary<string, PrepareService.PrepareServiceClient>();


        public PaxosPrepareSender(LMServerState state)
        {
            this.state = state;
            this.acceptorSenderVar = new AcceptorSender(state);

            foreach (KeyValuePair<string, string> lM in state.LMUrls)
            {
                if (lM.Key != this.state.Id)
                {
                    AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
                    GrpcChannel channel = GrpcChannel.ForAddress(lM.Value);
                    clients[lM.Key] = (new PrepareService.PrepareServiceClient(channel));
                }
            }
        }

        public AcceptorSender AcceptorSenderVar
        {
            get { return acceptorSenderVar; }
        }

        public async Task SendPrepareRequest(int prepareId)
        {
            Console.WriteLine("VOU MANDAR PREPARE");
            Dictionary<string, PromiseReply> replys = new Dictionary<string, PromiseReply>();

            this.state.RemoveLastConsensus();

            foreach (KeyValuePair<string, PrepareService.PrepareServiceClient> client in this.clients)
            {
                if (!this.state.ISuspectCrashed(client.Key))
                {
                    try
                    {
                        replys[client.Key] = await client.Value.PrepareStepAsync(new PrepareRequest
                        {
                            RequestId = prepareId
                        }, new CallOptions(deadline: DateTime.UtcNow.AddSeconds(5)));
                    }
                    catch (Exception e)
                    {
                        Console.WriteLine("Timeout exceeded: " + e.Message);
                    }
                }
            }

            Console.WriteLine($"NUMERO DE RESPOSTAS DO PREPARE RECEBIDAS = {replys.Count}");

            foreach (KeyValuePair<string, PromiseReply> pair in replys)
            {
                this.state.CheckDiferenceLeases(this.state.ConvertGrpcToList(pair.Value.LeaseTable));
            }

            if (replys.Count + 1 > this.state.QuorumSize())
            {
                Console.WriteLine("RECEBI MAIORIA DOS PREPARES");
                this.acceptorSenderVar.sendAccept();
            }

        }

    }
}
