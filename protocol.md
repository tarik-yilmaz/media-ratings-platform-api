# Project Protocol
This protocol is for the semester project for the Java course 'SWEN1' (Software Engineering 1) 
for the winter semester 2025 at the University of Applied Sciences FH Technikum Vienna.

# Usage
## Start
docker compose up -d

## Check status
docker compose ps

## Check logs
docker compose logs -f

## Stop (stop container, hold data)
docker compose down

## Reset (stop container, delete DB, initialize new schema)
docker compose down -v
docker compose up -d


## Test Postman (MRP_Postman_Collection.json)
1. Auth -> Register User:
POST /api/users/register

2. Auth -> Login User:
POST POST /api/users/login

3. Use Token (Authorization Header) to access other endpoints:
For all Media Requests:
- Key: Authorization
- Value: Bearer <token>

4. Media -> Create Media Entry:
POST /api/media
GET /api/media/{id}
Expected: 200

5. Media -> Update Media Entry:
PUT /api/media/{id}
Expected: 200
Media -> Delete Media Entry:
DELETE /api/media/{id}
Expected: 204


## 1) Description of technical steps and architecture
Following steps were taken to setup the project:
1. Setup of the project
2. PostgreSQL database setup
3. Http server setup
4. User-Interface setup and registration (without tokens?)
5. login & token generation
6. Media-entity & CRUD
7. Implementations of Ratings logic
5. Search & Filter
6. User Profile & Stats
7. Recommendations
8. Unit tests
9. Postman collection for manual API testing

### Steps done:

- Setup repository, cloned it, created project in IntelliJ IDEA and added Maven.
- Added README.md and protocol.md.
- Downloaded and installed Docker Desktop and pulled postgres image.
- Created docker compose setup for PostgreSQL and connected it to the project.
- Implemented schema initialization via schema.sql (tables + indexes).
- Added configuration loading via application.properties.
- Implemented JDBC repositories for users, media, ratings.
- Implemented basic auth flow:
- register user (password hashing)
- login user (token returned)
- Implemented Media CRUD endpoints and protected them via Bearer token.

## Architecure overview (packages/layers)
The project is implemented without a framework.

**config:**
Reads application.properties
Provides DB connection (DatabaseConfig) and server settings (ServerConfig)

**dto:**
Request objects for JSON input (RegisterRequest, LoginRequest, MediaRequest etc.)

**model:**
Domain objects (User, Media, Rating) implemented using Builder Pattern

**repository:**
Database access layer (JDBC + SQL)
Responsible for CRUD operations and mapping ResultSet -> model objects

**service:**


**Business logic layer (e.g. user registration rules, token validation rules, ownership checks):**


**controller + server startup (HTTP layer):**
Receives HTTP requests
Parses JSON using Jackson
Validates Authorization header (Bearer token)
Returns JSON responses + correct HTTP status codes

**Why this structure:**

Controller stays focused on HTTP (request/response).

Repository stays focused on SQL (no business decisions).

Business logic is kept in the service layer (better readability + testability).

## 2) Explanation of unit test coverage and why specific logic was tested

- Current test coverage is basic and focuses on configuration sanity checks:
- Server port is valid (1..65535)
- DB URL exists and contains postgresql
- BCrypt rounds are at least 10

Reason:
- Without correct config the whole application cannot run.
- These tests detect typical setup mistakes early (wrong properties, wrong port, missing DB URL)

Planned unit tests for later phases:
- AuthService (register, login) including password hashing and wrong password cases
- TokenService (token format, expiration, invalid token)
- MediaService (validation + ownership checks)
- Repository integration tests (optional)

## 3) Notes on problems encountered and how they were solved

**Docker compose not found**

Problem:
Running `docker compose up -d` in project root failed with: `no configuration file provided: not found`

Cause:
The compose file is located in docker/docker-compose.yml, not in the root directory.

Fix:
`docker compose -f .\docker\docker-compose.yml up -d`

**Java version mismatch (switch arrow syntax)**

Problem:
Compilation error about switch rules not supported in Java 11.

Cause:
Arrow switch syntax needs newer Java.

Fix:
Updated Maven compiler to Java 17.

**`return` inside static initializer**

Problem:
`java`: Rückgabe außerhalb von Methode in `DatabaseConfig`.

Cause:
`return` is not allowed inside `static{}` blocks.

Fix:
Replaced early return with if-else logic (no return).

**LocalDateTime JSON support**

Problem:
`LocalDateTime` needs Jackson JavaTimeModule.

Fix:
Added dependency to `jackson-datatype-jsr310`.
## 4) Estimated time tracking for each major part of the project

- Project setup (repo, IntelliJ, Maven ): ca. 2h
- Docker/Postgres setup + schema init: ca. 4h
- Config setup (properties, DatabaseConfig, ServerConfig): ca. 4h
- Repository layer (User/Media/Rating): ca. 5h
- Auth flow (register, login + hashing + token): ca. 5h
- Media enpoints (CRUD + auth protected): ca. 5h
- Postman testing + fixes: ca. 3h
- Unit test setup: ca. 3h

