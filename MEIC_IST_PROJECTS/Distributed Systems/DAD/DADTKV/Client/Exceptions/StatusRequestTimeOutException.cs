using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Client.Exceptions
{
  public class StatusRequestTimeOutException : Exception
  {
    public StatusRequestTimeOutException(string serverId) :
      base("Status request to " + serverId + "failed") 
    { }

  }
}
