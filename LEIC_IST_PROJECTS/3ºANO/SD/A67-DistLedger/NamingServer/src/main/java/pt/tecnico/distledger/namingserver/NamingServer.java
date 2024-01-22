package pt.tecnico.distledger.namingserver;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.*;
import java.util.stream.Collectors;

import pt.tecnico.distledger.namingserver.exceptions.RegisterException;
import pt.tecnico.distledger.namingserver.exceptions.DeleteException;
import java.io.IOException;

/* This class contais all informations needed by server that means a map that associates a service name to a service entry */

public class NamingServer {

    private Map<String, ServiceEntry> register_NS;

    public NamingServer() {
        this.register_NS = new HashMap<>();
    }

    public synchronized void register(String serviceName, String qualificator, String target) throws RegisterException {
        ServerEntry serverEntry;
        ServiceEntry serviceEntry;

        if (register_NS.containsKey(serviceName)) {
            serverEntry = new ServerEntry(target, qualificator);
            serviceEntry = register_NS.get(serviceName);
            for (ServerEntry entry : serviceEntry.getServiceEntryS()) {
                if (target.equals(entry.getTarget()))
                    throw new RegisterException("Not possible to register the server");
            }
            serviceEntry.addServerEntry(serverEntry);
        }
        else {
            serverEntry = new ServerEntry(target, qualificator);
            serviceEntry = new ServiceEntry(serviceName);
            serviceEntry.addServerEntry(serverEntry);
            register_NS.put(serviceName, serviceEntry);
        }
    }
    
    public synchronized List<ServerEntry> lookup(String serviceName, String qualificator) {
        List<ServerEntry> lookupList = new ArrayList<>();

        if (qualificator.equals("")) {
            if (register_NS.containsKey(serviceName)) {
                for (ServerEntry serverEntry :register_NS.get(serviceName).getServiceEntryS()) {
                    lookupList.add(serverEntry);
                }
            }
        }
        else if (register_NS.containsKey(serviceName)) {
            ServiceEntry serviceEntry = register_NS.get(serviceName);
            for (ServerEntry serverEntry : serviceEntry.getServiceEntryS()) {
                if (serverEntry.getQualificator().equals(qualificator)) {
                    lookupList.add(serverEntry);
                }
            }
        }

        return lookupList;
    }

    public synchronized void delete(String serviceName, String target) throws DeleteException {
        if (register_NS.containsKey(serviceName)) {
            ServiceEntry serviceEntry = register_NS.get(serviceName);
            List<ServerEntry> matching_server = serviceEntry.getServiceEntryS().stream()
                .filter(entry -> target.equals(entry.getTarget()))
                .collect(Collectors.toList());

            if (matching_server.size() == 0)
                throw new DeleteException("Not possible to remove the server");
            
            for (ServerEntry serverEntry : matching_server) {
                serviceEntry.removeServerEntry(serverEntry);
            }
        }
        else {
            throw new DeleteException("Not possible to remove the server");
        } 
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        
        NamingServer NS = new NamingServer();
        
        System.out.println(NamingServer.class.getSimpleName());

        final int port = Integer.parseInt(args[0]);
        
        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port).addService(new NamingServerServiceImpl(NS)).build();

        // Start the server
        server.start();
        
        // Server threads are running in the background.
        System.out.println("Server started");

        // Do not exit the main thread. Wait until server is terminated.
        server.awaitTermination();
    }
}
