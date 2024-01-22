using System;
using System.Diagnostics;
using System.IO;
using System.Runtime.CompilerServices;

namespace Manager
{
  public class Program
  {
    public static void Main(string[] args)
    {
      string? line;
      string scriptName;

      List<Process> processList = new List<Process>();

      // Holds all of the id´s of the server processes, both TM and LM to be initiated in the order they were read
      List<string> serverProcs = new List<string>();

      // Holds the id´s of the transaction managers with their respective URL´s to then be initiated 
      Dictionary<string, string> TMUrls = new Dictionary<string, string>();

      // Holds the id´s of the lease managers with their respective URL´s to then be initiated 
      Dictionary<string, string> LMUrls = new Dictionary<string, string>();

      // Holds the id´s of the clients with their respective script names to then be initiated 
      Dictionary<string, string> clientScripts = new Dictionary<string, string>();

      // Holds the processes that are TRULY crashed for each timeslot
      Dictionary<int, List<string>> crashedProcs = new Dictionary<int, List<string>>();

      // For each timeslot, holds the list of processes that another process SUSPECTS to be crashed
      Dictionary<int, Dictionary<string, List<string>>> susProcsState = new Dictionary<int, Dictionary<string, List<string>>>();

      int numTimeslots = 0;
      string wallTime = "";
      int timeslotDuration = 0;

      if (args.Length != 1)
      {
        Console.WriteLine("Error: Please provide the name of the script to run!");
        return;
      }
      scriptName = args[0];

      StreamReader sr = new StreamReader(scriptName);

      line = sr.ReadLine();
      while (line != null)
      {
        switch (line[0])
        {
          case '#': // Line is a comment
            break;
          case 'P':
            ParseCommandP(line, ref TMUrls, ref LMUrls, ref clientScripts, ref serverProcs);
            break;
          case 'S':
            numTimeslots = Int32.Parse(line.Split(' ')[1]);
            break;
          case 'T':
            wallTime = line.Split(' ')[1];
            break;
          case 'D':
            timeslotDuration = Int32.Parse(line.Split(' ')[1]);
            break;
          case 'F':
            ParseCommandF(line, ref crashedProcs, ref susProcsState, serverProcs);
            break;
        }
        line = sr.ReadLine();
      }
      sr.Close();

      /*
      foreach (KeyValuePair<int, List<string>> entry in crashedProcs)
      {
        Console.Write("Timeslot: " + entry.Key + " -> ");
        foreach (string s in entry.Value) Console.Write(s + ", ");
      }
      Console.WriteLine();
      foreach (var e in susProcsState)
      {
        Console.Write("Timeslot: " + e.Key);
        foreach (var e2 in e.Value)
        {
          Console.Write("; TM that suspects: " + e2.Key);
          foreach (var e3 in e2.Value)
          {
            Console.WriteLine("; TMs suspected: " + e3);
          }
        }
      }
      Console.WriteLine();
      */

      LaunchAllProcesses(TMUrls, LMUrls, clientScripts, serverProcs, crashedProcs, susProcsState,
        timeslotDuration, numTimeslots, wallTime, processList);

      Console.WriteLine();
      Console.WriteLine("Press q and enter to kill all the processes");
      string? l = Console.ReadLine();
      if (l != null && l == "q")
      {
        Console.WriteLine("q pressed");
        KillAllProcess(processList);
      }
    }

    private static void ParseCommandP(string line, ref Dictionary<string, string> TMDic,
          ref Dictionary<string, string> LMDic, ref Dictionary<string, string> clientScripts,
          ref List<string> serverProces)
    {
      string[] parsedLine = line.Split(' ');

      string id = parsedLine[1];

      if (parsedLine[2] == "T")
      {
        string URL = parsedLine[3];
        TMDic[id] = URL;
        serverProces.Add(id);
      }
      if (parsedLine[2] == "L")
      {
        string URL = parsedLine[3];
        LMDic[id] = URL;
        serverProces.Add(id);
      }
      if (parsedLine[2] == "C")
      {
        string clientScriptName = parsedLine[3];
        clientScripts[id] = clientScriptName;
      }
    }

    private static void ParseCommandF(string line, ref Dictionary<int, List<string>> crashedProcs,
          ref Dictionary<int, Dictionary<string, List<string>>> susProcsState, List<string> serverProcs)
    {
      string[] parsedLine = line.Split(' ');
      int i;

      int timeslot = Int32.Parse(parsedLine[1]);

      for (i = 2; i < parsedLine.Length; i++)
      {
        if (parsedLine[i] != "N" && parsedLine[i] != "C")
        {
          break;
        }
        bool isNormal = parsedLine[i] == "N" ? true : false;
        if (!isNormal)
        {
          if (crashedProcs.ContainsKey(timeslot))
          {
            crashedProcs[timeslot].Add(serverProcs[i - 2]);
          }
          else
          {
            crashedProcs[timeslot] = new List<string> { serverProcs[i - 2] };
          }
        }
      }

      for (; i < parsedLine.Length; i++)
      {
        string idSuspicious = parsedLine[i].Trim('(', ')').Split(',')[0];
        string idSuspected = parsedLine[i].Trim('(', ')').Split(',')[1];
        if (!susProcsState.ContainsKey(timeslot))
        {
          susProcsState[timeslot] = new Dictionary<string, List<string>>();
        }
        if (susProcsState[timeslot].ContainsKey(idSuspicious))
        {
          susProcsState[timeslot][idSuspicious].Add(idSuspected);
        }
        else
        {
          susProcsState[timeslot][idSuspicious] = new List<string> { idSuspected };
        }
      }
    }

    /// <summary>
    /// Creates a string containing ids and URLs of every TM and LM
    /// </summary>
    private static string CreateURLString(Dictionary<string, string> TMDic, Dictionary<string, string> LMDic)
    {
      string URL = " -tm";


      foreach (KeyValuePair<string, string> pair in TMDic)
      {
        URL = String.Concat(URL, " " + pair.Key + " " + pair.Value);
      }

      URL = String.Concat(URL, " -lm");

      foreach (KeyValuePair<string, string> pair in LMDic)
      {
        URL = String.Concat(URL, " " + pair.Key + " " + pair.Value);
      }

      return URL;
    }

    /// <summary>
    /// Launches all the processes of the system
    /// </summary>
    private static void LaunchAllProcesses(Dictionary<string, string> TMDic,
           Dictionary<string, string> LMDic, Dictionary<string, string> clientScripts,
           List<string> serverProcesses, Dictionary<int, List<string>> crashedProcs,
           Dictionary<int, Dictionary<string, List<string>>> susProcsState,
           int timeslotDuration, int numTimeslots, string wallTime, List<Process> processList)
    {
      foreach (string e in serverProcesses)
      {
        if (TMDic.ContainsKey(e))
        {
          LaunchNewProcess("TransactionServer", String.Concat("/k dotnet run " + e + " " + TMDic[e] +" " + CreateURLString(TMDic, LMDic) + " " + CreateStatesString(crashedProcs) + " " + CreateSusString(susProcsState)), processList);
          //break;
        }

        if (LMDic.ContainsKey(e))
        {
          LaunchNewProcess("LeaseManager", String.Concat("/k dotnet run " + e + " " + LMDic[e] +  " " + CreateURLString(TMDic, LMDic) +" " + CreateStatesString(crashedProcs) + " " + CreateSusString(susProcsState) + " " + CreateTimesString(timeslotDuration, numTimeslots, wallTime)), processList);
        }
      }

      foreach (KeyValuePair<string, string> pair in clientScripts)
      {
        string clScript = String.Concat("../Scripts/", pair.Value);
        LaunchNewProcess("Client", String.Concat("/k dotnet run " + pair.Key + " " + clScript + " " + CreateURLString(TMDic, LMDic)), processList);
        break;
      }
    }

    private static string CreateTimesString(int timeSlotDuration, int numTimeSlots, string wallTime)
    {
      return "-times " + timeSlotDuration.ToString() + " " + numTimeSlots.ToString() + " " + wallTime.ToString();
    }

    /// <sumary>
    /// Creates a String of crashedProcs for the processes to read
    /// <sumary>
    private static string CreateStatesString(Dictionary<int, List<string>> crashedProcs)
    {
      string result = "-st";

      foreach (KeyValuePair<int, List<string>> pair in crashedProcs)
      {
        result = String.Concat(result, " -ts ");
        result = String.Concat(result, pair.Key);
        foreach (string crashedProc in pair.Value)
        {
          result = String.Concat(result, " " + crashedProc);
        }

      }

      return result;
    }

    /// <sumary>
    /// Creates a String of susProcsState for the processes to read
    /// <sumary>
    private static string CreateSusString(Dictionary<int, Dictionary<string, List<string>>> susProcsState)
    {
      string result = "-sus";

      foreach (KeyValuePair<int, Dictionary<string, List<string>>> pair in susProcsState)
      {
        result = String.Concat(result, " -ts ");
        result = String.Concat(result, pair.Key);
        foreach (KeyValuePair<string, List<string>> pair_aux in pair.Value)
        {
          result = String.Concat(result, " " + pair_aux.Key);
          foreach (string aux in pair_aux.Value)
          {
            result = String.Concat(result, " " + aux);
          }
        }

      }

      return result;
    }

    private static void LaunchNewProcess(string name, string arguments, List<Process> processList)
    {
      //------------MUST CHANGE TO LOCAL PROJECT PATH------------
      string path = String.Concat("C:/Users/João Cruz/OneDrive/Ambiente de Trabalho/1 semestre/DAD/Project/Project/", name + "/");

      Console.WriteLine(arguments);

      ProcessStartInfo startInfo = new ProcessStartInfo
      {
        FileName = "cmd.exe",
        WorkingDirectory = path,
        UseShellExecute = true,
        Arguments = arguments,
        CreateNoWindow = false,
        WindowStyle = ProcessWindowStyle.Normal
      };

      Process procs = new Process { StartInfo = startInfo };

      try
      {
        procs.Start();
        processList.Add(procs);
      }
      catch (Exception ex)
      {
        Console.WriteLine("Error initiating new process {name}: " + ex.Message);
      }
    }

    private static void KillAllProcess(List<Process> processes)
    {
      foreach (Process p in processes)
      {
        p.Kill();
        p.CloseMainWindow();
        //p.Kill();
      }
    }

  }
}
