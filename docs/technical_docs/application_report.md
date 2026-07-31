# Server Application Layer Report

This document details the application layer of the server module of X Clone.

---

## DTOs
The `dtos` package holds the two general api contracts, the two wrapper classes backend sends its responses using them:
* **GenericApiResponse**: For responses including data.
* **ApiResponse**: For responses not including data.

Both wrapper classes possess a boolean property indicating whether the request was processed successfully or not, 
a result code with type of `ResultCode` which is the business status backend sends and 
a message for more detail. 
Result code both used to ensure a successful process and find the error causing the error. 
If a response lacks result code, then the error cause is whether not handled or not an error a normal user should be aware of. 
The message inside the response will be shown to the user if the response is reporting an error. 
The `GenericApiReponse` has a generic property of `data` which will hold the attached object, usually a DTO.

## features
This package includes Queries, Commands, DTOs and mappers dedicated to each entity. 
The entities are **Hashtag**, **Media**, **Post**, **User** plus **Authentication**.

* **Query**: The records representing request bodies to be sent to the backend for *GET* operations.
* **Command**: The records representing request bodies to be sent to the backend, for *UPDATE*, *CREATE* and *DELETE* operations.
* **Mapper**: The helper class that contains logic for casting an entity's model, entity, DTO, etc. into each other. Mappers are the ones to handle constructing DTOs according to the models
* **DTOs**: The records representing response bodies that backend sends back.

Almost each api endpoint must have its very own query/command and DTO.

We prefer to apply polymorphism to mappers' methods unless forced to introduce different names.

## Models

Models are object representing entities, but with only the properties that are *business related* .
Services return models and prefer communicating by the means of them. 
These objects help separate responses (DTOs) and database objects (entities) from the business objects. 
An entity might have more than one models depending on system's needs.

## Ports
`PasswordEncoderPort` defines the contract for password hashing/verification that the application layer depends on, without knowing about the concrete hashing implementation. The actual implementation (`BCryptPasswordEncoderPort`) lives in infrastructure.

## Services
Services are basically where all the business logic should be implemented. Each service sits between the controllers and the domain repositories, and their job is to handle business-related processes.   
* **AuthService**: Handles registration and login.
* **UserService**: Owns user profile logic — reading and updating profile data, handling pin/upin and delete profile actions.
* **PostService**: The largest and most central service. It handles post creation, retrieval, toggle-like and find-posts-by-specific-keyword actions.
* **HashtagService**: Responsible for extracting, normalizing, and persisting hashtags.
* **MediaService**: Handles media registration, deletion and linking/unlinking medias to posts.
* **FollowUserService**: Manages the follow/unfollow relationships between users, backed by `FollowRepository`.
* **FeedService**: Builds the user's feed/timeline by pulling posts relevant to a user (e.g. from followed accounts) and shaping them into responses. It also owns the shared `toPostResponse(Post post, UUID requesterId)` mapper that assembles a post's computed fields.

---

### Tree

```tree
application
├── dtos
│   ├── GenericApiResponse.java
│   └── ApiResponse.java
├── features
│   ├── Authentication
│   │   └── commands
│   │       ├── LoginCommand.java
│   │       └── RegistrationCommand.java
│   ├── Hashtag
│   │   ├── dtos
│   │   │   └── GetHashtagDto.java
│   │   └── mapper
│   │       └── HashtagMapper.java
│   ├── Madia
│   │   ├── commands
│   │   │   └── RegisterMediaCommand.java
│   │   ├── dtos
│   │   │   └── GetMediaDto.java
│   │   └── mapper
│   │       └── MediaMapper.java
│   ├── Post
│   │   ├── commands
│   │   │   └── CreatePostCommand.java
│   │   ├── dtos
│   │   │   ├── GetPostDto.java
│   │   │   └── PostDetail.java
│   │   └── mapper
│   │       └── PostMapper.java
│   └── User
│       ├── commands
│       │   └── UpdateUserProfileCommand.java
│       ├── dtos
│       │   ├── GetIsFollowingDto.java
│       │   ├── GetUserDto.java
│       │   └── GetUserProfileDto.java
│       └── mapper
│           └── UserMapper.java
├── models
│   ├── post
│   │   ├── CreatePostModel.java 
│   │   └── Post.java
│   ├── Hashtag
│   │   ├── MinimalUser.java
│   │   └── User.java
│   ├── Hashtag.java
│   ├── Like.java
│   └── Media.java
├── ports
│   └── PasswordEncoderPort.java
└── services
    ├── AuthService.java
    ├── FeedService.java
    ├── FollowService.java
    ├── HashtagService.java
    ├── MediaService.java
    ├── PostService.java
    └── UserService.java
```
 
