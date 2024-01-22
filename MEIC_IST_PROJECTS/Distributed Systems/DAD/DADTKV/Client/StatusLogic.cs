using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using Grpc.Core;
using Grpc.Net.Client;

namespace Client
{
  public class StatusLogic : StatusRequestService.StatusRequestServiceClient
  {
    private string id;

    // The clients, the Servers, both Lease and Transaction Managers
    private Dictionary<string, StatusRequestService.StatusRequestServiceClient> clients = new
      Dictionary<string, StatusRequestService.StatusRequestServiceClient>();

    // Keeps the last status request message that was sent to a Server
    private Dictionary<string, AsyncUnaryCall<StatusReply>> lastPropCall = new
        Dictionary<string, AsyncUnaryCall<StatusReply>>();

    public StatusLogic(string id, Dictionary<string, string> tMs, Dictionary<string, string> lMs)
    {
      this.id = id;

      foreach (KeyValuePair<string, string> entry in tMs)
      {
        AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
        try
        {
          GrpcChannel channel = GrpcChannel.ForAddress(entry.Value);
          clients.Add(entry.Key, new StatusRequestService.StatusRequestServiceClient(channel));
        } catch (Exception e) when (e is SocketException || e is HttpRequestException) 
        {
           Console.WriteLine(e.ToString());
        }
      }

      foreach (KeyValuePair<string, string> entry in lMs)
      {
        AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
        GrpcChannel channel = GrpcChannel.ForAddress(entry.Value);
        clients[entry.Key] = new StatusRequestService.StatusRequestServiceClient(channel);
      }
    }

    public async Task RequestStatusToAllServers()
    {
      Dictionary<string, StatusReply> replys = new Dictionary<string, StatusReply>();
      foreach (KeyValuePair<string, StatusRequestService.StatusRequestServiceClient> client in clients)
      {
        if (lastPropCall.ContainsKey(client.Key))
        {
          replys[client.Key] = await lastPropCall[client.Key].ResponseAsync;
          if (replys[client.Key].Ack)
          {
            Console.WriteLine($"Received Status Response from Server: {client.Key}");
          }
          lastPropCall.Remove(client.Key);
        }
        try
        {
          Console.WriteLine($"Sending Status request to TM: {client.Key}");
          lastPropCall[client.Key] = client.Value.RequestStatusAsync(new StatusRequest
          {
            ClientId = this.id
          }, new CallOptions(deadline: DateTime.UtcNow.AddSeconds(3)));
        } catch (Exception ex)
        {
          Console.WriteLine($"The request status for server {client.Key} timedout");
        }
      }
    }

  }
}
