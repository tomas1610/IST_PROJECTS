using Grpc.Core;
using Grpc.Net.Client;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.IO;
using System.Security;
using System.Threading;
using System.Threading.Tasks;

namespace LeaseManager
{

  public class Program
  {
    public static void Main(string[] args)
    {
      string id;
      int port;
      string hostname;
      ServerPort serverPort;

      Dictionary<string, string> TMUrls = new Dictionary<string, string>();

      Dictionary<string, string> LMUrls = new Dictionary<string, string>();

      List<int> timeslotsIAmCrashed = new List<int>();

      Dictionary<int, List<string>> lMsSusCrashed = new Dictionary<int, List<string>>();

      // Time variables
      int timeslotDuration;
      int numTimeslots;
      string wallTime;

      if (args.Length < 2)
      {
        Console.WriteLine("Please provide the id, the Url to receive requests ");
        return;
      }

      id = args[0];
      Uri uri = new Uri(args[1]);
      hostname = uri.Host;
      port = uri.Port;

      bool isTM = true;
      int i = 2;

      for (; i < args.Length; i++)
      {
        if (args[i].Equals("-st"))
        {
          i++;
          break;
        }

        if (args[i].Equals("-tm"))
        {
          isTM = true;
          continue;
        }

        if (args[i].Equals("-lm"))
        {
          isTM = false;
          continue;
        }

        if (isTM)
        {
          TMUrls[args[i]] = args[i + 1];
          i++;                            // We skip the next iteration since its the value 
        }
        else
        {
          LMUrls[args[i]] = args[i + 1];
          i++;
        }
      }

      bool readTs = true;
      int timeslot = 0;

      // we are on the -st flag
      for (; i < args.Length; i++)
      {
        if (args[i] == "-sus")
        {
          i++;
          break;
        }
        if (args[i] == "-ts")
        {
          timeslot = Int32.Parse(args[++i]);
          readTs = true;
          continue;
        }
        if (readTs == true)
        {
          if (args[i] == id)
          {
            timeslotsIAmCrashed.Add(timeslot);
          }
        }
      }

      // we are on the -sus flag
      for (; i < args.Length; i++)
      {
        if (args[i] == "-times")
        {
          i++;
          break;
        }
        if (args[i] == "-ts")
        {
          timeslot = Int32.Parse(args[++i]);
          readTs = true;
          continue;
        }
        if (readTs == true)
        {
          if (args[i] == id)
          {
            if (lMsSusCrashed.ContainsKey(timeslot))
            {
              lMsSusCrashed[timeslot].Add(args[i+1]);
            }
            else
            {
              lMsSusCrashed[timeslot] = new List<string> { args[i+1] };
            }
          }
          i++;
        }
      }

      timeslotDuration = Int32.Parse(args[i]);
      numTimeslots = Int32.Parse(args[i+1]);
      wallTime = args[i+2];

      LMServerState state = new LMServerState(id, TMUrls, LMUrls, timeslotsIAmCrashed, lMsSusCrashed, timeslotDuration, numTimeslots, wallTime);

      serverPort = new ServerPort(hostname, port, ServerCredentials.Insecure);
      Server server1 = new Server
      {
        Services =
        {
          LeaseRequestService.BindService(new LeaseRequestReceiver(state)),
          AcceptService.BindService(new AcceptorReceiver(state)),
          PrepareService.BindService(new PaxosPrepareReceiver(state)),
          StatusRequestService.BindService(new StatusPresentLogicService(state))
        },
        Ports = { serverPort }
      };

      server1.Start();

      Console.WriteLine($"Lease Manager Server started with identifer: {id}, host: {hostname} and port: {port}");
      //Configuring HTTP for client connections in Register method
      AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
      while (true) { }
    }

  }
}

