using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Grpc.Core;
using Grpc.Net.Client;
using TransactionServer.Exceptions;

namespace TransactionManager
{
  /// <summary>
  /// Server side of the propagation service, receives the propagation
  /// and applies all the write operations received to this server.
  /// </summary>
  public class TransPropagationReceiveService : TMTransPropagationService.TMTransPropagationServiceBase
  {
    // Server state that has this service
    TMServerState state;

    public TransPropagationReceiveService(TMServerState state)
    {
      this.state = state;
    }

    public override Task<TransactionPropagationReply> TransactionPropagation(
        TransactionPropagationSend request, ServerCallContext context)
    {
      return Task.FromResult(TransPropAux(request));
    }

    public TransactionPropagationReply TransPropAux(TransactionPropagationSend request)
    {
      lock (state)
      {
        if (state.TMsExcluded.Contains(request.Id) || state.TMsToBeExcluded.Contains(request.Id))
        {
          return new TransactionPropagationReply { Ack = false };
        }

        // create a list with the DadInt ids requested by the client to be read
        List<string> readTxRequested = new List<string>();
        foreach (var rd in request.Transaction.ReadOperations)
        {
          readTxRequested.Add(rd.DadIntId);
        }

        // create a list with the DadInts requested by the client to be written
        List<KeyValuePair<string, int>> writeTxRequested = new List<KeyValuePair<string, int>>();
        foreach (var wr in request.Transaction.WriteOperations)
        {
          writeTxRequested.Add(new KeyValuePair<string, int>(wr.DadInt.Key, wr.DadInt.Value));
        }

        TransNs.Transaction tx = new TransNs.Transaction(request.Id, readTxRequested, writeTxRequested);
        tx.Execute(state);

        return new TransactionPropagationReply { Ack = true };
      } 
    }
  }

  /// <summary>
  /// Client side of the propagation service, has the role of sending all the write operations
  /// of a transaction to all other TMs that are not suspected to be crashed by this TM.
  /// </summary>
  public class TransPropagationSendService : TMTransPropagationService.TMTransPropagationServiceClient
  {
    /// <summary>
    /// Server state that has this service
    /// </summary>
    TMServerState state;

    /// <summary>
    /// Holds the channels for every other TM, the dictionary keys are the other TMs ids
    /// </summary>
    private Dictionary<string, TMTransPropagationService.TMTransPropagationServiceClient> clients = new
      Dictionary<string, TMTransPropagationService.TMTransPropagationServiceClient>();

    public TransPropagationSendService(TMServerState state)
    {
      this.state = state;

      foreach (KeyValuePair<string, string> tM in state.TMUrls)
      {
        if (tM.Key != state.Id)
        {
          AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
          GrpcChannel channel = GrpcChannel.ForAddress(tM.Value);
          clients[tM.Key] = new TMTransPropagationService.TMTransPropagationServiceClient(channel);
        }
      }
    }

    public void PropagateTransaction(TransNs.Transaction tx)
    {
      int numReplys = 0;
      Dictionary<string, TransactionPropagationReply> replys = new Dictionary<string, TransactionPropagationReply>();
      foreach (KeyValuePair<string, TMTransPropagationService.TMTransPropagationServiceClient> client in clients)
      {
        if (!state.ISuspectCrashed(client.Key))
        {
          try
          {
            replys[client.Key] = client.Value.TransactionPropagation(new TransactionPropagationSend
            {
              Transaction = tx.TransformToGrpc(),
              Id = state.Id
            });
          } catch (RpcException ex)
          {
            Console.WriteLine(ex.ToString());
          }

          if (replys[client.Key].Ack == true)
          {
            numReplys++;
          }
        } 
      }
      if (numReplys < state.QuorumSize()-1)
      {
        throw new TMQuorumNotAchieved();
      }
    }

  }
}
