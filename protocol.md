# Project Protocol
This protocol is for the semester project for the Java course 'SWEN1' (Software Engineering 1) 
for the winter semester 2025 at the University of Applied Sciences FH Technikum Vienna.

# Usage
## Start
docker compose -f .\docker\docker-compose.yml up -d

## Check status
docker compose -f .\docker\docker-compose.yml ps

## Check logs
docker compose -f .\docker\docker-compose.yml logs -f

## Stop (stop container, hold data)
docker compose -f .\docker\docker-compose.yml down

## Reset (stop container, delete DB, initialize new schema)
docker compose -f .\docker\docker-compose.yml down -v
docker compose -f .\docker\docker-compose.yml up -d

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
9. Postman-/curl-Collection

### Steps done:

20251210: 
- Setup Repository, cloned it, created project in IntelliJ IDEA and added Maven.
- Added README.md and protocol.md.
- Downloaded and installed Docker Desktop and pulled postgres image.

## 2) Explanation of unit test coverage and why specific logic was tested

## 3) Notes on problems encountered and how they were solved

## 4) Estimated time tracking for each major part of the project
