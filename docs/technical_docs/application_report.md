# Server Application Layer Report

This document details the application layer of the server module of X Clone.

---

## DTOs
The `dtos` package holds the request and response objects that cross the boundary between the API layer and the services. It is a leaf package — nothing in it depends on repositories or other outgoing infrastructure.   
* **Requests**: 
  - `CreatePostRequest`
  - `RegisterMediaRequest`
  - `UpdateProfileRequest`
  - `UserLoginRequest`
  - `UserRegisterRequest`
* **Responses** (`dtos/responses`): 
  - `PostResponse`,
  - `UserProfileResponse`
  - `UserResponse`  
  
These DTOs are what controllers accept and return

## Ports
`PasswordEncoderPort` defines the contract for password hashing/verification that the application layer depends on, without knowing about the concrete hashing implementation. The actual implementation (`BCryptPasswordEncoderPort`) lives in infrastructure.

## Services
Services are basically where all the real logic happens. Each service sits between the controllers and the domain repositories, and their job is to handle the actual business rules, and shaping data into response DTOs.   
* **AuthService**: Handles registration and login.
* **UserService**: Owns user profile logic — reading and updating profile data and shaping `UserResponse`/`UserProfileResponse` DTOs, handling pin/upin and delete profile actions.
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
│   ├── CreatePostRequest.java
│   ├── RegisterMediaRequest.java
│   ├── responses
│   │   ├── PostResponse.java
│   │   ├── UserProfileResponse.java
│   │   └── UserResponse.java
│   ├── UpdateProfileRequest.java
│   ├── UserLoginRequest.java
│   └── UserRegisterRequest.java
├── ports
│   └── PasswordEncoderPort.java
├── exception
│   └── BadArgumentException.java
|   ├── BaseApplicationException.java
|   ├── ConflictException.java
|   ├── ForbiddenException.java
|   ├── NotFoundException.java
|   └── ErrorCode.java
└── services
    ├── AuthService.java
    ├── FeedService.java
    ├── FollowService.java
    ├── HashtagService.java
    ├── MediaService.java
    ├── PostService.java
    └── UserService.java
```
 
