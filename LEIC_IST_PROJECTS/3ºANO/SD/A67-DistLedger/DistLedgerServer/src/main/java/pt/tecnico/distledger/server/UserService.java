package pt.tecnico.distledger.server;

import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.operation.*;

import io.grpc.stub.StreamObserver;
import java.util.*;

/* MISSING : VERIFY POSSIBLE EXCEPTIONS , AND THROW THEM */

public class UserService extends UserServiceGrpc.UserServiceImplBase {

    private ServerState state;
    private DistLedgerCrossServerService serverService;
    private String qualificator;

    public UserService(ServerState state, DistLedgerCrossServerService serverService,String qualificator) {
        this.state = state;
        this.serverService = serverService;
        this.qualificator = qualificator;
        this.state.setMapTS(this.serverService.updateTS(this.state.getMapTS()));
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
        CreateAccountResponse response;
        if (this.state.getActive() != 1) {
            response = CreateAccountResponse.newBuilder().setError(ErrorMsg.ERROR_PRIMARY_SERVER_UNAVAILABLE).build();
        }
        else {                      
            this.state.setMapTS(this.serverService.updateTS(this.state.getMapTS()));
            if (this.state.compareTS(request.getPrevTSList()) == 1) {
                if (this.state.createAccount(request.getUserId(), 1) >= 0) {
                    this.state.incrementTS(this.qualificator);
                    this.state.incrementValueTSReplic();
                    this.state.getLedger().get(this.state.getLedger().size() - 1).setOperationPrevTS(request.getPrevTSList());
                    this.state.getLedger().get(this.state.getLedger().size() - 1).setOperationTS(this.state.createListTS());
                    response = CreateAccountResponse.newBuilder().setError(ErrorMsg.OK).addAllTS(this.state.createListTS()).build();        
                }       
                else
                    response = CreateAccountResponse.newBuilder().setError(ErrorMsg.ERROR_CREATE).addAllTS(request.getPrevTSList()).build();
            }
            else {
                this.state.createAccount(request.getUserId(), 0);
                this.state.incrementTS(this.qualificator);
                this.state.getLedger().get(this.state.getLedger().size() - 1).setOperationPrevTS(request.getPrevTSList());
                this.state.getLedger().get(this.state.getLedger().size() - 1).setOperationTS(this.state.createListTS());
                response = CreateAccountResponse.newBuilder().setError(ErrorMsg.OK).addAllTS(this.state.createListTS()).build();
            }
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    @Override
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {

        BalanceResponse response;

        if(this.state.getActive() != 1) {
            response = BalanceResponse.newBuilder().setError(ErrorMsg.ERROR_UNAVAILABLE).build();
        }
        else {
            this.state.setMapTS(this.serverService.updateTS(this.state.getMapTS()));
            while(this.state.compareTS(request.getPrevTSList()) != 1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            int balance = this.state.balance(request.getUserId());
            if (balance >= 0) 
                response = BalanceResponse.newBuilder().setValue(balance).setError(ErrorMsg.OK).addAllValueTS(this.state.getValueTS()).build();
            else 
                response = BalanceResponse.newBuilder().setValue(0).setError(ErrorMsg.ERROR_BALANCE).build();   
            
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {

        TransferToResponse response;

        if (this.state.getActive() != 1) {
            response = TransferToResponse.newBuilder().setError(ErrorMsg.ERROR_UNAVAILABLE).build();
        }
        else {
            this.state.setMapTS(this.serverService.updateTS(this.state.getMapTS()));
            if (this.state.compareTS(request.getPrevTSList()) == 1) {
                int check = this.state.transferTo(request.getAccountFrom(),request.getAccountTo(),request.getAmount(),1);
                if (check == -1)
                    response = TransferToResponse.newBuilder().setError(ErrorMsg.ERROR_TRANSFER_ACCOUNT_FROM).addAllTS(this.state.createListTS()).build();
                else if (check == -2)
                    response = TransferToResponse.newBuilder().setError(ErrorMsg.ERROR_TRANSFER_ACCOUNT_DEST).addAllTS(this.state.createListTS()).build();
                else if (check == -3)
                    response = TransferToResponse.newBuilder().setError(ErrorMsg.ERROR_TRANSFER_AMOUNT).addAllTS(this.state.createListTS()).build();
                else {
                    this.state.incrementTS(this.qualificator);
                    this.state.incrementValueTSReplic();
                    this.state.getLedger().get(this.state.getLedger().size() - 1).setOperationPrevTS(request.getPrevTSList());
                    this.state.getLedger().get(this.state.getLedger().size() - 1).setOperationTS(this.state.createListTS());
                    response = TransferToResponse.newBuilder().setError(ErrorMsg.OK).addAllTS(this.state.createListTS()).build();
                }
            }
            else {
                this.state.transferTo(request.getAccountFrom(),request.getAccountTo(),request.getAmount(),0);
                this.state.incrementTS(this.qualificator);
                this.state.getLedger().get(this.state.getLedger().size() - 1).setOperationPrevTS(request.getPrevTSList());
                this.state.getLedger().get(this.state.getLedger().size() - 1).setOperationTS(this.state.createListTS());
                response = TransferToResponse.newBuilder().setError(ErrorMsg.OK).addAllTS(this.state.createListTS()).build();
            }
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}