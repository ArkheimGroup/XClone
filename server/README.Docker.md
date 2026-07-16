# XClone Server Docker integration
This document provides instructions on how to build, run, containerize and configure XClone backend server and its MySQL database using Docker.

---
## How it works
Server module is a [Multi-Container application](https://docs.docker.com/get-started/docker-concepts/running-containers/multi-container-applications/), two containers (One for Database and the other for java server programm) are stacked togheter.

### Database container
Database container is based on `mysql:8.0` image.

### Server container
Server container is based on `eclipse-temurin:25-jdk-jammy` image, a lightweight debian container developed by eclipse for java applications.  
also server container relies on Database container.

---
## Requirements 
First of all you need docker engine  
on linux you can use docker engine, but on macOS and Windows it's required to use Docker Desktop.

+ [Docker Desktop Download](https://www.docker.com/products/docker-desktop/)
+ [Docker engine Docs](https://docs.docker.com/engine/install/)
---
## Quick start
Docker compose it the easiest way to run entire backend stack (MySQL Database, Spring Boot Api and Socket IO Server)
### Build and run
Change your directory to `XClone/server` directory, where `Dockerfile`, `compose.yaml` and this README is located.  
Then run:
```bash
docker compose up --build
```
> Note: you can use `-d` flag with the compose command to detach from docker engine (run the server in background), or you can pree `d` when it finished initializing  

This command:  
1. Pulls MySQL database image
2. Builds the Spring Boot application
3. Starts MySQl database container (`db`), initializes with a user with credentials specified as environment variables in `compose.yaml`
4. Creates a Database with the name specified in `compose.yaml`
5. If `db` container was healthy, it Starts the Spring Boot application with environment variables specified in `compose.yaml`

> Note: username and passwords should be same in both environment variables for MySQL `db` container and Spring Boot `server` container as those environment variables are used to connect Spring Boot application to the database

Once started backend services would be available at:
+ **REST API**: `http://localhost:8080`
+ **Java socker**: at port `8082`
+ **MySQL Database**: `localhost:3306`
### Stoping the services
If you are attached into the docker application you can just use `Ctrl + c` to stop the app  
if detached (application running in the background):
```bash
docker compose down
```
> Note: if you want to delete database volumes use `-v` flag.  

### Restarting the services after image build
If The image i already built, you can restart the services with this command (inside `XClone/server` directory):
```bash
docker compose up server
```

---
## Environment variables


| Environment Variable | Default Value in Compose | Description |
| :--- |:-------------------------| :--- |
| `DB_HOST` | `db`                     | The hostname of the database container (`db` service). |
| `DB_PORT` | `3306`                   | The port on which MySQL is running inside the network. |
| `DB_NAME` | `x_clone`                | The database schema name to connect to. |
| `DB_USER` | `arkheim`                | The MySQL user name. |
| `DB_PASSWORD` | `12345678`               | The MySQL password for the user. |
| `SPRING_SQL_INIT_MODE` | `always`                 | Instructs Spring Boot to run `schema.sql` on startup. |
| `SPRING_SQL_INIT_CONTINUE_ON_ERROR` | `true`                   | Continues execution even if schema initialization errors occur. |
---
## Rebuilding the app
Since the compilation is done using docker container, any changes done to the source code or resources (except for `uploads` folder which is attached as a volume) would require a complete rebuild to affect the images.  
i recommend running the app on locally on your machine with spring `application.properties` environment variables manually specified until you reach a certain point
then you can use the build command.