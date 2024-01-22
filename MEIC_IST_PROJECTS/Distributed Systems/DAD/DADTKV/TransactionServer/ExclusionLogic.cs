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
  public class ExclusionLogicService : ExclusionService.ExclusionServiceBase
  {
    private TMServerState state;

    public ExclusionLogicService(TMServerState state) 
    {
      this.state = state;
    }

    public override Task<KickoutReply> KickoutFirstRound(KickoutRequest request, ServerCallContext context)
    {
      return Task.FromResult(KickoutFirstRoundAux(request));
    }

    public override Task<KickoutConfirmationReply> KickoutSecondRound(KickoutConfirmationSend request, ServerCallContext context)
    {
      return Task.FromResult(KickoutSecondRoundAux(request));
    }

    public KickoutReply KickoutFirstRoundAux(KickoutRequest request)
    {
      state.AddATMToBeExcluded(request.SuspectedId);
      return new KickoutReply { Ack = true };
    }

    public KickoutConfirmationReply KickoutSecondRoundAux(KickoutConfirmationSend request)
    {
      state.ExcludeTM(request.SuspectedId);
      return new KickoutConfirmationReply { };
    }
  }

  public class ExclusionRequester : ExclusionService.ExclusionServiceClient
  {
    private TMServerState state;

    /// <summary>
    /// Holds the channels for every other TM, the dictionary keys are the other TMs ids
    /// </summary>
    private Dictionary<string, ExclusionService.ExclusionServiceClient> clients = new
      Dictionary<string, ExclusionService.ExclusionServiceClient>();

    public ExclusionRequester(TMServerState state)
    {
      this.state = state;

      AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
      foreach (KeyValuePair<string, string> tM in state.TMUrls)
      {
        if (tM.Key != state.Id)
        {
          GrpcChannel channel = GrpcChannel.ForAddress(tM.Value);
          clients[tM.Key] = new ExclusionService.ExclusionServiceClient(channel);
        }
      }
    }

    public void ExcludeTM(string tMId)
    {
      try
      {
        RequestExclusionFirstRound(tMId);
        ConfirmExclusion(tMId, true);
      } catch (TMQuorumNotAchieved ex)
      {
        // Send a message to not exclude tMId
        ConfirmExclusion(tMId, false);
        throw;
      }
    }

    public void RequestExclusionFirstRound(string tMId)
    {
      int numReplys = 0;
      Dictionary<string, KickoutReply> replys = new Dictionary<string, KickoutReply>();
      foreach (KeyValuePair<string, ExclusionService.ExclusionServiceClient> client in clients)
      {
        if (!state.ISuspectCrashed(tMId))
        {
          try
          {
            replys[client.Key] = client.Value.KickoutFirstRound(new KickoutRequest { MyId = state.Id, SuspectedId = tMId });
            numReplys++;
          }
          catch (RpcException ex)
          {
            Console.WriteLine(ex.ToString());
          }
        }
      }
      if (numReplys < state.QuorumSize())
      {
        throw new TMQuorumNotAchieved();
      }
    }

    public void ConfirmExclusion(string tMId, bool ack)
    {
      Dictionary<string, KickoutConfirmationReply> replys = new Dictionary<string, KickoutConfirmationReply>();
      foreach (KeyValuePair<string, ExclusionService.ExclusionServiceClient> client in clients)
      {
        if (!state.ISuspectCrashed(tMId))
        {
          try
          {
            replys[client.Key] = client.Value.KickoutSecondRound(new KickoutConfirmationSend { SuspectedId = tMId, Ack = ack });
          }
          catch (RpcException ex)
          {
            Console.WriteLine(ex.ToString());
          }
        }
      }
    }

  }

}
