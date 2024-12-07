package example.grpcclient;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import service.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.google.protobuf.Empty; // needed to use Empty

public class Client2 {
	private EchoGrpc.EchoBlockingStub blockingStub;
	private JokeGrpc.JokeBlockingStub blockingStub2;
	private RegistryGrpc.RegistryBlockingStub blockingStub3;
	private RegistryGrpc.RegistryBlockingStub blockingStub4;
	private FlowersGrpc.FlowersBlockingStub blockingStub5;
	private FollowGrpc.FollowBlockingStub blockingStub6;
	private PetAdoptionGrpc.PetAdoptionBlockingStub blockingStub7;

	public Client2(Channel channel, Channel regChannel) {
		blockingStub = EchoGrpc.newBlockingStub(channel);
		blockingStub2 = JokeGrpc.newBlockingStub(channel);
		blockingStub3 = RegistryGrpc.newBlockingStub(regChannel);
		blockingStub4 = RegistryGrpc.newBlockingStub(channel);
		blockingStub5 = FlowersGrpc.newBlockingStub(channel);
		blockingStub6 = FollowGrpc.newBlockingStub(channel);
		blockingStub7 = PetAdoptionGrpc.newBlockingStub(channel);
	}
	
	public void updateStubForEcho(String serverAddress) {
		ManagedChannel serverChannel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
		blockingStub = EchoGrpc.newBlockingStub(serverChannel);
	}
	
	public void updateStubForJoke(String serverAddress) {
		ManagedChannel serverChannel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
		blockingStub2 = JokeGrpc.newBlockingStub(serverChannel);
	}
	
	public void updateStubForFlowers(String serverAddress) {
		ManagedChannel serverChannel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
		blockingStub5 = FlowersGrpc.newBlockingStub(serverChannel);
	}
	
	public void updateStubForFollow(String serverAddress) {
		ManagedChannel serverChannel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
		blockingStub6 = FollowGrpc.newBlockingStub(serverChannel);
	}
	
	public void updateStubForPetAdoption(String serverAddress) {
		ManagedChannel serverChannel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
		blockingStub7 = PetAdoptionGrpc.newBlockingStub(serverChannel);
	}

	public void getNodeServices() {
		GetServicesReq request = GetServicesReq.newBuilder().build();
		ServicesListRes response;
		try {
			response = blockingStub4.getServices(request);
			System.out.println(response.toString());
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
			return;
		}
	}

	public void getServices() {
		GetServicesReq request = GetServicesReq.newBuilder().build();
		ServicesListRes response;
		try {
			response = blockingStub3.getServices(request);
			System.out.println(response.toString());
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
			return;
		}
	}

	public String findServer(String serviceName) {
		FindServerReq request = FindServerReq.newBuilder().setServiceName(serviceName).build();
		SingleServerRes response;
		try {
			response = blockingStub3.findServer(request);

			if (response.getIsSuccess()) {
				Connection connection = response.getConnection();
				String serverAddress = connection.getUri() + ":" + connection.getPort();
				System.out.println("Server found: " + serverAddress);
				return serverAddress;
			} else {
				System.out.println("Error finding server: " + response.getError());
				return null;
			}
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
			return null; // Return null if no server is found
		}
	}

	public void findServers(String name) {
		FindServersReq request = FindServersReq.newBuilder().setServiceName(name).build();
		ServerListRes response;
		try {
			response = blockingStub3.findServers(request);
			System.out.println(response.toString());
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
			return;
		}
	}

	public static void handleEchoRequest(Client2 client) {
		Scanner scanner = new Scanner(System.in);
		String serviceName = "services.Echo/parrot";

		while (true) {
			System.out.print("Enter a message to echo: ");
			try {
				String message = scanner.nextLine();
				System.out.println("Looking for a server for service: " + serviceName);

				String serverAddress = client.findServer(serviceName);

				if (serverAddress == null || serverAddress.isEmpty()) {
					System.out.println("No server found for service: " + serviceName);
					break;
				}

				client.updateStubForEcho(serverAddress);
				client.askServerToParrot(message);
				break;
			} catch (Exception e) {
				System.out.println("Something went wrong... Try again later.");
				break;
			}
		}
	}

	public void askServerToParrot(String message) {
		ClientRequest request = ClientRequest.newBuilder().setMessage(message).build();
		ServerResponse response;
		try {
			response = blockingStub.parrot(request);
		} catch (Exception e) {
			System.err.println("RPC failed: " + e.getMessage());
			return;
		}
		System.out.println("Received from server: " + response.getMessage());
	}

	public static void handleJokeRequest(Client2 client) {
		Scanner scanner = new Scanner(System.in);
		String serviceName;
		String serverAddress;
		while (true) {
			System.out.print("Would you like to: \n0) Exit \n1) Add a joke \n2) View a joke \n");
			try {
				int num = scanner.nextInt();
				scanner.nextLine();

				switch (num) {
				case 0:
					break;
				case 1:
					serviceName = "services.Joke/setJoke";
					System.out.println("Looking for a server for service: " + serviceName);

					serverAddress = client.findServer(serviceName);

					if (serverAddress == null || serverAddress.isEmpty()) {
						System.out.println("No server found for service: " + serviceName);
						break;
					}

					client.updateStubForJoke(serverAddress);

					System.out.println("Please enter the joke you wish to add: ");
					String joke = scanner.nextLine();

					client.setJoke(joke);
					break;
				case 2:
					serviceName = "services.Joke/getJoke";
					System.out.println("Looking for a server for service: " + serviceName);

					serverAddress = client.findServer(serviceName);

					if (serverAddress == null || serverAddress.isEmpty()) {
						System.out.println("No server found for service: " + serviceName);
						break;
					}

					client.updateStubForJoke(serverAddress);

					System.out.println("How many jokes would you like to view?");
					int jokes = scanner.nextInt();
					scanner.nextLine();
					if (jokes < 1) {
						System.out.println("The number of jokes must be greater than 0!");
					} else {
						client.askForJokes(jokes);
					}
					break;
				default:
					System.out.println("Not a valid choice! Please choose one of the available options!");
					continue;
				}
				break;
			} catch (InputMismatchException ime) {
				System.out.println("Not a valid integer. Please try again!");
				scanner.nextLine(); // Clear the invalid input
				continue;
			} catch (Exception e) {
				System.out.println("Something went wrong... Try again later.");
				break;
			}
		}
	}

	public void askForJokes(int num) {
		JokeReq request = JokeReq.newBuilder().setNumber(num).build();
		JokeRes response;

		try {
			response = blockingStub2.getJoke(request);
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
			return;
		}
		System.out.println("Your jokes: ");
		for (String joke : response.getJokeList()) {
			System.out.println("--- " + joke);
		}
	}

	public void setJoke(String joke) {
		JokeSetReq request = JokeSetReq.newBuilder().setJoke(joke).build();
		JokeSetRes response;

		try {
			response = blockingStub2.setJoke(request);
			if (response.getOk()) {
				System.out.println("Joke successfully added!");
			} else {
				System.out.println("Failed to add joke!");
			}
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
			return;
		}
	}

	public static void handleFlowersRequest(Client2 client) {
		Scanner scanner = new Scanner(System.in);
		String serviceName;
		String serverAddress;
		while (true) {
			System.out.print(
					"Would you like to: \n0) Exit \n1) Plant a flower \n2) View all flowers \n3) Water a flower \n4) Care for a flower \n");
			try {
				int num = scanner.nextInt();
				scanner.nextLine();

				switch (num) {
				case 0:
					return;
				case 1:
					serviceName = "services.Flowers/plantFlower";
					System.out.println("Looking for a server for service: " + serviceName);

					serverAddress = client.findServer(serviceName);

					if (serverAddress == null || serverAddress.isEmpty()) {
						System.out.println("No server found for service: " + serviceName);
						break;
					}

					client.updateStubForFlowers(serverAddress);

					System.out.println("Please provide the flower's name: ");
					String name = scanner.nextLine();
					if (name.isEmpty()) {
						System.out.println("Name cannot be empty!");
						continue;
					}

					System.out.println(
							"Please provide the amount of times the flower must be watered before it blooms. The limit is 6: ");
					int waterTime = scanner.nextInt();
					if (waterTime < 1 || waterTime > 6) {
						System.out.println("Water time must be between 1 and 6!");
						scanner.nextLine();
						continue;
					}

					System.out.println("Please provide how long the flower blooms before it dies. The limit is 6: ");
					int bloomTime = scanner.nextInt();
					scanner.nextLine();
					if (bloomTime < 1 || bloomTime > 6) {
						System.out.println("Bloom time must be between 1 and 6!");
						continue;
					}

					client.plantFlower(name, waterTime, bloomTime);
					break;
				case 2:
					serviceName = "services.Flowers/careForFlower";
					System.out.println("Looking for a server for service: " + serviceName);

					serverAddress = client.findServer(serviceName);

					if (serverAddress == null || serverAddress.isEmpty()) {
						System.out.println("No server found for service: " + serviceName);
						break;
					}

					client.updateStubForFlowers(serverAddress);

					client.viewFlowers();
					break;
				case 3:
					serviceName = "services.Flowers/viewFlowers";
					System.out.println("Looking for a server for service: " + serviceName);

					serverAddress = client.findServer(serviceName);

					if (serverAddress == null || serverAddress.isEmpty()) {
						System.out.println("No server found for service: " + serviceName);
						break;
					}

					client.updateStubForFlowers(serverAddress);

					System.out.println("Please provide the flower's name: ");
					String name1 = scanner.nextLine();
					if (name1.isEmpty()) {
						System.out.println("Name cannot be empty!");
						continue;
					}

					client.waterFlower(name1);
					break;
				case 4:
					serviceName = "services.Flowers/waterFlower";
					System.out.println("Looking for a server for service: " + serviceName);

					serverAddress = client.findServer(serviceName);

					if (serverAddress == null || serverAddress.isEmpty()) {
						System.out.println("No server found for service: " + serviceName);
						break;
					}

					client.updateStubForFlowers(serverAddress);

					System.out.println("Please provide the flower's name: ");
					String name2 = scanner.nextLine();
					if (name2.isEmpty()) {
						System.out.println("Name cannot be empty!");
						continue;
					}

					client.careForFlower(name2);
					break;
				default:
					System.out.println("Not a valid choice! Please choose one of the available options!");
					continue;
				}
				break;
			} catch (InputMismatchException ime) {
				System.out.println("Not a valid integer. Please try again!");
				scanner.nextLine(); // Clear the invalid input
				continue;
			} catch (Exception e) {
				System.out.println("Something went wrong... Try again later.");
				break;
			}
		}
	}

	public void plantFlower(String name, int waterTime, int bloomTime) {
		FlowerReq request = FlowerReq.newBuilder().setName(name).setWaterTimes(waterTime).setBloomTime(bloomTime)
				.build();
		FlowerRes response;

		try {
			response = blockingStub5.plantFlower(request);
			System.out.println(response.getIsSuccess() ? response.getMessage() : response.getError());
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
			return;
		}
	}

	public void viewFlowers() {
		Empty request = Empty.newBuilder().build();
		FlowerViewRes response;

		try {
			response = blockingStub5.viewFlowers(request);
			if (response.getIsSuccess()) {
				System.out.println("All Flowers: ");
				for (Flower flower : response.getFlowersList()) {
					System.out.println("--- " + flower.getName() + " (Water Times: " + flower.getWaterTimes()
							+ ", Bloom Time: " + flower.getBloomTime() + ", State: " + flower.getFlowerState() + ")");
				}
			} else {
				System.out.println("Error: " + response.getError());
			}
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
			return;
		}
	}

	public void waterFlower(String name) {
		FlowerCare request = FlowerCare.newBuilder().setName(name).build();
		WaterRes response;

		try {
			response = blockingStub5.waterFlower(request);
			System.out.println(response.getIsSuccess() ? response.getMessage() : response.getError());
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
			return;
		}
	}

	public void careForFlower(String name) {
		FlowerCare request = FlowerCare.newBuilder().setName(name).build();
		CareRes response;

		try {
			response = blockingStub5.careForFlower(request);
			System.out
					.println(response.getIsSuccess() ? response.getMessage() + " Bloom Time: " + response.getBloomTime()
							: response.getError());
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
			return;
		}
	}

	public static void handleFollowRequest(Client2 client) {
		Scanner scanner = new Scanner(System.in);
		String serviceName;
		String serverAddress;
		while (true) {
			System.out.print(
					"Would you like to: \n0) Exit \n1) Add a user \n2) Follow another user \n3) View all who a user is following \n");
			try {
				int num = scanner.nextInt();
				scanner.nextLine();
				switch (num) {
				case 0:
					break;
				case 1:
					serviceName = "services.Follow/addUser";
					System.out.println("Looking for a server for service: " + serviceName);

					serverAddress = client.findServer(serviceName);

					if (serverAddress == null || serverAddress.isEmpty()) {
						System.out.println("No server found for service: " + serviceName);
						break;
					}

					client.updateStubForFollow(serverAddress);

					System.out.println("Please enter the name of the user you wish to add: ");
					String name = scanner.nextLine();
					if (name.isEmpty()) {
						System.out.println("Name cannot be empty!");
						continue;
					}

					client.addUser(name);
					break;
				case 2:
					serviceName = "services.Follow/follow";
					System.out.println("Looking for a server for service: " + serviceName);

					serverAddress = client.findServer(serviceName);

					if (serverAddress == null || serverAddress.isEmpty()) {
						System.out.println("No server found for service: " + serviceName);
						break;
					}

					client.updateStubForFollow(serverAddress);

					System.out.println("Please enter your user name: ");
					String userName = scanner.nextLine();
					if (userName.isEmpty()) {
						System.out.println("Name cannot be empty!");
						continue;
					}

					System.out.println("Please enter the name of the user you wish to follow: ");
					String followName = scanner.nextLine();
					if (followName.isEmpty()) {
						System.out.println("Name cannot be empty!");
						break;
					}

					client.follow(userName, followName);
					break;
				case 3:
					serviceName = "services.Follow/viewFollowing";
					System.out.println("Looking for a server for service: " + serviceName);

					serverAddress = client.findServer(serviceName);

					if (serverAddress == null || serverAddress.isEmpty()) {
						System.out.println("No server found for service: " + serviceName);
						break;
					}

					client.updateStubForFollow(serverAddress);

					System.out.println("Please enter the name of the user you wish to view the following of: ");
					String name1 = scanner.nextLine();
					if (name1.isEmpty()) {
						System.out.println("Name cannot be empty!");
						continue;
					}

					client.viewFollowing(name1);
					break;
				default:
					System.out.println("Not a valid choice! Please choose one of the available options!");
					continue;
				}
				break;
			} catch (InputMismatchException ime) {
				System.out.println("Not a valid integer. Please try again!");
				scanner.nextLine(); // Clear the invalid input
				continue;
			} catch (Exception e) {
				System.out.println("Something went wrong... Try again later.");
				break;
			}
		}
	}

	public void addUser(String name) {
		UserReq request = UserReq.newBuilder().setName(name).build();
		UserRes response;

		try {
			response = blockingStub6.addUser(request);
			if (response.getIsSuccess()) {
				System.out.println("Successfully added user!");
			} else {
				System.out.println(response.getError());
			}
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
			return;
		}
	}

	public void follow(String userName, String followName) {
		UserReq request = UserReq.newBuilder().setName(userName).setFollowName(followName).build();
		UserRes response;

		try {
			response = blockingStub6.follow(request);
			if (response.getIsSuccess()) {
				System.out.println("Successfully followed user!");
			} else {
				System.out.println(response.getError());
			}
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
			return;
		}
	}

	public void viewFollowing(String userName) {
		UserReq request = UserReq.newBuilder().setName(userName).build();
		UserRes response;

		try {
			response = blockingStub6.viewFollowing(request);
			if (response.getIsSuccess()) {
				System.out.println("Following: ");
				for (String user : response.getFollowedUserList()) {
					System.out.println("--- " + user);
				}
			} else {
				System.out.println(response.getError());
			}
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
			return;
		}
	}

	public static void handlePetAdoptionRequest(Client2 client) {
		Scanner scanner = new Scanner(System.in);
		String serviceName;
		String serverAddress;
		while (true) {
			System.out.print(
					"Would you like to: \n0) Exit \n1) Upload a pet for adoption \n2) View all pets waiting for adoption \n3) Adopt a pet \n4) Search for a specific pet \n");
			try {
				int num = scanner.nextInt();
				scanner.nextLine();
				switch (num) {
				case 0:
					break;
				case 1:
					serviceName = "services.PetAdoption/addPet";
					System.out.println("Looking for a server for service: " + serviceName);

					serverAddress = client.findServer(serviceName);

					if (serverAddress == null || serverAddress.isEmpty()) {
						System.out.println("No server found for service: " + serviceName);
						break;
					}

					client.updateStubForPetAdoption(serverAddress);

					System.out.println("Please enter the name of the pet you would like to add: ");
					String petName = scanner.nextLine();
					if (petName.isEmpty()) {
						System.out.println("Name cannot be empty!");
						continue;
					}

					System.out.println(
							"Please indicate the pet's type: \n1) Dog \n2) Cat \n3) Bird \n4) Rabbit \n5) Dragon \n");
					int type = scanner.nextInt();
					scanner.nextLine();
					if (type < 1 || type > 5) {
						System.out.println("That is not one of the available types at this time!");
						continue;
					}

					System.out.println("Please enter the pet's age: ");
					int age = scanner.nextInt();
					scanner.nextLine();
					if (age < 0) {
						System.out.println("The age cannot be a negative number!");
						continue;
					}

					System.out.println("Please enter a description of your pet (or leave it blank): ");
					String description = scanner.nextLine();

					client.addPet(petName, type, age, description);
					break;
				case 2:
					serviceName = "services.PetAdoption/ListPets";
					System.out.println("Looking for a server for service: " + serviceName);

					serverAddress = client.findServer(serviceName);

					if (serverAddress == null || serverAddress.isEmpty()) {
						System.out.println("No server found for service: " + serviceName);
						break;
					}

					client.updateStubForPetAdoption(serverAddress);

					client.listPets();
					break;
				case 3:
					serviceName = "services.PetAdoption/RequestAdoption";
					System.out.println("Looking for a server for service: " + serviceName);

					serverAddress = client.findServer(serviceName);

					if (serverAddress == null || serverAddress.isEmpty()) {
						System.out.println("No server found for service: " + serviceName);
						break;
					}

					client.updateStubForPetAdoption(serverAddress);

					System.out.println("Please enter the ID of the pet you would like to adopt: ");
					int ID = scanner.nextInt();
					scanner.nextLine();
					if (ID < 1) {
						System.out.println("ID cannot be less than 1");
						continue;
					}

					client.requestAdoption(ID);
					break;
				case 4:
					serviceName = "services.PetAdoption/SearchPets";
					System.out.println("Looking for a server for service: " + serviceName);

					serverAddress = client.findServer(serviceName);

					if (serverAddress == null || serverAddress.isEmpty()) {
						System.out.println("No server found for service: " + serviceName);
						break;
					}

					client.updateStubForPetAdoption(serverAddress);

					System.out.println(
							"Please indicate the type you are looking for: \n1) Dog \n2) Cat \n3) Bird \n4) Rabbit \n5) Dragon \n");
					int type1 = scanner.nextInt();
					scanner.nextLine();
					if (type1 < 1 || type1 > 5) {
						System.out.println("That is not one of the available types at this time!");
						continue;
					}

					System.out.println("Please indicate the maximum age you would go for: ");
					int maxAge = scanner.nextInt();
					scanner.nextLine();
					if (maxAge < 0) {
						System.out.println("Age cannot be less than 0");
						continue;
					}

					client.searchPets(type1, maxAge);
					break;
				default:
					System.out.println("Not a valid choice! Please choose one of the available options!");
					continue;
				}
				break;
			} catch (InputMismatchException ime) {
				System.out.println("Not a valid integer. Please try again!");
				scanner.nextLine(); // Clear the invalid input
				continue;
			} catch (Exception e) {
				System.out.println("Something went wrong... Try again later.");
				break;
			}
		}
	}

	public void addPet(String name, int type, int age, String description) {
		AddPetReq request = AddPetReq.newBuilder().setName(name).setType(PetType.forNumber(type)).setAge(age)
				.setDescription(description).build();
		AddPetRes response;

		try {
			response = blockingStub7.addPet(request);
			System.out.println("Add Pet Response: " + response.getMessage());
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
		}
	}

	public void listPets() {
		Empty request = Empty.newBuilder().build();
		PetListRes response;

		try {
			response = blockingStub7.listPets(request);
			if (response.getIsSuccess()) {
				System.out.println("Available Pets:");
				for (Pet pet : response.getPetsList()) {
					System.out.println("ID: " + pet.getId() + ", Name: " + pet.getName() + ", Type: " + pet.getType()
							+ ", Age: " + pet.getAge() + ", Description: " + pet.getDescription());
				}
			} else {
				System.out.println("Error: " + response.getMessage());
			}
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
		}
	}

	public void requestAdoption(int ID) {
		AdoptionReq request = AdoptionReq.newBuilder().setPetId(ID).build();
		AdoptionRes response;

		try {
			response = blockingStub7.requestAdoption(request);
			System.out.println("Adoption Request Response: " + response.getMessage());
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
		}
	}

	public void searchPets(int type, int maxAge) {
		PetSearchReq request = PetSearchReq.newBuilder().setType(PetType.forNumber(type).name()).setMaxAge(maxAge)
				.build();
		PetSearchRes response;

		try {
			response = blockingStub7.searchPets(request);
			if (response.getIsSuccess()) {
				System.out.println("Search Results:");
				for (Pet pet : response.getPetsList()) {
					System.out.println("ID: " + pet.getId() + ", Name: " + pet.getName() + ", Type: " + pet.getType()
							+ ", Age: " + pet.getAge() + ", Description: " + pet.getDescription());
				}
			} else {
				System.out.println("Error: " + response.getMessage());
			}
		} catch (Exception e) {
			System.err.println("RPC failed: " + e);
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 6) {
			System.out.println(
					"Expected arguments: <host(String)> <port(int)> <regHost(string)> <regPort(int)> <message(String)> <regOn(bool)>");
			System.exit(1);
		}
		int port = 9099;
		int regPort = 9003;
		String host = args[0];
		String regHost = args[2];
		String message = args[4];
		try {
			port = Integer.parseInt(args[1]);
			regPort = Integer.parseInt(args[3]);
		} catch (NumberFormatException nfe) {
			System.out.println("[Port] must be an integer");
			System.exit(2);
		}

		String target = host + ":" + port;
		ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

		String regTarget = regHost + ":" + regPort;
		ManagedChannel regChannel = ManagedChannelBuilder.forTarget(regTarget).usePlaintext().build();
		try {
			Client2 client = new Client2(channel, regChannel);
			if (args[5].equals("true")) {
				System.out.println("Available Services (with registry): ");
				client.getServices();
			} else {
				System.out.println("Available Services (without registry): ");
				client.getNodeServices();
			}

			Scanner scanner = new Scanner(System.in);
			while (true) {
				System.out.print(
						"Pick from the following services (must be a number): \n0) Quit \n1) Echo Requests \n2) Joke Requests \n3) Flowers Requests \n4) Follow Requests \n5) Pet Adoption Requests \n");
				try {
					int choice = scanner.nextInt();
					scanner.nextLine();

					switch (choice) {
					case 1:
						handleEchoRequest(client);
						continue;
					case 2:
						handleJokeRequest(client);
						continue;
					case 3:
						handleFlowersRequest(client);
						continue;
					case 4:
						handleFollowRequest(client);
						continue;
					case 5:
						handlePetAdoptionRequest(client);
						continue;
					case 0:
						System.out.println("Goodbye!");
						break;
					default:
						System.out.println("Invalid choice. Please choose a valid service number.");
						continue;
					}
				} catch (InputMismatchException ime) {
					System.out.println("That is not a valid integer. Please try again!");
					scanner.nextLine();
					continue;
				} catch (Exception e) {
					System.out.println("An unknown error occurred. Goodbye!");
					e.printStackTrace();
					break;
				}
				break;
			}
			scanner.close();
		} catch (Exception e) {
			System.out.println("An error has occurred: " + e);
		} finally {
			channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
			regChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
		}
	}
}