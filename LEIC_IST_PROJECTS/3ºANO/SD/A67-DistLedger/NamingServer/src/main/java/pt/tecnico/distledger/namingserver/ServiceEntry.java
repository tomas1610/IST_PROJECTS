package pt.tecnico.distledger.namingserver;

import java.util.List;
import java.net.*;
import java.util.*;

/* This class contains a service name and a set of Server EntryS */

public class ServiceEntry {
    
    private String serviceName;
    private List<ServerEntry> serviceEntryS;

    public ServiceEntry(String serviceName) {
        this.serviceName = serviceName;
        this.serviceEntryS = new ArrayList<>();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<ServerEntry> getServiceEntryS() {
        return serviceEntryS;
    }

    public void setServiceEntryS(List<ServerEntry> serviceEntryS) {
        this.serviceEntryS = serviceEntryS;
    }

    public void addServerEntry(ServerEntry serverEntry) {
        if (!serviceEntryS.contains(serverEntry))
            this.serviceEntryS.add(serverEntry);
    }

    public void removeServerEntry(ServerEntry serverEntry) {
        this.serviceEntryS.remove(serverEntry);
    }

    @Override
    public String toString() {
        return "serviceName = " + serviceName +
            "service Entrys = " + serviceEntryS + 
            "\n";
    }
}
