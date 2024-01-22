package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*; 

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.*;
import java.util.stream.Collectors;



public class ServerState {

    private List<Operation> ledger;
    private Map<String, Integer> accounts;
    private int active;
    private String qualificator;
    private HashMap<ServerInfo,Integer> mapTS;
    private List<Integer> valueTS;

    public ServerState(String qualificator) {
        this.ledger = new ArrayList<>();
        this.accounts = new HashMap<>();
        this.accounts.put("broker",1000);
        this.active = 1;
        this.valueTS = new ArrayList<>();
        this.valueTS.add(0);
        this.valueTS.add(0);
        this.valueTS.add(0);
        this.mapTS = new HashMap<ServerInfo,Integer>();
        this.qualificator = qualificator;
    }

    public List<Operation> getLedger() {
        return ledger;
    }

    public void setLedger(List<Operation> ledger) {
        this.ledger = ledger;
    }

    public Map<String, Integer> getAccounts() {
        return accounts;
    }

    public void setAccounts(Map<String, Integer> accounts) {
        this.accounts = accounts;
    }

    public List<Integer> getValueTS() {
        return valueTS;
    }

    public void setValueTS(List<Integer> valueTS) {
        this.valueTS = valueTS;
    }

    public HashMap<ServerInfo,Integer> getMapTS(){
        return mapTS;
    }

    public void setMapTS(HashMap<ServerInfo,Integer> mapTS){
        this.mapTS = mapTS;
    }

    public String getQualificator() {
        return qualificator;
    }

    public int getMinIndex(String qualificator) {
        int index = 0;
        for (Map.Entry<ServerInfo,Integer> entry : this.mapTS.entrySet()){
            if (!entry.getKey().getQualificator().equals(qualificator)){
                index = this.valueTS.get((int) entry.getKey().getQualificator().charAt(0) - (int) 'A');
            }
        }
        return index;
    }

    public void incrementTS(String qualificator) {
        for (Map.Entry<ServerInfo,Integer> entry : this.mapTS.entrySet()){
            if (entry.getKey().getQualificator().equals(qualificator)){
                this.mapTS.put(entry.getKey(),entry.getValue() + 1);
            }
        }
    }

    public void incrementTSGossip(List<Integer> operationTS) {

        for (Map.Entry<ServerInfo,Integer> entry : this.mapTS.entrySet()){
            int index = (int) entry.getKey().getQualificator().charAt(0) - (int) 'A';
            if (operationTS.get(index) > entry.getValue())
                this.mapTS.put(entry.getKey(),operationTS.get(index));
        }
    }

    public void incrementValueTSReplic() {

        List<Integer> list = this.createListTS();

        for (int i = 0; i < list.size(); i++){
            if (list.get(i) > this.valueTS.get(i))
                this.valueTS.set(i,list.get(i));
        }
    }

    public void incrementValueTS(List<Integer> operationTS) {

        for (int i = 0; i < operationTS.size(); i++){
            if (operationTS.get(i) > this.valueTS.get(i))
                this.valueTS.set(i,operationTS.get(i));
        }
    }

    public void incrementTSwithOperation(List<Integer> operationTS){
        for (int i = 0; i < operationTS.size(); i++){
            if (operationTS.get(i) > this.valueTS.get(i))
                this.valueTS.set(i,operationTS.get(i));
            }
    }

    public boolean containOperation(List<Integer> operationTS) {
    
        return this.ledger.stream().anyMatch(operation -> operation.getOperationPrevTS().equals(operationTS));
    }

    public List<Integer> createListTS() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i< 3; i++)
            list.add(0);

        for (Map.Entry<ServerInfo,Integer> entry : this.mapTS.entrySet()){
            list.set((int) entry.getKey().getQualificator().charAt(0) - (int) 'A',entry.getValue());
        }
        return list;
    }

    public int compareTS(List<Integer> operationTS) {       // return 1 if operation is stable

        for (int i = 0; i < operationTS.size(); i++){
            if (operationTS.get(i) > this.valueTS.get(i))
                return 0;
        }
        return 1;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public void addOperation(Operation operation) {
        this.ledger.add(operation);
    }

    public void removeLastOperation() {
        if (this.ledger.size() > 0)
            this.ledger.remove(this.ledger.size()-1);
    }

    //Check if this userId already has an account and if not creates it
    public synchronized int createAccount(String userId, int stable) {
        if (stable == 0)
            addOperation(new CreateOp(userId));
        else {
            Operation operation = new CreateOp(userId);
            addOperation(operation);
            executeCreate(userId);
            return 1;
        }
 
        return 0;
    }

    public synchronized int executeCreate(String userId) {
        if (this.accounts.containsKey(userId))
            return -1;
        this.accounts.put(userId,0);
        return 1;
    }

    //Returns the balance of account if account exists or -1 if the account doesnt exists
    public synchronized int balance(String userId) {                             
        if (!this.accounts.containsKey(userId))
            return -1;
        return this.accounts.get(userId);
    }

    //Verify the existence of both accounts (from and destination) and if fromAccount's balance is enough to realize the operation 
    public synchronized int transferTo(String userFrom, String userTo, int amount, int stable) {  
        if (stable == 0){
            addOperation(new TransferOp(userFrom, userTo, amount));
        }
        else {          
            Operation operation = new TransferOp(userFrom, userTo, amount);
            addOperation(operation);
            executeTransfer(userFrom, userTo, amount);
            return 1;
        }
        return 0;
    }

    public synchronized int executeTransfer(String userFrom, String userTo, int amount) {
        int balance1 = balance(userFrom);
        int balance2 = balance(userTo);
        if (balance1 < 0)
            return -1;
        if (balance2 < 0)
            return -2;
        if (balance1 < amount)
            return -3;
        this.accounts.put(userFrom,balance1-amount);
        this.accounts.put(userTo,balance2 + amount);
        return 1;
    }

    public void setTS(List<Integer> TS) {
        for (Map.Entry<ServerInfo,Integer> entry : this.mapTS.entrySet()){
            this.mapTS.put(entry.getKey(),TS.get((int) entry.getKey().getQualificator().charAt(0) - (int) 'A'));
        }
    }

    public int already_Executed(List<Integer> operationTS) {

        int sum = operationTS.stream().mapToInt(Integer::intValue).sum();
        int value = this.valueTS.stream().mapToInt(Integer::intValue).sum();

        if (sum < value)
            return 1;
 
        return 0;
    }

    public synchronized void execute_all_unstable(){

        for (Operation operation : this.ledger) {
            System.out.println(operation.getOperationPrevTS());
            System.out.println(operation.getOperationTS());
            System.out.println(this.valueTS);
            System.out.println(this.createListTS());
            if (already_Executed(operation.getOperationPrevTS()) == 1)
                continue;
            if (compareTS(operation.getOperationPrevTS()) == 0)
                break;
            execute_operation(operation);
            operation.setOperationTS(this.valueTS);
        }
    }

    public int execute_operation(Operation operation) {
        this.incrementTSwithOperation(operation.getOperationTS());
        //operation.setOperationTS(this.valueTS);
        if (operation.getType() == OperationType.OP_CREATE_ACCOUNT)
            return executeCreate(operation.getAccount());
        else if(operation.getType() == OperationType.OP_TRANSFER_TO)
            return executeTransfer(operation.getAccount(),((TransferOp) operation).getDestAccount(),((TransferOp) operation).getAmount());
        return -1;
    }

    public void sortLedger(){
        Collections.sort(ledger, new Comparator<Operation>() {
            @Override
            public int compare(Operation operation_1, Operation operation_2) {
                int sum1 = operation_1.getOperationPrevTS().stream().mapToInt(Integer::intValue).sum();
                int sum2 = operation_2.getOperationPrevTS().stream().mapToInt(Integer::intValue).sum();
                return Integer.compare(sum1,sum2);
            }
        });
    }

    public synchronized void activate() {
        this.active = 1;
    }

    public synchronized void deactivate() {
        this.active = 0;
    }

    @Override
    public String toString() {
        return "ServerState{" +
                "ledger=" + ledger +
                "accounts=" + accounts +
                '}';
    }
}