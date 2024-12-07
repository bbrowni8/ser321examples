# Project Description #
Implements the services echo, joke, follow, flowers, and petAdoption using GRPC. The details of these services can be found in their respective .proto files.

# Input Expections #
## Start ##
At the beginning, the client will be asked to pick a number 0-5 to choose a service. Choosing a number outside of this range (or a value that is not a number at all) will result in a message indicating that the input was not valid. 
Choices Include:
0) Exit
1) Echo Services
2) Joke Services
3) Flowers Services
4) Follow Services
5) Pet Adoption Services

## Services ##
### Echo ###
For the echo service, the client will be asked to input a string to be echoed back to them. 

### Joke Services ###
For the Joke services, the client will have the options:
0) Exit
1) Add a joke -- The client will be asked to provide enter the joke (String)
2) View a joke -- The client will be asked how many jokes they wish to view (int) 

### Flowers Services ###
For the Flowers services, the client will have the options:
0) Exit
1) Plant a flower -- The client will be asked to provide a flower name (String)
2) View all flowers -- No additional input is required
3) Water a flower -- The client will be asked to provide a flower name (String)
4) Care for a flower -- The client will be asked to provide a flower name (String)

### Follow Services ###
The client will have the options:
0) Exit
1) Add a user -- The client will be asked to provide a name (String)
2) Follow another user -- The client will be asked to provide the follower and followee (Strings)
3) View all who a user is following -- The client will be asked to provide a name (String)

### Pet Adoption Services ###
The client will have the options:
0) Exit
1) Upload a pet for adoption -- The client will be asked to provide a name (String), the pet's type (int), the pet's age (int), and a description (String)
2) View all pets waiting for adoption -- No additional input is needed
3) Adopt a pet -- The client will be asked to provide a pet ID (int) and the adoptee's name (String)
4) Search for a specific pet -- The client will be asked to provide a maximum age (int) and a type (int)

Pet Types are as follows:
1) Dog
2) Cat
3) Bird
4) Rabbit
5) Dragon

## End ##
The program loops back to the start (as long as no critical errors occurred) and the client may request another service.

# Fulfilled Requirements #
## Readme ##
a) Fulfilled
b) Fulfilled
c) Fulfilled
d) Fulfilled
e) Fulfilled 

## Task 1 ##
1) Fulfilled
2) Fulfilled
3) Fulfilled
4) Fulfilled
5) Fulfilled
6) Fulfilled
7) Fulfilled

## Task 2 ##
1) Fulfilled
2) Fulfilled 
3) Fulfilled

## Task 3.1 ##
1-3) Fulfilled
4a) Fulfilled
4b) Fulfilled
4c) Fulfilled
4d) Fulfilled
4e) Fulfilled

## Task 3.2 ##
1) Fulfilled
2) Fulfilled

# Screencast #
https://youtu.be/M1ZhhyY9rCI

# Testing #
Instead of implementing the Pauto=1 option for testing, unit tests were added to ServerTest.

First Terminal -- gradle runNode
Second Terminal -- gradle runTest

# GRPC Services and Registry

The following folder contains a Registry.jar which includes a Registering service where Nodes can register to allow clients to find them and use their implemented GRPC services. 

Some more detailed explanations will follow and please also check the build.gradle file

## Run things locally without registry
To run see also video. To run locally and without Registry which you should do for the beginning

First Terminal

    gradle runNode

Second Terminal

    gradle runClient

## Run things locally with registry

First terminal

    gradle runRegistryServer

Second terminal

    gradle runNode -PregOn=true 

Third Terminal

    gradle runClient -PregOn=true

### gradle runRegistryServer
Will run the Registry node on localhost (arguments are possible see gradle). This node will run and allows nodes to register themselves. 

The Server allows Protobuf, JSON and gRPC. We will only be using gRPC

### gradle runNode
Will run a node with an Echo and Joke service. The node registers itself on the Registry. You can change the host and port the node runs on and this will register accordingly with the Registry

### gradle runClient
Will run a client which will call the services from the node, it talks to the node directly not through the registry. At the end the client does some calls to the Registry to pull the services, this will be needed later.

### gradle runClient2 -PregOn=true
Will run a client which will call the services from the registry, it talks to the node through the registry.

Note: You must run it with -PregOn=true in order for the registry to work properly.

### gradle runDiscovery
Will create a couple of threads with each running a node with services in JSON and Protobuf. This is just an example and not needed for assignment 6. 

### gradle testProtobufRegistration
Registers the protobuf nodes from runDiscovery and do some calls. 

### gradle testJSONRegistration
Registers the json nodes from runDiscovery and do some calls. 

### gradle test
Runs the test cases for Joke and Echo. It expects a new start of the server before running the tests!
First run
    gradle runNode
then in second terminal
    gradle test

To run in IDE:
- go about it like in the ProtoBuf assignment to get rid of errors
- all mains expect input, so if you want to run them in your IDE you need to provide the inputs for them, see build.gradle
