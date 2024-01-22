using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Grpc.Core;

namespace TransactionManager
{
  public class StatusPresentLogicService : StatusRequestService.StatusRequestServiceBase
  {
    private TMServerState state;

    public StatusPresentLogicService(TMServerState state)
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
