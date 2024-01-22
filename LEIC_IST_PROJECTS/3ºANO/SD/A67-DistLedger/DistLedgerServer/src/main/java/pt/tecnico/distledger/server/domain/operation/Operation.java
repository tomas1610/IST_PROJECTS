package pt.tecnico.distledger.server.domain.operation;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;

import java.util.List;
import java.util.ArrayList;

public class Operation {

    private DistLedgerCommonDefinitions.OperationType type;
    private String account;
    private List<Integer> prevTS;
    private List<Integer> ts_operation;
    private int propagated;

    public Operation(String account) {
        this.account = account;
    }

    public DistLedgerCommonDefinitions.OperationType getType() { return type; }

    public void setType(DistLedgerCommonDefinitions.OperationType type) {
        this.type = type;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
    
    public List<Integer> getOperationTS(){
        return ts_operation;
    }

    public void setOperationTS(List<Integer> ts_operation){
        this.ts_operation = ts_operation;
    }

    public List<Integer> getOperationPrevTS(){
        return prevTS;
    }

    public void setOperationPrevTS(List<Integer> prevTS){
        this.prevTS = prevTS;
    }
} 