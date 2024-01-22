using Client.Exceptions;
using Grpc.Core;
using Grpc.Net.Client;
using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Cache;
using System.Security;
using System.Security.Cryptography.X509Certificates;
using System.Security.Principal;
using System.Threading;
using System.Threading.Tasks;

namespace Client
{
  class Program
  {
    public static void Main(string[] args)
    {
      string id;
      string? line;
      string file;

      int transactionNumber = 0;

      // Holds the id´s of the transaction managers with their respective URL´s 
      Dictionary<string, string> TMUrls = new Dictionary<string, string>();

      // Holds the id´s of the transaction managers with their respective URL´s 
      Dictionary<string, string> LMUrls = new Dictionary<string, string>();

      if (args.Length < 2)
      {
        Console.WriteLine("Invalid number of arguments to start a client");
        return;
      }

      id = args[0];
      file = args[1];

      bool isTM = true;
      int i = 2;

      for (; i < args.Length; i++)
      {
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
        if (!isTM)
        {
          LMUrls[args[i]] = args[i + 1];
          i++;
        }
      }

      Console.WriteLine("Id: " + id);
      foreach (KeyValuePair<string, string> tm in TMUrls)
      {
        Console.WriteLine($"TM: {tm.Key} with URL: {tm.Value}");
      }
      foreach (KeyValuePair<string, string> lm in LMUrls)
      {
        Console.WriteLine($"LM: {lm.Key} with URL: {lm.Value}");
      }
      Console.WriteLine();

      TransactionSubmitSend submitter = new TransactionSubmitSend(id, TMUrls);
      StatusLogic statusRequester = new StatusLogic(id, TMUrls, LMUrls);
      
      while (true)
      {
        StreamReader sr = new StreamReader(file);

        line = sr.ReadLine();

        while (line != null)
        {
          string[] parsed_line = line.Split(' ');
          switch (parsed_line[0])
          {
            case "#":                                               // Comment
              break;
            case "T":                                               // Transaction
              Console.WriteLine("------ TRANSACTION READ ------");
              parseCommandT(id, parsed_line[1], parsed_line[2], TMUrls, submitter, ref transactionNumber);
              Console.WriteLine("------ TRANSACTION END ------");
              break;
            case "W":                                             // WAIT
              Console.WriteLine("----- SLEEPING... -----");
              Thread.Sleep(Int32.Parse(parsed_line[1]));
              break;
            case "S":                                               // STATUS
              statusRequester.RequestStatusToAllServers();
              break;
          }

          line = sr.ReadLine();
        }
      }
    }

    private static void parseCommandT(string id, string read_list, string write_list, 
      Dictionary<string, string> TMUrls, TransactionSubmitSend submitter, ref int transactionNumber)
    {
      char[] delimiters = new char[] { ' ', ',', '"', '(', ')' };
      Transaction transaction = new Transaction();

      string[] read_split = read_list.Split(delimiters, StringSplitOptions.RemoveEmptyEntries);

      foreach (string s in read_split)
      {
        transaction.ReadOperations.Add(new ReadOperation { DadIntId = s });
      }

      string[] write_split = write_list.Split(new char[] { '(', ')', '<', '>' }, 
        StringSplitOptions.RemoveEmptyEntries);
      string[] final_write_split = string.Concat(write_split).Split(',', 
        StringSplitOptions.RemoveEmptyEntries);             // Divides every pair

      for (int i = 0; i < final_write_split.Length; i = i + 2)
      {
        string aux = final_write_split[i].Replace("\"", "");             // removes " char from the key name 
        KeyValuePair<string, int> kvp = new KeyValuePair<string, int>(aux, Int32.Parse(final_write_split[i + 1]));
        DadIntType dadInt = new DadIntType { Key = kvp.Key, Value = kvp.Value };
        transaction.WriteOperations.Add(new WriteOperation { DadInt = dadInt });
      }

      /*
      Console.WriteLine("Transaction Number: " + transactionNumber.ToString());
      foreach (ReadOperation readOp in transaction.ReadOperations)
      {
        Console.WriteLine("readOp: " +  readOp.DadIntId); 
      }
      foreach (WriteOperation writeOp in transaction.WriteOperations)
      {
        Console.WriteLine("writeOp: (" + writeOp.DadInt.Key + ", " + writeOp.DadInt.Value + ")");
      }
      */

      submitter.SubmitTransaction(transaction, transactionNumber);
      transactionNumber++;
    }

  }
}

