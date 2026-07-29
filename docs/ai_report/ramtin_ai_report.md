# AI Report
This document reports my usage of artificial intelligence throughout the project development.

---
## General
**Model**: ChatGPT 5.6

Generally I constantly asked AI which package each class has to go in. I would explain a class's duty and its DIs and
the AI agent would give advice for its placement. Although model usually instructed for minimal cases where I had to bring up
more orginized tree architectures

---
## Server development

anything else inside domain layer (Entities and Repositories) was written from scratch by myself.
### Infrastructure layer
**Model**: ChatGPT 5.6

I asked AI's advise for what fields the `ApiError` record should have. Furthermore, when the develpement of
`GlobalExceptionHandler` was over, I asked AI for final review for debugs, none of AI's generated code was copy-pasted
AI only helped to detect the bugs.

--- 
## Client development
### General
**Model**: ChatGPT 5.6

Since I had no experience for frontend development, I asked AI how the frontend should be layered according to Clean Architecture.
Once again the model provided me with minimalism, parts like navigation system were my own ideas.

### presentation
**Model**: ChatGPT 5.6

AI was asked to provide the first draft of CSS styling files. The styles were good, but the details were omitted, thus I had to modify it. 
For login/registration pages, AI provided the regex patterns for validating emails, since adding them myself would take much longer time. 
Parts like states were made by AI along with strict reviews. I had long conversations with AI to understanding state's purpose and workflow in order to suggest fixes to the team.
The first draft of `FeedViewModel` auto refresh was created by AI, it was then added to the view model after team removed unnecessary code and smells, added the needed configurations and details and
ensured its compatibility and reliability. 
The first draft of avatar loading was made by AI, so the team could apply the pattern to wherever required.

---
## Debugging
**Model**: ChatGPT 5.6

At the early stages, each segment of the project would through multiple warnings which soon invoked our concerns. 
AI was a part of research tools to understand warnings origin and their fixes. Some warnings where overlooked since 
their fix was not required and team could continue the delevopment without concerns.