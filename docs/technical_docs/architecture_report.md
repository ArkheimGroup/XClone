# Architecture
This document examines the architecture of XClone.  
- [Server](#server)
- [Client](#client)
---
# Server

## Layers
Server modules consists of three layers:
#### 1. Domain Layer
Domain Layer is the core layer of application. any other layer would know about this layer since this is where project is rooted.
Domain layer has three important class types:
1. **Entities**: They represent a row of database tables (e.g. User, Post...)
2. **Repository**: Repositories are a blue print of what should be done with the database, this means that they should be interface. later on they would be used inside application layer services (application layer should use the raw interface inside the code), and they are implemented inside infrastructure layer. Later on the implementations inside infrastructure layer should be injected to the application layer. This is useful because it decouples application layer and infrastructure layer.
3. **Exception**: Average system exception won't be as much helpful has custom exceptions. A number of extra properties added to an average exception will enhance system's exception handling greatly. This practice will make the system more predictable, more capable throwing user-friendly exceptions and ease debugging. This package will declare the custom exceptions and the custom status codes inside the system.
#### 2. Application Layer
Application layer should implement any business logic inside it. things like authentication, registration and...
Application layer consists of few class types:
1. **Models**: Data object representing business-related format of entities, omitting unnecessary database-related fields. Services rather communicating via models. 
2. **DTOs**: Data Transfer Objects or DTOs wrap the models/entities inside domain layer for transformation.
3. **Services**: As I said before, services should implement any business logic for the project.
#### 3. Infrastructure Layer
Infrastructure layer does all the dirty work for IO and Database operations.
Infrastructure layer consists of few folders:
1. **API**: RESTful API Controllers live inside this folder, they map different endpoint to a block of code which calls one or more services from the application layer to perform the task.
2. **GlobalExceptionHandler**: System must still respond to the client in a predictable manner. This class will catch each exception that might occur during the process, and returns an object honoring API contracts, thus client will still receive information after calling the backend (Even if the response reports and error) . 
2. **Repository**: This folder contains implementations of domain layer repositories, these are the one to later on inject to the application layer.

## Diagram
<p align="center">
<img src="../../resources/architecture/server_architecture.png">
</p> 

## Tree 

```Tree
server
├── application
│   ├── dtos
│   │   ├── ApiResponse.java
│   │   └── GenericApiResponse.java
│   ├── features
│   │   └── Each entitiy's DTOs, queries, commands and mapper
│   ├── models
│   │   └── One or more model for each entity
│   ├── ports
│   │   └── PasswordEncoderPort.java
│   └── services
│       ├── HashtagService.java
│       └── *Service.java
├── domain
│   ├── entities
│   │   ├── HashtagEntity.java
│   │   ├── LikeEntity.java
│   │   ├── MediaEntity.java
│   │   ├── PostEntity.java
│   │   └── UserEntity.java
│   ├── exception
│   │   ├── ResultCode.java
│   │   ├── BaseApplicationException.java
│   │   └── *Exception.java
│   └── repository
│       ├── FollowRepository.java
│       └── *Repository.java
├── infrastructure
│   ├── api
│   │   ├── controllers
│   │   │   ├── AuthController.java
│   │   │   └── *Controller.java
│   │   └── GlobalExceptionHandler.java
│   ├── config
│   │   └── AppConfig.java
│   ├── repository
│   │   ├── JdbcHashtagRepository.java
│   │   └── Jdbc*Repository.java
│   ├── socket
│   │   └── SocketFeedServer.java
│   └── utils
│       ├── BCryptPasswordEncoderPort.java
│       └── UuidBinaryConvertor.java
└── ServerApplication.java
```

To replicate the actual tree, run inside root of XClone:

```bash
tree server/src/main/java/arkheim/server
```

---
# Client
## Layers
Client module also consists of three layers:
#### 1. Domain Layer
like server this layer has three packages:
1. **DTOs**: These are *Data Transfer Objects*, both representing requests that can be sent to backend and responses that'll be received from the backend.
2. **Models**: Similar to server's models, these are the core business models of what is present on backend's entities, however they are different in few ways (e.g. they don't have a password field).
3. **Ports**: Ports are the same thing as server's domain layer repositories, however due to naming convention they are not repositories as they don't access a database or a storage directly. They set contracts for corresponding adapters.

#### 2. Presentation Layer
This layer alone leverages another architecture itself: Model View ViewModel or MVVM.

<p align="center">
<img src="../../resources/architecture/mvvm.png">
</p> 


In this architecture, Views handles user interactions and sends the events to ViewModels where they perform the required task and manipulate the model which is present inside server's database.

This layer has 5 packages:
1. **controllers** : Includes controllers for each view in the project. A controller's duty is to handle user interactions with UI, handle UI changes, handle scene switching, making the calls to view models and binding FXML objects to their corresponding property inside the view model.
2. **navigation** : This package contains navigation system. Responsible for switching between pages (scenes) and applying styling according to the theme.
3. **state** : Currently, this package is dedicated to Home page only. It's consisted of this page's UI events contract and a state record.
4. **theme** : This package is only consisted of one enum representing style theme (light / dark mode)
5. **viewmodels** : Includes view models, a view model task is to handle backend/port execution and change in states and shared UI properties.

#### 3. Infrastructure Layer
This layer handles clients connection to the server. Including classes to send and receive data, configurations for connection.

1. **Adapter**: This package is the one sending HTTP requests. Although `TcpFeedAdapter` uses raw TCP for communication since it's duty is far more expensive than other adapters. Each adapter represents an api endpoint from the server, each create the corresponding HTTP request, passes it to `ApiClient` and returns the response. 
2. **ApiClient**: This class has the duty of centralizing sending HTTP requests and process their responses. This class sends an HTTP request it receives and checks the response. If the response represents an error, `ApiClient` parses the `ApiResponse` inside and throws an `ApiException` . If the response represents a successful process, it checks the expected response type. If the expected type is `ApiResponse`, body is simply parsed and returned, but if the response is a `GenericApiResposne`, `ApiClient` parses the body and returns only the data inside the `GenericApiResponse` inside the body.
3. **config**: Including configuration for server connection.
4. **exception**: Including a copy of server's `ResultCode` so controllers and view models can have more detail information about the process, specially if the response represents an error (the code can specify what went wrong) . It also includes a custom Exception, `ApiException` which is thrown when server reports an error. Client does not need the variety exceptions server has since client can find the origin of exception according to `ResultCode` not exception's type.

According to current API contracts, server only responses using `ApiResponse` (and not `GenericApiResponse`) when an error occurs.

## Diagram
<p align="center">
<img src="../../resources/architecture/client_architecture.png">
</p> 

## Tree 

```Tree
├───domain
│   ├───dtos
│   │   ├───Hashtag
│   │   │   └───response
│   │   │       └─── HashtagDto.java
│   │   ├───Media
│   │   │   ├───request
│   │   │   │   └─── RegisterMediaRequest.java
│   │   │   └───response
│   │   │       └─── MediaDto.java
│   │   ├───Post
│   │   │   ├───request
│   │   │   │   └─── CreatePostRequest.java
│   │   │   └───response
│   │   │       ├─── PostDto.java
│   │   │       └─── PostDetailDto.java
│   │   ├───User
│   │   │   ├───request
│   │   │   │   ├─── LoginRequest.java
│   │   │   │   ├─── RegisterRequest.java
│   │   │   │   └─── UpdateUserProfileRequest.java
│   │   │   └───response
│   │   │       ├─── IsFollowingDto.java
│   │   │       ├─── UserDto.java
│   │   │       └─── UserProfileDto.java
│   │   ├─── ApiResponse.java
│   │   └─── GenericApiResponse.java
│   ├───models
│   │   ├─── Hashtag.java
│   │   ├─── Like.java
│   │   ├─── Media.java
│   │   ├─── Post.java
│   │   └─── User.java
│   └───ports
│       ├─── AuthPort.java
│       ├─── FeedPort.java
│       ├─── FollowPort.java
│       ├─── HashtagPort.java
│       ├─── MediaPort.java
│       ├─── PostPort.java
│       └─── UserPort.java
├───infrastructure
│   ├───adapter
│   │   ├─── HttpAuthAdapter.java
│   │   ├─── HttpFollowAdapter.java
│   │   ├─── HttpHashtagAdapter.java
│   │   ├─── HttpMediaAdapter.java
│   │   ├─── HttpPostAdapter.java
│   │   ├─── HttpUserAdapter.java
│   │   └─── TcpFeedAdapter.java
│   ├───config
│   │   └─── ClientConfig.java
│   ├───exception
│   │   ├─── ApiException.java
│   │   └─── ResultCode.java
│   ├─── ApiClient.java
│   └─── LocalDateTimeAdapter.java
├───presentation
│   ├───controllers
│   │   ├─── BaseController.java
│   │   ├─── HomeController.java
│   │   ├─── LoginController.java
│   │   ├─── PostController.java
│   │   ├─── ProfileController.java
│   │   └─── RegisterController.java
│   ├───navigation
│   │   ├─── JavaFxNavigator.java
│   │   └─── Navigator.java
│   ├───state
│   │   ├─── FeedUiEvent.java
│   │   └─── FeedUiState.java
│   ├───theme
│   │   └─── ThemeMode.java
│   ├───utils
│   │   ├─── IconUtils.java
│   │   └─── MediaUiUtills.java
│   ├───viewmodels
│   │   ├─── AuthViewModel.java
│   │   ├─── FeedViewModel.java
│   │   ├─── FollowViewModel.java
│   │   ├─── HashtagViewModel.java
│   │   ├─── MediaViewModel.java
│   │   ├─── PostViewModel.java
│   │   └─── UserViewModel.java
│   └─── MainApplication.java
└─── Launcher.java
```

To replicate the actual tree, run inside root of XClone:

```bash
tree client/src/main/java/arkheim/client
```
