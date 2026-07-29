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
Domain layer has two important class types:
1. **Entities**: They represent a row of database tables (e.g. User, Post...)
2. **Repository**: Repositories are a blue print of what should be done with the database, this means that they should be interface. later on they would be used inside application layer services (application layer should use the raw interface inside the code), and they are implemented inside infrastructure layer. Later on the implementations inside infrastructure layer should be injected to the application layer. This is useful because it decouples application layer and infrastructure layer.
#### 2. Application Layer
Application layer should implement any business logic inside it. things like authentication, registration and...
Application layer consists of few class types:
1. **DTOs**: Data Transfer Objects or DTOs wrap the entities inside domain layer for transformation.
2. **Services**: As i said before, services should implement any business logic for the project.
#### 3. Infrastructure Layer
Infrastructure layer does all the dirty work for IO and Database operations.
Infrastructure layer consists of few folders:
1. **API**: RESTful API Controllers live inside this folder, they map different endpoint to a block of code which calls a service from the application layer to do the job
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
│   │   ├── CreatePostRequest.java
│   │   ├── RegisterMediaRequest.java
│   │   ├── responses
│   │   │   ├── PostResponse.java
│   │   │   ├── UserProfileResponse.java
│   │   │   └── UserResponse.java
│   │   ├── UpdateProfileRequest.java
│   │   ├── UserLoginRequest.java
│   │   └── UserRegisterRequest.java
│   ├── ports
│   │   └── PasswordEncoderPort.java
│   └── services
│       ├── HashtagService.java
│       └── *Service.java
├── domain
│   ├── entities
│   │   ├── Hashtag.java
│   │   ├── Like.java
│   │   ├── Media.java
│   │   ├── Post.java
│   │   └── User.java
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
like server this layer has two folders:
1. **Entities**: these are the core business models of what is present on backend's entities, however they are different in few ways (e.g. they don't have a password field).
2. **Ports**: Ports are the same thing as server's domain layer repositories, however due to naming convention they are not repositories as they don't access a database or a storage directly. They set contracts for corresponding adapters.
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
Inside this layer there are **Adapters** which adapts (implements) Ports inside domain layer to DI into ViewModels.
## Diagram
<p align="center">
<img src="../../resources/architecture/client_architecture.png">
</p> 

## Tree 

```Tree
├───domain
│   ├───entities
│   │   ├─── Hashtag.java
│   │   ├─── Like.java
│   │   ├─── Media.java
│   │   ├─── Post.java
│   │   └─── User.java
│   └───ports
│       ├───dtos
│       │   ├─── HashtagDto.java
│       │   ├─── MediaDto.java
│       │   ├─── PostDto.java
│       │   ├─── UserDto.java
│       │   └─── UserProfileDto.java
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
