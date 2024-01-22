using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Grpc.Core;

namespace LeaseManager
{
  public class StatusPresentLogicService : StatusRequestService.StatusRequestServiceBase
  {
    private LMServerState state;

    public StatusPresentLogicService(LMServerState state)
    {
      this.state = state;
    }

    public override Task<StatusReply> RequestStatus(StatusRequest request, ServerCallContext context)
    {
      return Task.FromResult(RequestStatusAux(request));
    }

    public StatusReply RequestStatusAux(StatusRequest request)
    {
      state.ShowStatus();
      return new StatusReply { Ack = true };
    }
  }
}
