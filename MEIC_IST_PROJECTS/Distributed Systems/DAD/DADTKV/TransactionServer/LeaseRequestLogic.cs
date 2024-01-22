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
  public class LeaseAssignmentReceiver : LeaseRequestService.LeaseRequestServiceBase
  {
    private TMServerState state;

    public LeaseAssignmentReceiver(TMServerState state)
    {
      this.state = state;
    }

    public override Task<LeaseAssignmentReply> AssignLeases(LeaseAssignmentSend request, 
      ServerCallContext context)
    {
      return Task.FromResult(AssignLeasesAux(request));
    }

    public LeaseAssignmentReply AssignLeasesAux(LeaseAssignmentSend request)
    {
      Dictionary<string, List<string>> leases = new Dictionary<string, List<string>>();
      lock (this)
      {
        foreach (LeaseAssignment leaseAssignment in request.LeasesAssigned)
        {
          List<string> tMIds = new List<string>();
          foreach (string tMId in leaseAssignment.TMIds)
          {
            tMIds.Add(tMId);
          }
          leases[leaseAssignment.DadIntKey] = tMIds;
        }
        if (state.UpdateCurrentTimeslot(request.Epoch))
        {
          // only updates the Lease Table if the timeslot was actually increased
          state.UpdateLeasesTable(leases);
        }
      }
      return new LeaseAssignmentReply { Ack = true };
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

  }



  public class LeaseRequestSendService : LeaseRequestService.LeaseRequestServiceClient
  {
    private TMServerState state;

    // Holds the channels to all LMs, the dictionary keys are the LMs ids
    private Dictionary<string, LeaseRequestService.LeaseRequestServiceClient> clients = new
     Dictionary<string, LeaseRequestService.LeaseRequestServiceClient>();

    public LeaseRequestSendService(TMServerState state)
    {
      this.state = state;

      foreach (KeyValuePair<string, string> lM in state.LMUrls)
      {
        AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
        GrpcChannel channel = GrpcChannel.ForAddress(lM.Value);
        clients[lM.Key] = new LeaseRequestService.LeaseRequestServiceClient(channel);
      }
    }

    public bool RequestLeaseToAllLMs(List<string> leasesWanted)
    {
      int numReplys = 0; // Has to be at least one
      Dictionary<string, LeaseReply> replys = new Dictionary<string, LeaseReply>();
      foreach (KeyValuePair<string, LeaseRequestService.LeaseRequestServiceClient> lM in clients)
      {
        try
        {
          LeaseRequest request = new LeaseRequest { TMId = state.Id };
          foreach (string leaseWanted in leasesWanted)
          {
            request.DadIntKeys.Add(leaseWanted);
          }
          replys[lM.Key] = lM.Value.RequestLease(request, 
            new CallOptions(deadline: DateTime.UtcNow.AddSeconds(3)));

          numReplys++;
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.DeadlineExceeded)
        {
          if (ex.StatusCode == StatusCode.DeadlineExceeded)
          {
            Console.WriteLine("Timeout exceeded: " + ex.Message);
          } else
          {
            Console.WriteLine(ex.Message);
          }
        }
      }
      if (numReplys > 0)
      {
        return true;
      }
      throw new UnableToRequestLease();
    }



    
  }
}
