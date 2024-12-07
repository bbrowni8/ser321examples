package example.grpcclient;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.HashSet;
import java.util.Set;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import service.*;

import buffers.RequestProtos.Request;
import buffers.RequestProtos.Request.RequestType;
import buffers.ResponseProtos.Response;

public class FollowImpl extends FollowGrpc.FollowImplBase {
	private final Set<String> users = new HashSet<>();
    private final Set<String> userFollowings = new HashSet<>(); 
	
    @Override
	public void addUser(UserReq request, StreamObserver<UserRes> responseObserver) {
		String userName = request.getName();
        UserRes.Builder responseBuilder = UserRes.newBuilder();

        if (userName.isEmpty()) {
        	responseBuilder.setIsSuccess(false)
            .setError("The user name must not be blank!");
        } else if (users.contains(userName)) {
            responseBuilder.setIsSuccess(false)
                    .setError("The user already exists!");
        } else {
            users.add(userName);
            responseBuilder.setIsSuccess(true);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
	}
	
    @Override
	public void follow(UserReq request, StreamObserver<UserRes> responseObserver) {
    	String userName = request.getName();
        String followName = request.getFollowName();
        UserRes.Builder responseBuilder = UserRes.newBuilder();

        if (userName.isEmpty() || followName.isEmpty()) {
        	responseBuilder.setIsSuccess(false)
            .setError("The user name and/or follow name must not be blank!");
        } else if (!users.contains(userName)) {
            responseBuilder.setIsSuccess(false)
                    .setError("The user name you have provided does not exist!");
        } else if (!users.contains(followName)) {
            responseBuilder.setIsSuccess(false)
                    .setError("The user you are trying to follow does not exist!");
        } else {
            userFollowings.add(followName);
            responseBuilder.setIsSuccess(true);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
	}
	
    @Override
	public void viewFollowing(UserReq request, StreamObserver<UserRes> responseObserver) {
    	String userName = request.getName();
        UserRes.Builder responseBuilder = UserRes.newBuilder();

        if (userName.isEmpty()) {
        	responseBuilder.setIsSuccess(false)
            .setError("User name cannot be empty!");
        } else if (!users.contains(userName)) {
            responseBuilder.setIsSuccess(false)
                    .setError("The user name you have provided does not exist!");
        } else {
            responseBuilder.setIsSuccess(true);
            for (String followedUser : userFollowings) {
                responseBuilder.addFollowedUser(followedUser);
            }
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
	}
}
