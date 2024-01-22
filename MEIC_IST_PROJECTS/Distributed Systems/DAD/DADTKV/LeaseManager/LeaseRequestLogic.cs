using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Grpc.Core;
using Grpc.Net.Client;

namespace LeaseManager
{

  /// <summary>
  /// This class is responsible for handling the reception of lease requests by the Transaction Managers
  /// </summary>
  public class LeaseRequestReceiver : LeaseRequestService.LeaseRequestServiceBase
  {
    LMServerState state;

    public LeaseRequestReceiver(LMServerState state)
    {
      this.state = state;
    }

    public override Task<LeaseReply> RequestLease(LeaseRequest request, ServerCallContext context) 
    {
      return Task.FromResult(RequestLeaseAux(request));
    }

    public LeaseReply RequestLeaseAux(LeaseRequest request)
    {
      if (this.state.iAmCrashed())
      {
        Thread.Sleep(4000);
        return new LeaseReply { Ack = false };
      }
      state.AddLeaseForPaxos(request.TMId, request.DadIntKeys);
      return new LeaseReply { Ack = true };
    }
  }

  /// <summary>
  /// This class is responsible to send the result of the Paxos execution to the TMs.
  /// Also sends the epoch in which this consensus was run/decided.
  /// </summary>
  public class LeaseRequestAssigner : LeaseRequestService.LeaseRequestServiceClient
  {
    /// <summary>
    /// The Lease Manager server state that has this service.
    /// </summary>
    LMServerState state;

    /// <summary>
    /// Holds the channels for all the TMs. The key is a TM id.
    /// </summary>
    private Dictionary<string, LeaseRequestService.LeaseRequestServiceClient> clients = new
      Dictionary<string, LeaseRequestService.LeaseRequestServiceClient>();

    /// <summary>
    /// Keeps the last message sent to a TM. The key is a TM id.
    /// </summary>
    private Dictionary<string, AsyncUnaryCall<LeaseAssignmentReply>> lastPropCall = new 
      Dictionary<string, AsyncUnaryCall<LeaseAssignmentReply>>();

    public LeaseRequestAssigner(LMServerState state)
    {
      this.state= state;

      foreach (KeyValuePair<string, string> tM in state.TMUrls)
      {
        AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
        GrpcChannel channel = GrpcChannel.ForAddress(tM.Value);
        clients[tM.Key] = (new LeaseRequestService.LeaseRequestServiceClient(channel));
      }
    }

    /// <summary>
    /// Returns the order of lease assignment decided during a Paxos instance.
    /// Also sends back the epoch in which ot was decided.
    /// </summary>
    /// <param name="leases"></param>
    /// <returns></returns>
    public async Task ReturnDecidedLeaseAssingmentOrder(Dictionary<string, List<string>> leases)
    {
      Dictionary<string, LeaseAssignmentReply> replys = new Dictionary<string, LeaseAssignmentReply>();
      foreach (KeyValuePair<string, LeaseRequestService.LeaseRequestServiceClient> client in clients)
      {
        if (lastPropCall[client.Key] != null)
        {
          replys[client.Key] = await lastPropCall[client.Key].ResponseAsync;
        }
        try
        {
          LeaseAssignmentSend request = new LeaseAssignmentSend { Epoch = state.CurrentTimeslot };
          foreach (KeyValuePair<string, List<string>> lease in leases)
          {
            LeaseAssignment la = new LeaseAssignment { DadIntKey = lease.Key };
            foreach (string tMId in lease.Value)
            {
              la.TMIds.Add(tMId);
            }
            request.LeasesAssigned.Add(la);
          }
          lastPropCall[client.Key] = client.Value.AssignLeasesAsync(request);
        } catch (Exception ex)
        {
          Console.WriteLine("Something went wrong!! " + ex.ToString());
        }
      }
    }

  }
}
