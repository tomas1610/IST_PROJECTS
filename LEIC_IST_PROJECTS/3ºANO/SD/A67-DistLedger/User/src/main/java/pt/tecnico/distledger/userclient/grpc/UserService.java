package pt.tecnico.distledger.userclient.grpc;

import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.*;

public class UserService {

    /*TODO: The gRPC client-side logic should be here.
        This should include a method that builds a channel and stub,
        as well as individual methods for each remote operation of this service. */
        
    private ManagedChannel channel_naming, channel;
    private UserServiceGrpc.UserServiceBlockingStub stub;
    private NamingServerServiceGrpc.NamingServerServiceBlockingStub stub_lookup;
    private HashMap<ServerInfo,Integer> mapTS;
    private List<Integer> listTS;


    public UserService(String host, int port) {
        this.channel_naming = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub_lookup = NamingServerServiceGrpc.newBlockingStub(this.channel_naming);
        this.mapTS = new HashMap<ServerInfo,Integer>();
        this.listTS = new ArrayList<>();
        this.updateTS();
    }

    public void createAccount(String userId, String qualificator) {
        this.updateTS();
        CreateAccountRequest request = CreateAccountRequest.newBuilder().setUserId(userId).addAllPrevTS(this.createListTS()).build();
        find_connection(qualificator);

        CreateAccountResponse response = this.stub.createAccount(request);

        updateTS_with_answer(response.getTSList());
        
        channel.shutdown();
        displayMSG(response.getError());
    }

    public void balance(String userId, String qualificator) {
        this.updateTS();
        BalanceRequest request = BalanceRequest.newBuilder().setUserId(userId).addAllPrevTS(this.createListTS()).build();

        find_connection(qualificator);

        BalanceResponse response = this.stub.balance(request);
        channel.shutdown();
        displayMSG(response.getError());
        if (response.getError() == ErrorMsg.OK && response.getValue() >= 0){
            System.out.println(response.getValue());
            updateTS_with_answer(response.getValueTSList());
        }
    }

    public void transferTo(String accountFrom, String accountTo, int amount, String qualificator) {
        this.updateTS();
        TransferToRequest request = TransferToRequest.newBuilder().setAccountFrom(accountFrom).setAccountTo(accountTo).setAmount(amount).addAllPrevTS(this.createListTS()).build();

        find_connection(qualificator);

        TransferToResponse response = this.stub.transferTo(request);
        
        updateTS_with_answer(response.getTSList());

        channel.shutdown();
        displayMSG(response.getError());
    }

    public List<String> lookup(String serviceName, String qualificator) {

        List<String> servers = new ArrayList<>();

        LookupRequest request = LookupRequest.newBuilder().setServiceName(serviceName).setQualificator(qualificator).build();

        LookupResponse response = this.stub_lookup.lookup(request);

        for (ServerInfo info : response.getServerEntryList()){
            servers.add(info.getTarget());
        }

        return servers;
    }

    public void find_connection(String qualificator) {
        List<String> servers = new ArrayList<String>();
        servers = this.lookup("DistLedger",qualificator);
        if (servers.size() == 0)
            return;
        
        String[] target = servers.get(0).split(":");
        String host = target[0];
        int port = Integer.parseInt(target[1]);

        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = UserServiceGrpc.newBlockingStub(this.channel);
    }

    public void updateTS_with_answer(List<Integer> replicaTS) {
        for (Map.Entry<ServerInfo,Integer> entry : this.mapTS.entrySet()){
            if (replicaTS.get((int) entry.getKey().getQualificator().charAt(0) - (int) 'A') > entry.getValue())
                this.mapTS.put(entry.getKey(),replicaTS.get((int) entry.getKey().getQualificator().charAt(0) - (int) 'A'));
        }
    }

    public List<Integer> createListTS() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 3; i++)
            list.add(0);
        
        for (Map.Entry<ServerInfo,Integer> entry : this.mapTS.entrySet()){
            list.set((int) entry.getKey().getQualificator().charAt(0) - (int) 'A',entry.getValue());
        }

        return list;
    }

    public void updateTS() {
        LookupRequest request = LookupRequest.newBuilder().setServiceName("DistLedger").setQualificator("").build();

        LookupResponse response = this.stub_lookup.lookup(request);

        for (ServerInfo info : response.getServerEntryList()){
            if (!this.mapTS.containsKey(info)){
                this.mapTS.put(info,0);
            }
        }

        for (Map.Entry<ServerInfo,Integer> entry : this.mapTS.entrySet()){
            if (!response.getServerEntryList().contains(entry.getKey()))
                this.mapTS.remove(entry.getKey());
        }
    }

    public void incrementTS(String qualificator) {
        for (Map.Entry<ServerInfo,Integer> entry : this.mapTS.entrySet()){
            if (entry.getKey().getQualificator().equals(qualificator)){
                this.mapTS.put(entry.getKey(),entry.getValue() + 1);
            }
        }
    }

    public void shutdown() {
        this.channel_naming.shutdown();
    }

    private static void displayMSG(ErrorMsg msg) {
		switch (msg) {
            case OK:
                System.out.println("OK");
                break;
            case ERROR_CREATE:
                System.out.println("ACCOUNT ALREADY EXISTS");
                break;
            case ERROR_BALANCE:
                System.out.println("NO ACCOUNT TO CHECK BALANCE");
                break;
            case ERROR_DELETE:
                System.out.println("NO ACCOUNT TO DELETE");
                break;
            case ERROR_TRANSFER_ACCOUNT_FROM:
                System.out.println("ORIGIN OF TRANSFER DONT EXISTS");
                break;
            case ERROR_TRANSFER_ACCOUNT_DEST:
                System.out.println("DESTINATION OF TRANSFER DONT EXISTS");
                break;
            case ERROR_TRANSFER_AMOUNT:
                System.out.println("DONT HAVE BALANCE ENOUGH TO MAKE THE TRANSFER");
                break;
            case ERROR_UNAVAILABLE:
                System.out.println("UNAVAILABLE");
                break;
            case ERROR_BALANCE_NOT_ZERO:
                System.out.println("BALANCE NOT ZERO");
                break;
            case ERROR_CANNOT_BROKER:
                System.out.println("CANNOT REMOVE BROKER ACCOUNT");
                break;
             case ERROR_PRIMARY_SERVER_UNAVAILABLE:
                System.out.println("PRIMARY SERVER IS UNAVAILABLE");
                break;
            case ERROR_WRITE_OPERATION_IN_SECONDARY_SERVER:
                System.out.println("CANNOT EXECUTE WRITE OPERATION ON SECONDARY SERVER");
                break;
            case ERROR_CANNOT_PROPAGATE_SECONDARY_SERVER:
                System.out.println("CANNOT PROPAGATE OPERATION TO SECONDARY SERVER");
                break;
            case SERVER_LATE:
                System.out.println("SERVER IS NOT UPDATE , GOSSIP TO MAKE OPERATION");
                break;
		    default:
			    System.out.println("UNKNOWN");
			    break;
		}
	}
}
