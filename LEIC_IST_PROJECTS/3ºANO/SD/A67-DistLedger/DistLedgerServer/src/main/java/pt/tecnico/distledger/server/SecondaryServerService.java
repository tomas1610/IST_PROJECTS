package pt.tecnico.distledger.server;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*; 
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;

import io.grpc.stub.StreamObserver;
import java.util.*;
import java.util.stream.Collectors;

public class SecondaryServerService extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {

    private ServerState serverState;
    private DistLedgerCrossServerService serverService;

    public SecondaryServerService(ServerState serverState, DistLedgerCrossServerService serverService) {
        this.serverState = serverState;
        this.serverService = serverService;
    }

    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {

        PropagateStateResponse response;


        if (this.serverState.getActive() != 1) {
            response = PropagateStateResponse.newBuilder().setError(ErrorPropagate.UNAVAILABLE).build();
        }
        else {

            this.serverState.setMapTS(this.serverService.updateTS(this.serverState.getMapTS()));
        
            for (DistLedgerCommonDefinitions.Operation operation_proto : request.getLedgerState().getLedgerList()){

                if (this.serverState.containOperation(operation_proto.getPrevTSList()))
                    continue;

                this.serverState.incrementTSGossip(operation_proto.getTSList());
                if (operation_proto.getType() == OperationType.OP_CREATE_ACCOUNT){  
                    this.serverState.createAccount(operation_proto.getAccount(),0);
                    this.serverState.getLedger().get(this.serverState.getLedger().size() - 1).setOperationPrevTS(operation_proto.getPrevTSList());
                    this.serverState.getLedger().get(this.serverState.getLedger().size() - 1).setOperationTS(operation_proto.getTSList());
                }
                else if(operation_proto.getType() == OperationType.OP_TRANSFER_TO){
                    this.serverState.transferTo(operation_proto.getAccount(), operation_proto.getDestAccount(), operation_proto.getAmount(),0);
                    this.serverState.getLedger().get(this.serverState.getLedger().size() - 1).setOperationPrevTS(operation_proto.getPrevTSList());
                    this.serverState.getLedger().get(this.serverState.getLedger().size() - 1).setOperationTS(operation_proto.getTSList());
                }
                Operation last_operation = this.serverState.getLedger().get(this.serverState.getLedger().size() - 1);
                if (this.serverState.compareTS(last_operation.getOperationPrevTS()) == 1){
                    this.serverState.execute_operation(last_operation);
                }
                this.serverState.incrementValueTS(last_operation.getOperationTS());
            }
            
            this.serverState.sortLedger();
            this.serverState.execute_all_unstable();
            response = PropagateStateResponse.newBuilder().setError(ErrorPropagate.OK).build();
        } 
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void checkState(StateRequest request, StreamObserver<StateResponse> responseObserver) {

        StateResponse response;
        if (this.serverState.getActive() == 1)
            response = StateResponse.newBuilder().setError(ErrorPropagate.OK).build();
        else
            response = StateResponse.newBuilder().setError(ErrorPropagate.UNAVAILABLE).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public int compareTS(List<Integer> clientTS, List<Integer> replicaTS){

        for (int i = 0; i < clientTS.size(); i++){
            if (clientTS.get(i) > replicaTS.get(i))
                return 0;
        }

        return 1;
    }
}