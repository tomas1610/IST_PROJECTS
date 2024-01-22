using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using Grpc.Core;
using Grpc.Net.Client;

namespace Client
{
  public class TransactionSubmitSend : ClientToTMService.ClientToTMServiceClient
  {
    private string id;

    // The clients are the Transaction Managers
    private Dictionary<string, ClientToTMService.ClientToTMServiceClient> clients = new 
      Dictionary<string, ClientToTMService.ClientToTMServiceClient>();

    public TransactionSubmitSend(string id, Dictionary<string, string> TMUrls)
    {
      this.id = id;

      foreach (KeyValuePair<string, string> entry in TMUrls)
      {
        AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
        try
        {
          GrpcChannel channel = GrpcChannel.ForAddress(entry.Value);
          clients[entry.Key] = new ClientToTMService.ClientToTMServiceClient(channel);
        } catch (Exception e) when (e is SocketException || e is HttpRequestException)
        {
          Console.WriteLine(e.ToString());
        }


      }
    }

    /// <summary>
    /// Submits a transaction to a random TM.
    /// </summary>
    /// <param name="tx"></param>
    /// <returns></returns>
    public void SubmitTransaction(Transaction tx, int transactionNumber)
    {
      Random random1 = new Random();
      int random = random1.Next(clients.Count);
      ClientToTMService.ClientToTMServiceClient client = clients.ElementAt(random).Value;

      try
      {
        SubmitTransactionsRequest request = new SubmitTransactionsRequest();
        request.ClientId = this.id;
        request.Transaction = tx;

        TransactionsReplyResults reply = client.TransactionSubmission(request, 
          new CallOptions(deadline: DateTime.UtcNow.AddSeconds(2)));

        foreach (DadIntType dadInt in reply.Values)
        {
          if (dadInt.Key == "abort")
          {
            Console.WriteLine("The transaction was aborted");
          }
        }

        PrintTransactionResults(reply, transactionNumber);
      } catch (RpcException ex)
      {
        if (ex.StatusCode == StatusCode.DeadlineExceeded)
        {
          Console.WriteLine($"The transaction submission to TM: {clients.ElementAt(random).Key} timed out, retrying...");
          SubmitTransaction(tx, transactionNumber);
        } else if (ex.StatusCode == StatusCode.Unavailable)
        {
          Console.WriteLine($"The transaction submission to TM: {clients.ElementAt(random).Key} failed, exitting...");
        } else
        {
          Console.WriteLine(ex.Message);
        }
      }
    }

    private void PrintTransactionResults(TransactionsReplyResults replyResults, int transactionNumber)
    {
      Console.WriteLine($"Results for the read entrys of the {transactionNumber} transaction:");
      foreach (DadIntType dadInt in replyResults.Values)
      {
        Console.Write(dadInt.Key + ": " + dadInt.Value + ";; ");
      }
      Console.WriteLine();
    }

  }
}
