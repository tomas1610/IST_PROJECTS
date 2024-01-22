using Grpc.Core;
using Grpc.Net.Client;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.IO;
using System.Security;
using System.Threading;
using System.Threading.Tasks;
using TransactionServer;

namespace TransactionManager {
  
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

      Dictionary<int, List<string>> tMsSusCrashed = new Dictionary<int, List<string>>();

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
        else if (readTs == true)
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
        if (args[i] == "-ts")
        {
          timeslot = Int32.Parse(args[++i]);
          readTs = true;
          continue;
        }
        else if (readTs == true)
        {
          if (args[i] == id)
          {
            if (tMsSusCrashed.ContainsKey(timeslot))
            {
              tMsSusCrashed[timeslot].Add(args[i+1]);
            }
            else
            {
              tMsSusCrashed[timeslot] = new List<string> { args[i+1] };
            }
          }
          i++;
        }
      }

      foreach(var entry in TMUrls) { Console.WriteLine("Id: " + entry.Key + "Url: " + entry.Value); }
      foreach(var entry in LMUrls) { Console.WriteLine("Id: " + entry.Key + "Url: " + entry.Value); }
      foreach(int entry in timeslotsIAmCrashed) { Console.WriteLine(entry.ToString()); }
      foreach(var entry in tMsSusCrashed) 
      { 
        Console.Write("Timeslot: " + entry.Key);
        foreach(string e in entry.Value) { Console.Write(e); }
        Console.WriteLine();
      }

      TMServerState state = new TMServerState(id,TMUrls, LMUrls, timeslotsIAmCrashed, tMsSusCrashed);

      serverPort = new ServerPort(hostname, port, ServerCredentials.Insecure);
      Server server1 = new Server
      {
        Services = 
        { 
          ClientToTMService.BindService(new TMServerService(state)),
          TMTransPropagationService.BindService(new TransPropagationReceiveService(state)),
          LeaseRequestService.BindService(new LeaseAssignmentReceiver(state)),
          StatusRequestService.BindService(new StatusPresentLogicService(state))
        },
        Ports = { serverPort }
      };

      server1.Start();
      state.Server = server1;

      Console.WriteLine($"Transaction Manager Server started with identifer: {id}, host: {hostname} and port: {port}");
      while (true) { }
    }
    
  }
}
