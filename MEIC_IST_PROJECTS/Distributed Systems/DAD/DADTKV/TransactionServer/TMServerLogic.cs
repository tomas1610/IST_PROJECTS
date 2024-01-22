using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Channels;
using System.Threading.Tasks;
using Grpc.Core;
using Grpc.Net.Client;
using TransactionServer.Exceptions;
using TransNs;

namespace TransactionManager
{

  public class TMServerService : ClientToTMService.ClientToTMServiceBase
  {
    // The Server State that has this service associated
    private TMServerState state;

    public TMServerService(TMServerState state)
    {
      this.state = state;
    }

    public override Task<TransactionsReplyResults> TransactionSubmission(SubmitTransactionsRequest request, 
        ServerCallContext context)
    {
            return Task.FromResult(TransactionSubAux(request));
    }

    public TransactionsReplyResults TransactionSubAux(SubmitTransactionsRequest request)
    {
      List<KeyValuePair<string, int?>> values = new List<KeyValuePair<string, int?>>();
      lock (this)
      {
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

        TransNs.Transaction tx = new TransNs.Transaction(request.ClientId, this.state.Id, readTxRequested, writeTxRequested);

        TransactionsReplyResults replyAbort = new TransactionsReplyResults { Ack = false };
        replyAbort.Values.Add(new DadIntType { Key = "abort" });

        bool requested = false;
        while (!state.CheckLeasesForTransaction(tx))
        {
          Thread.Sleep(100);
          try
          {
            if (!requested)
            {
              state.RequestLeases(tx);
              requested = true;
            }
          }
          catch (UnableToRequestLease ex)
          {
            Console.WriteLine(ex.Message);
            return replyAbort;
          }
        }
        try
        {
          //propagate transaction to the other TMs suspected to be alive
          state.StatePropagateTransaction(tx);
        } catch (TMQuorumNotAchieved ex)
        {
          Console.WriteLine(ex.Message + " Transaction propagation failed!");
          return replyAbort;
        }
        values = tx.Execute(state);
      }

      TransactionsReplyResults reply = new TransactionsReplyResults { Ack = true };
      foreach (KeyValuePair<string, int?> value in values)
      {
        if (value.Value == null)
        {
          reply.Values.Add(new DadIntType { Key = value.Key });
        } else
        {
          reply.Values.Add(new DadIntType { Key = value.Key, Value = (int) value.Value });
        }
      }
      return reply;
    }

  }
}
