using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TransactionServer.Exceptions
{
  public class TMQuorumNotAchieved : Exception
  {
    public TMQuorumNotAchieved() : 
      base("Unable to get a quorum majority of responses")
    { }
  }
}
