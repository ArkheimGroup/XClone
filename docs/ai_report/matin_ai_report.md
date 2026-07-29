# AI Report
This document reports my usage of artificial intelligence throughout the development of **X Clone**, a full-stack social media application with a clean architecture.

In this project I used LLMs to help with architecture decisions, boilerplate generation, naming/design review, and general troubleshooting across both the server and client codebases.
 
---

### Server — Application layer
**Model**: Claude sonnet 5   
I used Claude to implement the core services. including post-to-DTO mapping, the query logic behind some of the methods, etc.   
I also used it to review my service dependency structure to prevent potential circular dependencies between services taking place.

---

### Client — Presentation layer (MVVM)
**Model**: Claude sonnet 5   
This is where I leaned on AI the most, since I was working through a lot of MVVM design decisions for the first time in JavaFX.   
I used Claude to implement the viewmodels for the presentation layer, each wrapping a corresponding port interface.   
It also helped me to solve some problems I had about the shared data between views.

---

### Server & Client - Domain layer
**Model**: Claude sonnet 5   
I used it to help design server-side and then client-side domain entites - what belongs on them, what doesn't, and how they should be structured.

---

### Clean Architecture
**Models**: Claude sonnet 5 & GPT-5.5
I generally used this two models to resolve my questions about the project's architecture, how a project with clean architecture works, how we put DTOs and DAOs into practice, etc. so that I would not make any mistakes and make every decision correctly and with ease throughout the project.