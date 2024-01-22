using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TransactionServer.Exceptions
{
  public class UnableToRequestLease : Exception
  {
    public UnableToRequestLease() :
      base("Unable to request the lease")
    { }
  }
}
