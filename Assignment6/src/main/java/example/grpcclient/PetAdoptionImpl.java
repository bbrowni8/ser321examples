package example.grpcclient;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import service.*;
import com.google.protobuf.Empty;

import buffers.RequestProtos.Request;
import buffers.RequestProtos.Request.RequestType;
import buffers.ResponseProtos.Response;

public class PetAdoptionImpl extends PetAdoptionGrpc.PetAdoptionImplBase {
	private List<Pet> pets = new ArrayList<>();
	private static int petIdCounter = 0;
	
	@Override
	public void addPet(AddPetReq request, StreamObserver<AddPetRes> responseObserver) {
		AddPetRes.Builder responseBuilder = AddPetRes.newBuilder();

        // Validate name
        if (request.getName() == null || request.getName().isEmpty()) {
            responseBuilder.setIsSuccess(false)
                    .setMessage("Pet name cannot be empty.");
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            return;
        }

        // Validate pet type
        if (request.getType() == PetType.UNKNOWN) {
            responseBuilder.setIsSuccess(false)
                    .setMessage("Pet type cannot be UNKNOWN.");
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            return;
        }

        // Validate age
        if (request.getAge() < 0) {
            responseBuilder.setIsSuccess(false)
                    .setMessage("Pet age cannot be negative.");
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            return;
        }

        int newPetID = ++petIdCounter;
        Pet newPet = Pet.newBuilder()
                .setId(newPetID)
                .setName(request.getName())
                .setType(request.getType())
                .setAge(request.getAge())
                .setDescription(request.getDescription())
                .setIsAdopted(false)
                .build();

        pets.add(newPet);

        responseBuilder.setIsSuccess(true)
                .setMessage("Pet added successfully!");

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
	}
	
	@Override
    public void listPets(Empty request, StreamObserver<PetListRes> responseObserver) {
		PetListRes.Builder responseBuilder = PetListRes.newBuilder();
        List<Pet> availablePets = new ArrayList<>();

        for (Pet pet : pets) {
            if (!pet.getIsAdopted()) { // Only list pets that are not adopted
                availablePets.add(pet);
            }
        }

        if (availablePets.isEmpty()) {
            responseBuilder.setIsSuccess(false)
                    .setMessage("No pets are currently up for adoption.")
                    .addAllPets(new ArrayList<>());
        } else {
            responseBuilder.setIsSuccess(true)
                    .setMessage("")
                    .addAllPets(availablePets);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void requestAdoption(AdoptionReq request, StreamObserver<AdoptionRes> responseObserver) {
    	boolean isAdoptionSuccessful = false;
        String message = "Adoption request failed";

        // Find the pet by ID
        for (int i = 0; i < pets.size(); i++) {
            Pet pet = pets.get(i);
            if (pet.getId() == request.getPetId() && !pet.getIsAdopted()) {
                Pet adoptedPet = pet.toBuilder().setIsAdopted(true).build(); // Mark the pet as adopted
                pets.set(i, adoptedPet); // Update the pet in the list
                isAdoptionSuccessful = true;
                message = "Adoption request successful for " + adoptedPet.getName();
                break;
            }
        }

        AdoptionRes response = AdoptionRes.newBuilder()
                .setIsSuccess(isAdoptionSuccessful)
                .setMessage(message)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void searchPets(PetSearchReq request, StreamObserver<PetSearchRes> responseObserver) {
    	PetSearchRes.Builder responseBuilder = PetSearchRes.newBuilder();

        // Initialize success flag and message
        boolean isSuccess = true;
        String message = "";

        // Validate request parameters
        if (request.getType() == null || request.getType().isEmpty()) {
            isSuccess = false;
            message = "Pet type cannot be empty.";
        } else if (request.getMaxAge() < 0) {
            isSuccess = false;
            message = "Max age cannot be negative.";
        } else {
            for (Pet pet : pets) {
                boolean matchesType = pet.getType() == PetType.valueOf(request.getType().toUpperCase());
                boolean matchesAge = pet.getAge() <= request.getMaxAge();

                if (!pet.getIsAdopted() && matchesType && matchesAge) {
                    responseBuilder.addPets(pet);
                }
            }
        }

        responseBuilder.setIsSuccess(isSuccess)
                       .setMessage(message);

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
