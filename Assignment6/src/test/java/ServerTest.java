import com.google.protobuf.Empty;
import example.grpcclient.Client;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Test;
import static org.junit.Assert.*;
import org.json.JSONObject;
import service.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ServerTest {

	ManagedChannel channel;
	private EchoGrpc.EchoBlockingStub blockingStub;
	private JokeGrpc.JokeBlockingStub blockingStub2;
	private FlowersGrpc.FlowersBlockingStub blockingStub3;
	private FollowGrpc.FollowBlockingStub blockingStub4;

	@org.junit.Before
	public void setUp() throws Exception {
		// assuming default port and localhost for our testing, make sure Node runs on
		// this port
		channel = ManagedChannelBuilder.forTarget("localhost:8000").usePlaintext().build();

		blockingStub = EchoGrpc.newBlockingStub(channel);
		blockingStub2 = JokeGrpc.newBlockingStub(channel);
		blockingStub3 = FlowersGrpc.newBlockingStub(channel);
		blockingStub4 = FollowGrpc.newBlockingStub(channel);
	}

	@org.junit.After
	public void close() throws Exception {
		channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);

	}

	@Test
	public void testPlantFlower() {
	    // Success case
	    FlowerReq request = FlowerReq.newBuilder().setName("Camellia").setWaterTimes(5).setBloomTime(3).build();
	    FlowerRes response = blockingStub3.plantFlower(request);
	    assertTrue(response.getIsSuccess());
	    assertEquals("Flower successfully planted!", response.getMessage());
	    assertTrue(response.getError().isEmpty());

	    // Error case: Flower name taken
	    response = blockingStub3.plantFlower(request);
	    assertFalse(response.getIsSuccess());
	    assertEquals("Flower name is already taken!", response.getError());

	    // Error case: Invalid water time > 6
	    FlowerReq invalidRequest1 = FlowerReq.newBuilder().setName("Hydrangea").setWaterTimes(7).setBloomTime(3).build();
	    response = blockingStub3.plantFlower(invalidRequest1);
	    assertFalse(response.getIsSuccess());
	    assertEquals("Water and bloom times MUST be 6 or less!", response.getError());

	    // Error case: Invalid bloom time > 6
	    FlowerReq invalidRequest2 = FlowerReq.newBuilder().setName("Buttercup").setWaterTimes(5).setBloomTime(7).build();
	    response = blockingStub3.plantFlower(invalidRequest2);
	    assertFalse(response.getIsSuccess());
	    assertEquals("Water and bloom times MUST be 6 or less!", response.getError());

	    // Boundary case: Exactly 6 water times and bloom time
	    FlowerReq boundaryRequest = FlowerReq.newBuilder().setName("Poppy").setWaterTimes(6).setBloomTime(6).build();
	    response = blockingStub3.plantFlower(boundaryRequest);
	    assertTrue(response.getIsSuccess());
	    assertEquals("Flower successfully planted!", response.getMessage());
	}

	@Test
	public void testViewFlowers() {
	    // Case: No flowers planted
	    Empty request = Empty.newBuilder().build();
	    FlowerViewRes response = blockingStub3.viewFlowers(request);
	    assertFalse(response.getIsSuccess());
	    assertEquals("No flowers planted.", response.getError());

	    // Case: Flowers planted
	    FlowerReq plantRequest = FlowerReq.newBuilder().setName("Daisy").setWaterTimes(5).setBloomTime(3).build();
	    blockingStub3.plantFlower(plantRequest);

	    // Now view flowers
	    response = blockingStub3.viewFlowers(request);
	    assertTrue(response.getIsSuccess());
	    assertEquals(1, response.getFlowersCount());

	    // Add another flower
	    FlowerReq plantRequest2 = FlowerReq.newBuilder().setName("Tulip").setWaterTimes(4).setBloomTime(2).build();
	    blockingStub3.plantFlower(plantRequest2);

	    // Now view flowers
	    response = blockingStub3.viewFlowers(request);
	    assertTrue(response.getIsSuccess());
	    assertEquals(2, response.getFlowersCount());  // Verify that two flowers are returned
	}

	@Test
	public void testWaterFlower() {
	    // Plant a flower first
	    FlowerReq plantRequest = FlowerReq.newBuilder().setName("Lily").setWaterTimes(2).setBloomTime(3).build();
	    blockingStub3.plantFlower(plantRequest);

	    // Success case: Water the flower (not blooming)
	    FlowerCare waterRequest = FlowerCare.newBuilder().setName("Lily").build();
	    WaterRes response = blockingStub3.waterFlower(waterRequest);
	    assertTrue(response.getIsSuccess());
	    assertEquals("Flower watered!", response.getMessage());

	    // Success case: Now blooming
	    response = blockingStub3.waterFlower(waterRequest);
	    assertTrue(response.getIsSuccess());
	    assertEquals("Flower is now blooming.", response.getMessage());

	    // Error case: Flower is already blooming
	    response = blockingStub3.waterFlower(waterRequest);
	    assertFalse(response.getIsSuccess());
	    assertEquals("Flower is already blooming!", response.getError());

	    // Error case: Flower not found
	    FlowerCare notFoundRequest = FlowerCare.newBuilder().setName("NonExistentFlower").build();
	    response = blockingStub3.waterFlower(notFoundRequest);
	    assertFalse(response.getIsSuccess());
	    assertEquals("Flower not found.", response.getError());
	}

	@Test
	public void testCareForFlower() {
	    // Plant a flower first
	    FlowerReq plantRequest = FlowerReq.newBuilder().setName("Sunflower").setWaterTimes(1).setBloomTime(1).build();
	    blockingStub3.plantFlower(plantRequest);

	    // Water the flower to make it bloom
	    FlowerCare waterRequest = FlowerCare.newBuilder().setName("Sunflower").build();
	    blockingStub3.waterFlower(waterRequest);

	    // Success case: Care for the flower (increases bloom time)
	    FlowerCare careRequest = FlowerCare.newBuilder().setName("Sunflower").build();
	    CareRes response = blockingStub3.careForFlower(careRequest);
	    assertTrue(response.getIsSuccess());
	    assertEquals("Flower cared for successfully!", response.getMessage());
	    assertEquals(2, response.getBloomTime());  // Bloom time should increase

	    // Error case: Flower does not bloom yet
	    FlowerReq plantRequest2 = FlowerReq.newBuilder().setName("Rose").setWaterTimes(1).setBloomTime(1).build();
	    blockingStub3.plantFlower(plantRequest2);

	    FlowerCare careRequest2 = FlowerCare.newBuilder().setName("Rose").build();
	    response = blockingStub3.careForFlower(careRequest2);
	    assertFalse(response.getIsSuccess());
	    assertEquals("Flower has not bloomed yet.", response.getError());

	    // Edge case: Care for flower multiple times (should keep increasing bloom time)
	    FlowerReq plantRequest3 = FlowerReq.newBuilder().setName("Orchid").setWaterTimes(1).setBloomTime(2).build();
	    blockingStub3.plantFlower(plantRequest3);
	    FlowerCare waterRequest3 = FlowerCare.newBuilder().setName("Orchid").build();
	    blockingStub3.waterFlower(waterRequest3);

	    CareRes response1 = blockingStub3.careForFlower(careRequest);
	    CareRes response2 = blockingStub3.careForFlower(careRequest);
	    assertEquals(4, response2.getBloomTime());  // Verify bloom time increases after multiple care requests
	}

	@Test
	public void testAddUser() {
	    // Success case
	    UserReq request = UserReq.newBuilder().setName("Alice").build();
	    UserRes response = blockingStub4.addUser(request);
	    assertTrue(response.getIsSuccess());
	    assertTrue(response.getError().isEmpty());

	    // Error case: User already exists
	    response = blockingStub4.addUser(request);
	    assertFalse(response.getIsSuccess());
	    assertEquals("The user already exists!", response.getError());

	    // Edge case: Empty user name
	    UserReq emptyUserRequest = UserReq.newBuilder().setName("").build();
	    response = blockingStub4.addUser(emptyUserRequest);
	    assertFalse(response.getIsSuccess());
	    assertEquals("The user name must not be blank!", response.getError());
	}

	@Test
	public void testFollowUser() {
	    // Add users first
	    UserReq addUserRequest1 = UserReq.newBuilder().setName("Alice").build();
	    blockingStub4.addUser(addUserRequest1);
	    UserReq addUserRequest2 = UserReq.newBuilder().setName("Bob").build();
	    blockingStub4.addUser(addUserRequest2);

	    // Success case: Follow an existing user
	    UserReq followRequest = UserReq.newBuilder()
	            .setName("Alice")
	            .setFollowName("Bob")
	            .build();
	    UserRes response = blockingStub4.follow(followRequest);
	    assertTrue(response.getIsSuccess());
	    assertTrue(response.getError().isEmpty());

	    // Error case: User trying to follow does not exist
	    UserReq followNonExistentRequest = UserReq.newBuilder()
	            .setName("Alice")
	            .setFollowName("Charlie")
	            .build();
	    response = blockingStub4.follow(followNonExistentRequest);
	    assertFalse(response.getIsSuccess());
	    assertEquals("The user you are trying to follow does not exist!", response.getError());

	    // Error case: User does not exist
	    UserReq followRequestInvalidUser = UserReq.newBuilder()
	            .setName("Charlie")
	            .setFollowName("Bob")
	            .build();
	    response = blockingStub4.follow(followRequestInvalidUser);
	    assertFalse(response.getIsSuccess());
	    assertEquals("The user name you have provided does not exist!", response.getError());

	    // Edge case: Empty follow name
	    UserReq emptyFollowRequest = UserReq.newBuilder()
	            .setName("Alice")
	            .setFollowName("")
	            .build();
	    response = blockingStub4.follow(emptyFollowRequest);
	    assertFalse(response.getIsSuccess());
	    assertEquals("The user name and/or follow name must not be blank!", response.getError());
	}

	@Test
	public void testViewFollowing() {
	    // Add user and follow another user first
	    UserReq addUserRequest1 = UserReq.newBuilder().setName("Alice").build();
	    blockingStub4.addUser(addUserRequest1);
	    UserReq addUserRequest2 = UserReq.newBuilder().setName("Bob").build();
	    blockingStub4.addUser(addUserRequest2);
	    UserReq addUserRequest3 = UserReq.newBuilder().setName("Charlie").build();
	    blockingStub4.addUser(addUserRequest3);
	    UserReq followRequest = UserReq.newBuilder()
	            .setName("Alice")
	            .setFollowName("Bob")
	            .build();
	    blockingStub4.follow(followRequest);
	    UserReq followRequest2 = UserReq.newBuilder()
	            .setName("Alice")
	            .setFollowName("Charlie")
	            .build();
	    blockingStub4.follow(followRequest2);

	    // Success case: View followed users
	    UserReq viewRequest = UserReq.newBuilder().setName("Alice").build();
	    UserRes response = blockingStub4.viewFollowing(viewRequest);
	    assertTrue(response.getIsSuccess());
	    assertEquals(2, response.getFollowedUserCount());
	    assertTrue(response.getFollowedUserList().contains("Bob"));
	    assertTrue(response.getFollowedUserList().contains("Charlie"));

	    // Error case: User does not exist
	    UserReq viewNonExistentUserRequest = UserReq.newBuilder().setName("David").build();
	    response = blockingStub4.viewFollowing(viewNonExistentUserRequest);
	    assertFalse(response.getIsSuccess());
	    assertEquals("The user name you have provided does not exist!", response.getError());

	    // Edge case: Empty user name in request
	    UserReq emptyViewRequest = UserReq.newBuilder().setName("").build();
	    response = blockingStub4.viewFollowing(emptyViewRequest);
	    assertFalse(response.getIsSuccess());
	    assertEquals("User name cannot be empty!", response.getError());
	}

	@Test
	public void parrot() {
		// success case
		ClientRequest request = ClientRequest.newBuilder().setMessage("test").build();
		ServerResponse response = blockingStub.parrot(request);
		assertTrue(response.getIsSuccess());
		assertEquals("test", response.getMessage());

		// error cases
		request = ClientRequest.newBuilder().build();
		response = blockingStub.parrot(request);
		assertFalse(response.getIsSuccess());
		assertEquals("No message provided", response.getError());

		request = ClientRequest.newBuilder().setMessage("").build();
		response = blockingStub.parrot(request);
		assertFalse(response.getIsSuccess());
		assertEquals("No message provided", response.getError());
	}

	// For this test the server needs to be started fresh AND the list of jokes
	// needs to be the initial list
	@Test
	public void joke() {
		// getting first joke
		JokeReq request = JokeReq.newBuilder().setNumber(1).build();
		JokeRes response = blockingStub2.getJoke(request);
		assertEquals(1, response.getJokeCount());
		assertEquals("Did you hear the rumor about butter? Well, I'm not going to spread it!", response.getJoke(0));

		// getting next 2 jokes
		request = JokeReq.newBuilder().setNumber(2).build();
		response = blockingStub2.getJoke(request);
		assertEquals(2, response.getJokeCount());
		assertEquals("What do you call someone with no body and no nose? Nobody knows.", response.getJoke(0));
		assertEquals("I don't trust stairs. They're always up to something.", response.getJoke(1));

		// getting 2 more but only one more on server
		request = JokeReq.newBuilder().setNumber(2).build();
		response = blockingStub2.getJoke(request);
		assertEquals(2, response.getJokeCount());
		assertEquals("How do you get a squirrel to like you? Act like a nut.", response.getJoke(0));
		assertEquals("I am out of jokes...", response.getJoke(1));

		// trying to get more jokes but out of jokes
		request = JokeReq.newBuilder().setNumber(2).build();
		response = blockingStub2.getJoke(request);
		assertEquals(1, response.getJokeCount());
		assertEquals("I am out of jokes...", response.getJoke(0));

		// trying to add joke without joke field
		JokeSetReq req2 = JokeSetReq.newBuilder().build();
		JokeSetRes res2 = blockingStub2.setJoke(req2);
		assertFalse(res2.getOk());

		// trying to add empty joke
		req2 = JokeSetReq.newBuilder().setJoke("").build();
		res2 = blockingStub2.setJoke(req2);
		assertFalse(res2.getOk());

		// adding a new joke (well word)
		req2 = JokeSetReq.newBuilder().setJoke("whoop").build();
		res2 = blockingStub2.setJoke(req2);
		assertTrue(res2.getOk());

		// should have the new "joke" now and return it
		request = JokeReq.newBuilder().setNumber(1).build();
		response = blockingStub2.getJoke(request);
		assertEquals(1, response.getJokeCount());
		assertEquals("whoop", response.getJoke(0));
	}

}