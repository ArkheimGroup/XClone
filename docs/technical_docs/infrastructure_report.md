# Infrastructure Report

This document details the infrastructure layer of both the client and server modules of XClone.

- [Client Infrastructure](#client-infrastructure)
- [Server Infrastructure](#server-infrastructure)

---

## Client Infrastructure
Client infrastructure interacts with server's infrastructure.

### Adapters
Adapters adapt `Ports` inside Client's domain layer, they send HTTP Requests to server and receive the server's response.

There are two main types of adapters:

1.  **HTTP Adapters**: These adapters extend an abstract class called `ApiClient` to make RESTful API calls to the server. They handle the DTOs wrapping and unwrapping plus sending HTTP Requests. The following are the HTTP adapters:
    *   `HttpAuthAdapter`
    *   `HttpFollowAdapter`
    *   `HttpHashtagAdapter`
    *   `HttpMediaAdapter`
    *   `HttpPostAdapter`
    *   `HttpUserAdapter`

2.  **TCP Adapter**: The `TcpFeedAdapter` is a special adapter that communicates with the server over a raw TCP socket. This is used for fetching the user's feed in real-time. It sends the user's UUID to the server and receives a JSON array of posts.

### config
Containing configuration for server connection.

### exception
Including a copy of server's `ResultCode` so controllers and view models can have more detail information about the process, specially if the response represents an error (the code can specify what went wrong) . It also includes a custom Exception, `ApiException` which is thrown when server reports an error. Client does not need the variety exceptions server has since client can find the origin of exception according to `ResultCode` not exception's type.

### ApiClient
The `ApiClient` is an abstract helper class that simplifies making HTTP requests. This class has the duty of centralizing sending HTTP requests and process their responses. This class sends an HTTP request it receives and checks the response. If the response represents an error, `ApiClient` parses the `ApiResponse` inside and throws an `ApiException` . If the response represents a successful process, it checks the expected response type. If the expected type is `ApiResponse`, body is simply parsed and returned, but if the response is a `GenericApiResposne`, `ApiClient` parses the body and returns only the data inside the `GenericApiResponse` inside the body.

### LocalDateTimeAdapter
This class is a Gson compatibility adapter for `LocalDateTime`. It exists to deserialize several possible backend timestamp formats into one Java type. 
This class will be removed in future version when the API contracts are enhanced. This helper was vibe coded to quickly fix current DateTime deserializing problems for Beta version.

### Tree

```Tree
infrastructure
├───adapter
│   ├─── HttpAuthAdapter.java
│   ├─── HttpFollowAdapter.java
│   ├─── HttpHashtagAdapter.java
│   ├─── HttpMediaAdapter.java
│   ├─── HttpPostAdapter.java
│   ├─── HttpUserAdapter.java
│   └─── TcpFeedAdapter.java
├───config
│   └─── ClientConfig.java
├───exception
│   ├─── ApiException.java
│   └─── ResultCode.java
├─── ApiClient.java
└─── LocalDateTimeAdapter.java
```

---

## Server Infrastructure
The server infrastructure handles incoming requests, database interactions (CRUD), and managing passwords.

### API

The API layer is built using Spring Boot and provides a RESTful interface for the client. It consists of:

* **Controllers**: These classes handle the incoming HTTP requests and forward any buisness logic to the application layer and return the appropriate responses.
* **GlobalExceptionHandler**: This class provides exception handling instrcutions for the entire application, ensuring that errors are handled.

### Repository
The repository layer consists of JDBC implementations of the repository interfaces defined in the domain layer. These classes are responsible for all the database operations (CRUD).

### Socket
`SocketFeedServer` is a simple TCP socket server that provides a real-time feed to the clients. It listens on a configurable port, reads the user UUID, fetches the corresponding feed from the `TimelineService`, and sends it back as a JSON string.  
The server uses a cached thread pool to handle multiple clients concurrently.

### Config & Utils
*  **Config**: This package contains Spring Boot configuration classes. `AppConfig` defines beans for the application.
*   **Utils**: This package contains utility classes used across the infrastructure layer, such as the `UuidBinaryConvertor` and `BCryptPasswordEncoderPort`.

### Tree

```tree
infrastructure
├── api
│   ├── controllers
│   │   ├── AuthController.java
│   │   ├── FollowController.java
│   │   ├── HashtagController.java
│   │   ├── MediaController.java
│   │   ├── PostController.java
│   │   └── UserController.java
│   └── GlobalExceptionHandler.java
├── config
│   ├── AppConfig.java
│   ├── DataBaseInitializer.java
│   └── WebConfig.java
├── repository
│   ├── JdbcFollowRepository.java
│   ├── JdbcHashtagRepository.java
│   ├── JdbcLikeRepository.java
│   ├── JdbcMediaRepository.java
│   ├── JdbcPostRepository.java
│   └── JdbcUserRepository.java
├── socket
│   └── SocketFeedServer.java
└── utils
    ├── BCryptPasswordEncoderPort.java
    └── UuidBinaryConvertor.java
```
