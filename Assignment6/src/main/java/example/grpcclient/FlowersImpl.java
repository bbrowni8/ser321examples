package example.grpcclient;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import service.*;
import com.google.protobuf.Empty;

import buffers.RequestProtos.Request;
import buffers.RequestProtos.Request.RequestType;
import buffers.ResponseProtos.Response;

public class FlowersImpl extends FlowersGrpc.FlowersImplBase {
	private final List<Flower> flowers = new ArrayList<>();
	private final long currentTime = System.currentTimeMillis();  
	private final Map<String, Long> flowerBloomStartTimes = new HashMap<>();
	
	@Override
	public void plantFlower(FlowerReq request, StreamObserver<FlowerRes> responseObserver) {
		for (Flower flower : flowers) {
			if (flower.getName().equals(request.getName())) {
				FlowerRes response = FlowerRes.newBuilder()
						.setIsSuccess(false)
						.setMessage("")
						.setError("Flower name is already taken!")
						.build();
				responseObserver.onNext(response);
				responseObserver.onCompleted();
				return;
			}
		}
		
		if (request.getWaterTimes() > 6 || request.getBloomTime() > 6) {
			FlowerRes response = FlowerRes.newBuilder()
					.setIsSuccess(false)
					.setMessage("")
					.setError("Water and bloom times MUST be 6 or less!")
					.build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
			return;
		}
		
		
		Flower newFlower = Flower.newBuilder()
				.setName(request.getName())
				.setWaterTimes(request.getWaterTimes())
				.setBloomTime(request.getBloomTime())
				.setFlowerState(State.PLANTED)
				.build();
		flowers.add(newFlower);
		
		FlowerRes response = FlowerRes.newBuilder()
				.setIsSuccess(true)
				.setMessage("Flower successfully planted!")
				.setError("")
				.build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
		return;
	}
	
	@Override
	public void viewFlowers(Empty request, StreamObserver<FlowerViewRes> responseObserver) {
	    if (flowers.isEmpty()) {
	        FlowerViewRes response = FlowerViewRes.newBuilder()
	                .setIsSuccess(false)
	                .addAllFlowers(new ArrayList<>())
	                .setError("No flowers planted.")
	                .build();
	        responseObserver.onNext(response);
	        responseObserver.onCompleted();
	        return;
	    }

	    long currentTime = System.currentTimeMillis();
	    List<Flower> updatedFlowers = new ArrayList<>();

	    for (Flower flower : flowers) {
	        if (flower.getFlowerState() == State.BLOOMING) {
	            Long bloomStartTime = flowerBloomStartTimes.get(flower.getName());
	            if (bloomStartTime != null) {
	                long bloomEndTime = bloomStartTime + (flower.getBloomTime() * 3600000);  // Calculate the bloom end time in milliseconds
	                if (currentTime >= bloomEndTime) {
	                    flower = flower.toBuilder().setFlowerState(State.DEAD).build();  // Mark the flower as dead after blooming duration
	                    flowerBloomStartTimes.remove(flower.getName());  // Remove it from the map as it is no longer blooming
	                }
	            }
	        }
	        updatedFlowers.add(flower);
	    }

	    FlowerViewRes response = FlowerViewRes.newBuilder()
	            .setIsSuccess(true)
	            .addAllFlowers(updatedFlowers)
	            .setError("")
	            .build();
	    responseObserver.onNext(response);
	    responseObserver.onCompleted();
	}


	@Override
	public void waterFlower(FlowerCare request, StreamObserver<WaterRes> responseObserver) {
	    for (int i = 0; i < flowers.size(); i++) {
	        Flower flower = flowers.get(i);
	        if (flower.getName().equals(request.getName())) {
	            if (flower.getFlowerState() == State.DEAD) {
	                WaterRes response = WaterRes.newBuilder()
	                        .setIsSuccess(false)
	                        .setMessage("")
	                        .setError("Flower is dead :(")
	                        .build();
	                responseObserver.onNext(response);
	                responseObserver.onCompleted();
	                return;
	            } else if (flower.getFlowerState() == State.BLOOMING) {
	                WaterRes response = WaterRes.newBuilder()
	                        .setIsSuccess(false)
	                        .setMessage("")
	                        .setError("Flower is already blooming!")
	                        .build();
	                responseObserver.onNext(response);
	                responseObserver.onCompleted();
	                return;
	            }

	            // Decrease waterTimes
	            flower = flower.toBuilder().setWaterTimes(flower.getWaterTimes() - 1).build();

	            if (flower.getWaterTimes() <= 0) {
	                long bloomEndTime = currentTime + (flower.getBloomTime() * 3600000);  // Calculate bloom duration in ms
	                flower = flower.toBuilder().setFlowerState(State.BLOOMING).build();
	                
	                // Store the blooming start time in the map
	                flowerBloomStartTimes.put(flower.getName(), currentTime);

	                WaterRes response = WaterRes.newBuilder()
	                        .setIsSuccess(true)
	                        .setMessage("Flower is now blooming.")
	                        .setError("")
	                        .build();
	                responseObserver.onNext(response);
	                responseObserver.onCompleted();

	                flowers.set(i, flower);  // Update the flower in the list
	                return;
	            }

	            WaterRes response = WaterRes.newBuilder()
	                    .setIsSuccess(true)
	                    .setMessage("Flower watered!")
	                    .setError("")
	                    .build();
	            responseObserver.onNext(response);
	            responseObserver.onCompleted();

	            flowers.set(i, flower);  // Update the flower in the list
	            return;
	        }
	    }

	    WaterRes response = WaterRes.newBuilder()
	            .setIsSuccess(false)
	            .setMessage("")
	            .setError("Flower not found.")
	            .build();
	    responseObserver.onNext(response);
	    responseObserver.onCompleted();
	}

    @Override
    public void careForFlower(FlowerCare request, StreamObserver<CareRes> responseObserver) {
    	for (int i = 0; i < flowers.size(); i++) {
            Flower flower = flowers.get(i);
            
            // Check if the flower exists and if it's blooming
            if (flower.getName().equals(request.getName())) {
                if (flower.getFlowerState() != State.BLOOMING) {
                    // If the flower is not blooming, return an error
                    CareRes response = CareRes.newBuilder()
                            .setIsSuccess(false)
                            .setMessage("")
                            .setError("Flower has not bloomed yet.")
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                    return;
                }

                // Increment the bloom time (e.g., flower gets additional hours of blooming)
                flower = flower.toBuilder()
                        .setBloomTime(flower.getBloomTime() + 1)  // Add one hour to bloom time
                        .build();

                // Send response indicating the flower was cared for successfully
                CareRes response = CareRes.newBuilder()
                        .setIsSuccess(true)
                        .setMessage("Flower cared for successfully!")
                        .setBloomTime(flower.getBloomTime())  // Updated bloom time
                        .setError("")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();

                // Update the flower in the list
                flowers.set(i, flower);
                return;
            }
        }

        // If the flower was not found
        CareRes response = CareRes.newBuilder()
                .setIsSuccess(false)
                .setMessage("")
                .setError("Flower not found.")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
