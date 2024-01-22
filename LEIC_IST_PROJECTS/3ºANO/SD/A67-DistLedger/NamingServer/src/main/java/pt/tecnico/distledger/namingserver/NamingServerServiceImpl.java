package pt.tecnico.distledger.namingserver;

import io.grpc.*;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.tecnico.distledger.namingserver.exceptions.RegisterException;
import pt.tecnico.distledger.namingserver.exceptions.DeleteException;
import io.grpc.stub.StreamObserver;

public class NamingServerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase {

    private NamingServer namingServer;

    public NamingServerServiceImpl(NamingServer namingServer) {
        this.namingServer = namingServer;
    }

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        try {
            this.namingServer.register(request.getServiceName(), request.getQualificator(), request.getTarget());
            RegisterResponse response = RegisterResponse.newBuilder().build(); 

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (RegisterException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void lookup(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {
        
        LookupResponse.Builder responseBuilder = LookupResponse.newBuilder();

        for (ServerEntry entry : this.namingServer.lookup(request.getServiceName(), request.getQualificator())){
            ServerInfo entryMsg = ServerInfo.newBuilder().setTarget(entry.getTarget()).setQualificator(entry.getQualificator()).build();
            responseBuilder.addServerEntry(entryMsg);
        }
        LookupResponse response = responseBuilder.build();

        //LookupResponse response = LookupResponse.newBuilder().addAllServerEntry(this.namingServer.lookup(request.getServiceName(), request.getQualificator())).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        try {
            this.namingServer.delete(request.getServiceName(), request.getTarget());
            DeleteResponse response = DeleteResponse.newBuilder().build(); 
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (DeleteException e){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
