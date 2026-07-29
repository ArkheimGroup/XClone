# Genral Report
This document talks about data orchestration & architecture and tech stack justifications.

# Table of contents
- [Genral Report](#genral-report)
- [Table of contents](#table-of-contents)
- [Data orchestarions](#data-orchestarions)
  - [1. API](#1-api)
  - [2. Socket](#2-socket)
- [Architecture Justification](#architecture-justification)
- [Tech stack Justification](#tech-stack-justification)
  - [Why RESTful API](#why-restful-api)
  - [Why Spring Boot](#why-spring-boot)
  - [Why MySQL](#why-mysql)
  - [Why Docker](#why-docker)

# Data orchestarions
There are two routes for the data to move:
## 1. API
```
    ┌─────────────────────────┐
    │          View:          │
    │      User interacts     │
    │     with Interface      │
    └───────────┬─────────────┘
                │ User
                │ Actions
    ┌───────────▼─────────────┐
    │       ViewModels:       │
    │   Controllers call      │
    │   ViewModel Methods     │
    └───────────┬─────────────┘
                │ Calls
                │ Methods
    ┌───────────▼─────────────┐
    │ Client Infrastructure:  │
    │   ViewModels send       │
    │  HTTP API Requests*     │
    └───────────┬─────────────┘
                │
                │ HTTP Request
    ============│============
                │ SERVER SIDE
    ┌───────────▼─────────────┐
    │ Server Infrastructure:  │
    │   API Controllers       │
    │    receive HTTP         │
    └───────────┬─────────────┘
                │ Calls
                │ Methods
    ┌───────────▼─────────────┐
    │   Application Layer:    │
    │  Infrastructure calls   │
    │   App. Service methods  │
    └───────────┬─────────────┘
                │ Calls
                │ Methods
    ┌───────────▼─────────────┐
    │  Infrastructure Layer:  │
    │ App Services call Repo  │
    │ In Infrastructure Layer*│
    └───────────┬─────────────┘
                │ Read/Write
                │ 
    ┌───────────▼─────────────┐
    │        Database:        │
    │  Infrastructure Layer   │
    │ manipulates database    │
    └─────────────────────────┘
```
>‌ \*:‌ in both client and server, ViewModels and Application layer never use Infrastructure layer directly.  
they use a interface from domain layer that infrastructure layer implements them and later on inside the main application code the infrastructure layer implementaions would be injected into ViewModels and Server's Application layer.

## 2. Socket
Socket's data orchestration is practically the same but instead of sending a HTTP request to the api, Client's infrastructure sends the user's UUID into the Server's socket.

---
# Architecture Justification
I Chose clean architecture as the architecture of this project for few reasons:  
1. Clean architecture makes the codebase more adaptable and scalable 
2. It is much much easier to work as a team on a project with clean architecture
3. People can work on the project simultaniasly 
4. Conflicts do not occur if people work on different layers

---
# Tech stack Justification
## Why RESTful API
RESTful API gives a straight forward, light-weight communication route between client and server.
this is much more reliable and cleaner than a normal Socket only connection as it introdouces status codes to the application.

But the real reason why i used RESTful API as the server-client communication method is the fact that building clients for RESTful API is much much easier.  
this is becuase REST API uses a built in protocol strcuture (HTTP), but if you where going to build the whole client-server communications using sockets you had to build a protocol structure.  
Also if a Raw TCP Socket was used other than the other issues the developer working on the project had to deal with, it had to manage a lot of low-level things, like buffer management, client connection managements and...

And for the final reason: testing a REST API is much much easier than a Socket server.  
to test a REST API you can use an already existing tool, but to test your socket you need to devbelop a test unit from scratch.

## Why Spring Boot
The main reson why i used Spring Boot as the server's framework was how easy it is to develop a RESTful API with Spring Framework, especially for a newbie.  
but there are other reasons:  
one of them is that you don't need to do the dependency injection manually, you only need to define Spirng **Bean**s and spring handles it.
another thing is that spring handles exceptions, you only need to give it an **Advice** configuration.

## Why MySQL
We chose MySQL as it is much more lightweight compared to PostgresSQL which was our other alternative.
and also we didn't develop this project with high client expected which means there is no need for an Enterprise SQL Server.

## Why Docker
I Used docker since it makes deployment hella easier.  
without docker anyone who tried to deploy the project needed a lot of setuping, i gave instructions about how to deploy the project in the [main README file](../../README.md#how-to-build-) you can compare that to [Docker deployment guide](../../server/README.Docker.md#build-and-run) and you see yourself.