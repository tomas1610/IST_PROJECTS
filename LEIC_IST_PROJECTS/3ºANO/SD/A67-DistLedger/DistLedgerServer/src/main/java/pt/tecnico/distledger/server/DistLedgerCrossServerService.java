package pt.tecnico.distledger.server;

import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.operation.TransferOp;

import java.util.*;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class DistLedgerCrossServerService {

    private ManagedChannel channel;
    private NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;
    private DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub_server_secondary;
    private ServerState state;

    public DistLedgerCrossServerService(int port, ServerState state) {
        this.channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build();
        this.stub = NamingServerServiceGrpc.newBlockingStub(this.channel);
        this.state = state;
    }

    public void register(String serviceName, String qualificator, String target) {
        RegisterRequest request = RegisterRequest.newBuilder().setServiceName(serviceName).setQualificator(qualificator).setTarget(target).build();

        RegisterResponse response = this.stub.register(request);
    }

    public List<String> lookup(String serviceName, String qualificator) {
        List<String> servers = new ArrayList<>();

        LookupRequest request = LookupRequest.newBuilder().setServiceName(serviceName).setQualificator(qualificator).build();

        LookupResponse response = this.stub.lookup(request);

        for (ServerInfo info : response.getServerEntryList()){
            servers.add(info.getTarget());
        }

        return servers;
    }

    public void delete(String serviceName, String target) {
        DeleteRequest request = DeleteRequest.newBuilder().setServiceName(serviceName).setTarget(target).build();

        DeleteResponse response = this.stub.delete(request);
    }

    public void gossip(List<Operation> ledger,String qualificator) {

        this.state.setMapTS(this.updateTS(this.state.getMapTS()));
        this.state.sortLedger();

        int min_index = this.state.getMinIndex(qualificator);

        for (Map.Entry<ServerInfo,Integer> entry : this.state.getMapTS().entrySet()){
            if (!entry.getKey().getQualificator().equals(qualificator))
                this.propagateState(ledger,entry.getKey().getQualificator(), qualificator,entry.getValue());
        }

    }

    public int checkState(String qualificator) {

        this.state.setMapTS(this.updateTS(this.state.getMapTS()));

        for (Map.Entry<ServerInfo,Integer> entry : this.state.getMapTS().entrySet()){
            if (!entry.getKey().getQualificator().equals(qualificator)){
                StateRequest request = StateRequest.newBuilder().build();
                find_connection(entry.getKey().getQualificator());
                StateResponse response = this.stub_server_secondary.checkState(request);
                if (response.getError() == ErrorPropagate.UNAVAILABLE)
                    return 0;
            }
        }

        return 1;
    }

    public PropagateStateResponse propagateState(List<Operation> ledger, String qualificator, String server, int min_index) {
        PropagateStateRequest.Builder requestBuilder = PropagateStateRequest.newBuilder();

        DistLedgerCommonDefinitions.LedgerState.Builder ledgerStateBuilder = DistLedgerCommonDefinitions
                .LedgerState.newBuilder();

        for (int i = min_index; i < ledger.size(); i++) {
            Operation operation = ledger.get(i);
            DistLedgerCommonDefinitions.Operation.Builder operationBuilder = DistLedgerCommonDefinitions.Operation.newBuilder();
            operationBuilder.setType(operation.getType());
            operationBuilder.setAccount(operation.getAccount());
            if (operation instanceof TransferOp){
                operationBuilder.setDestAccount(((TransferOp) operation).getDestAccount());
                operationBuilder.setAmount(((TransferOp) operation).getAmount());
            }
            operationBuilder.addAllPrevTS(operation.getOperationPrevTS());
            operationBuilder.addAllTS(operation.getOperationTS());
            ledgerStateBuilder.addLedger(operationBuilder.build());
        }

        PropagateStateRequest request = requestBuilder.setLedgerState(ledgerStateBuilder.build()).setQualificator(server).build();
        find_connection(qualificator);
        PropagateStateResponse response = this.stub_server_secondary.propagateState(request);
        return response;
    }

    public HashMap<ServerInfo,Integer> updateTS(HashMap<ServerInfo,Integer> mapTS) {
        LookupRequest request = LookupRequest.newBuilder().setServiceName("DistLedger").setQualificator("").build();

        LookupResponse response = this.stub.lookup(request);

        for (ServerInfo info : response.getServerEntryList()){
            if (!mapTS.containsKey(info)){
                mapTS.put(info,0);
            }
        }

        for (Map.Entry<ServerInfo,Integer> entry : mapTS.entrySet()){
            if (!response.getServerEntryList().contains(entry.getKey()))
                mapTS.remove(entry.getKey());
        }

        return mapTS;
    }

    public void find_connection(String qualificator) {
        List<String> servers = new ArrayList<String>();
        servers = lookup("DistLedger",qualificator);
        if (servers.size() == 0)
            return;
        
        String[] target = servers.get(0).split(":");
        String host = target[0];
        int port = Integer.parseInt(target[1]);

        ManagedChannel channel_secondary = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub_server_secondary = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel_secondary);
    }
}