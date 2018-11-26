# ephemeral-chat

## Framework, code
This project uses Java JDK 8, maven, Spring Framework, and MySql.

## Modules
**Chat-Service** : Main application that handles chat messaging logic. Can be scaled with multiple instances.
**Service-Registry** : Service Registry and Load Balancer. Allows for the services to connect and communicate with each other.
**Edge-Service** : A Zuul implemented gateway 'edge service'. Allows for clients to make a request into the microservice cluster and have the request be load balanced between the horizontally scaled, multiple instances of the chat-service's. 
**Chat-Service-Client-Library** : Client library jar for java implemented clients to import and use the DTO's for quicker use of the API's.

## Summary
Ephemeral Chat application. Messages are implemented in a hot/cold storage system. 
There is an API endpoint to persist a message initially into the 'Hot Storage' with an optional timeout. The timeout will begin counting down as soon as it is persisted. Upon expiring, the message will be moved to the 'Cold Storage'. Alternatively another API endpoint to retrieve all unexpired messages for a given username is available. This will immediately cause the retrieved messages to expire and move to cold storage as well.

## Presistence
The **Hot Storage** and **Cold Storage** is implemented via a relational database, specifically mysql. The Hot Storage also has a distributed caching implementation via Hazelcast. This allows for in-memory data retrieval for fast performance. This also allows the design of the **Chat-Service** to be *horizontal scalability*.

## Horizontal Scalability
With the use of the load balancer / service registry and the gateway edge service, and most importantly the design of the Chat-Service utilizing Hazelcast caching, this microservice cluster is ready and available to be horizontally scaled by starting up multiple instances of the Chat-Service. 

# Getting Started! Environment Setup.

## MySql
1) Download MySQL Community Server
  https://dev.mysql.com/downloads/mysql/

  Follow the steps in the installer setup.
  Once it asks for a password, choose "password" for the password for 'root'. Alternatively any password can be chosen,   however the application.yml configuration file will need to be updated in the chat-service accordingly.

  Mysql service should start running immediately. 
  Start the command line interface.
  If on Linux/Mac, update the PATH variable with the mysql location PATH="/usr/local/mysql/bin:$PATH" Or alternatively navigate to the bin and run the sql interface directly

  Run the command line interface by typing: "mysql -u root -p"
  It will ask for password, type 'password' as was configured during installation

  Create the database that is to be used: **"CREATE DATABASE chatdb;"**

## Downloading / Compiling the Services
2) Clone this repository and navigate to the root directory
  Compile the parent pom, which in turn compiles the child modules by running "mvn clean install"
  The appropriate libraries should be downloaded and installed to assemble the JAR files
  After getting "SUCCESS", you're ready to start each service.
  
## Running the Services
3) Run the JAR of each service. Order of startup is not mandatory but for fastest startup with no warnings, use this order:

  <pre>
  java -jar /service-registry/target/service-registry-1.0.0-SNAPSHOT.jar
  java -jar /edge-service/target/edge-service-1.0.0.SNAPSHOT.jar
  java -jar /chat-service/target/chat-service-1.0.0.SNAPSHOT.jar
  </pre>
  
  To run more instances to demonstrate horizontal scalability, at any time, run additional instances of the chat-service. A service may take up to 30 seconds to register with the Service Registry.
  * Note: The chat-service defaults to run on port 9000, any additional instance will need to be configured for another available port. This can be done dynamically via the command line
  
  <pre>
  java -jar /chat-service/target/chat-service-1.0.0.SNAPSHOT.jar --server.port=9001
  java -jar /chat-service/target/chat-service-1.0.0.SNAPSHOT.jar --server.port=9002
  java -jar /chat-service/target/chat-service-1.0.0.SNAPSHOT.jar --server.port=9003
  </pre>
  
  # API
  To communicate directly with the chat-service, you can use the API via the address of the service:
  For Example: GET operation: http://localhost:9000/chat/{id} (Description below)
  However this eliminates the benefits of the gateway service and horizontal scaling. Therefore by going through the edge service, it will forward the API request to any available chat-service instance running and registered. 
  For Example: GET operation: http://localhost:8080/chat-service/chat/{id}
  As you can see the port has changed from 9000 (Chat-Service) to 8080 (Edge-Service), and an additional */chat-service/* has entered the URL, and that's used to tell the edge service where to forward the request to.
  
### **POST /chat**
  Creates a new text message for passed in username. This message will be persisted in the backend database as well as stored in the distributed cache. "timeout" is optional, with a default value of 0. Username or text can not be null/empty. Timeout can not be a negative or 0 value.

  Example request body
  <pre>
  {
    "username": "David", 
    "text": "A short-lived message", 
    "timeout": 25
  }
  </pre>
  
  Example response body
  <pre>
  {
    "id": 9876
  }
  </pre>
  
### **GET /chat/{id}**
  Returns the message object for the given id. This service can return both expired and unexpired messages.
  
  Example response body
  <pre>
  {
    "username": "David",
    "text": "A longer-lived message"
    "expiration_date": "2018-12-30 15:20:30" 
  }
  </pre>

### **GET /chats/{username}**
  Returns a list of unexpired messages from the Hot Storage system. The messages are moved from the Hot Storage into the Cold storage in the backend. These messages can no longer be retrieved via this endpoint. 
  
  <pre>
  Example response body
  [
    {
      "id": 5656,
      "text": "This is a message"
    },
    {
      "id": 95958,
      "text": "This is also a message"
    }
  ]
  </pre>


# Epilogue
## The Decisions I've Made:
I've chosen to extend the service completely as a fully designed, fully functioning and thought out service to demonstrate the ease at which frameworks like Spring Framework, Hazelcast, Zuul, Service Registry and MySql can quickly come together providing a scalable set of microservices that are not only performant, but also extensible.

## Limitations of This Implementation:
As noted in the decisions made, this implementation has decided to not cripple any area of the system for the sake of quickly providing a "*functional*" Proof of Concept. It's easy to provide something that is functional, yet harder to design something well enough that is also performance with the full set of features. 
However, there still are some limitations with this implementation:
- **Hazelcast**: The cache is an excellent caching system with many features and uses. The current configuration in chat-service only allowed for a single cache map that's populated during run-time. Therefore a limitation of the service is that if all services are shut down, then started back up, the backing persistence storage will not populate into the cache. At least one instance of chat-service must remain running while the cache has objects in it. This reduces the Highly Available status of the service.
- **MySql**: MySql is a decent database but a NoSql like Mongo may be better suited for a horizontally scaled system.
- **Containers**: This implementation does not provide any type of containers like Docker which would allow DevOps style deployments much easier
- **Misc**: A better logging system, external configurations, a management dashboard, etc. A lot more 3rd party solutions can increase the deploymnt, maintainability, alarms and events, etc of the system. 

## What Would You Do if You Had More Time?
As noted in the limitations, there are multiple things that can continuely be improved upon to eliminate the limitations as well as making the management of the services easier. 

## How Would You Scale It In The Future?
I made careful point to make sure the scaling works now. The requirements are easy to get 'functioning' but designing the scaling out is important to get done right first, and well. As noted earlier, a limitation is that a cold start of the system does not populate the cache, this needs to be fixed to improve the scalability and high availability factors. Additionally adding Docker would make scaling easier.

David Moore
davidwmoore3@gmail.com
