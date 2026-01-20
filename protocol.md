# Project Protocol

This protocol documents the semester project Media Ratings Platform (MRP) for the Java course SWEN1 (Software Engineering 1) in Winter Semester 2025 at FH Technikum Wien.

The project is a standalone RESTful HTTP server (no Spring/ASP.NET) that provides an API for frontends (frontend not part of this project).
Persistence is implemented with PostgreSQL (Docker) and JDBC.

# Usage

## Start database (Docker)

```plaintext
docker compose up -d
```

if your compose file is not in the repository root:

```plaintext
docker compose -f ./docker/docker-compose.yml up -d
```

## Check status

```plaintext
docker compose ps
```

## Check logs

```plaintext
docker compose logs -f
```

## Stop (stop container, keep data)

```plaintext
docker compose down
```

## Reset (stop container, delete DB, initialize new schema)
```plaintext
docker compose down -v
docker compose up -d
```

# Manual API testing with Postman

Postman Collection: **MRP_Postman_Collection.json**

**Authentication flow**
**1. Register**
- `POST /api/users/register` 
- Body: `{ "username": "...", "password": "..." }`
**2. Login**
- `POST /api/users/login` 
- Body: `{ "username": "...", "password": "..." }`
- Response: token string
**3. Use token for all protected endpoints**
- Header:
  - `Authorization: Bearer <token>`

# Implemnted API endpoints (overview)

## Auth (public)
- `POST /api/users/register`
- `POST /api/users/login`

## Media (protected)
- `GET /api/media` (search/filter/sort)
- `POST /api/media` (create)
- `GET /api/media/{id}` (details)
- `PUT /api/media/{id}` (update, only creator)
- `DELETE /api/media/{id}` (delete, only creator)
- `POST /api/media/{id}/rate` (create rating for media)
- `GET /api/media/{id}/ratings` (list ratings with comment-visibility rule)
- `POST /api/media/{id}/favorite` (mark favorite)
- `DELETE /api/media/{id}/favorite` (unmark favorite)`

## Ratings (protected)
- `PUT /api/ratings/{id}` (update, only owner)
- `DELETE /api/ratings/{id}` (delete, only owner)
- `POST /api/ratings/{id}/confirm` (confirm own comment)
- `POST /api/ratings/{id}/like` (like other users ratings, max 1 like)

## User (protected, only self)
- `GET /api/users/{username}/profile`
- `PUT /api/users/{username/profile` (update email)
- `GET /api/users{username}/ratings` (own rating history)
- `GET /api/users/{username}/recommendations?limit=10`

## Leaderboard (public)
- `GET /api/leaderboard?limit=10`


## 1) Description of technical steps and architecture decisions

## Major implementation steps

**Project setup**
- Maven project created in IntelliJ IDEA
- Java 17 configured (maven-compiler-plugin + surefire)

**Database setup**
- PostgreSQL started via Docker Compose
- Schema initialization via `schema.sql` (tables + constraints + indexes)

**HTTP server setup**
- Java built-in `com.sun.net.httpserver.HttpServer`
- Central route registration in `MrpHttpServer`
- Thread pool via `Executors.newFixedThreadPool(16)`

**Configuration**
- `application.properties` for server and database configuration
- `DatabaseConfig` for JDBC connection handling
- `ServerConfig` for port and BCrypt settings

**Core domain & persistence**
- Models: `User`, `Media`, `Rating` implemented via Builder Pattern
- Repositories: pure JDBC repositories for SQL access and mapping (`ResultSet -> model`)

**Authentication**
- Password hashing with BCrypt
- Token issuance via `TokenService`
- Bearer token required for all endpoints except register/login
- Token maps to `userId` and is checked on each request

**Media management**
- CRUD endpoints for media entries
- Ownership rule: only `creatorId` may update/delete

**Rating system**
- Create rating (1–5 stars, optional comment)
- One rating per user per media enforced by DB unique constraint (`media_id`, `user_id`)
- Comment moderation: comment is only public after confirmation by author
- Like system: max 1 like per user per rating, stored in `rating_likes`

**Favorites**
- Favorite/unfavorite via `favorites` table with uniqueness (`user_id`, `media_id`)
- List favorites via `/api/users/favorites` endpoint

**Search / Filter / Sort**
- Server-side filtering in SQL (`MediaRepository.findFiltered`)
- Allowed filters: title partial match, genre match, mediaType, releaseYear, ageRestriction, minRating
- Allowed sort: title, year, score (whitelisted to avoid SQL injection)

**User profile & statistics**
- Profile endpoint includes statistics fields: `total_ratings`, `average_rating`
- Denormalized statistics updated after rating changes:
    - `media.average_score` recalculated from ratings
    - `users.total_ratings` and `users.average_rating` recalculated from ratings

**Recommendations**
- Simple scoring-based approach:
    - Use previously highly rated media (>= 4 stars)
    - Candidate set: media not rated by user
    - Score based on favorite genre + content similarity (type, age restriction, genre overlap)
    - Fallback: top rated media if user has no “liked” ratings

**Manual integration testing**
- Postman collection to demonstrate relevant endpoints and flows

**Unit tests**
- JUnit 5 test setup
- Test plan for >= 20 unit tests (core business logic)

---

## Architecture overview (packages/layers)

**config**
- Reads settings from `application.properties`
- `DatabaseConfig`: JDBC connection creation and config validation
- `ServerConfig`: server port and BCrypt rounds

**dto**
- Request DTOs for JSON input  
  (e.g. `RegisterRequest`, `LoginRequest`, `MediaRequest`, `RatingRequest`, `UserProfileUpdate`)

**model**
- Domain models (`User`, `Media`, `Rating`)
- Builder Pattern for readable object construction

**repository**
- Pure JDBC/SQL layer (no HTTP, no business rules)
- CRUD operations + mapping of DB rows to models
- Examples: `UserRepository`, `MediaRepository`, `RatingRepository`, `FavoritesRepository`

**service**
- Business logic and rules from specification:
    - input validation (stars range, required fields)
    - ownership checks (creator/owner restrictions)
    - moderation logic (confirm comment)
    - like uniqueness (1 like per rating/user)
    - recalculation of denormalized statistics

**controller**
- HTTP layer:
    - parses request path + method
    - reads body JSON via Jackson
    - requires Bearer token (except auth endpoints)
    - maps exceptions to correct HTTP status codes
    - returns JSON + correct codes via `HttpUtil`

**server**
- `MrpHttpServer`: route registration and thread pool configuration

---

## Why this structure
- Controller stays focused on request/response only.
- Repository stays focused on SQL only.
- Services keep business logic centralized and testable.

---

# 2) Unit test coverage and why specific logic was tested

## Current implemented tests
**ConfigTest (sanity checks)**
- Server port is in valid range
- DB URL exists and targets PostgreSQL
- BCrypt rounds are not too low

## Why these tests matter
- Without correct configuration the server cannot start.
- These tests catch typical setup mistakes early (wrong port, missing DB URL, weak BCrypt settings).

## Planned unit tests (core business logic, >= 20)
The test plan focuses on the **service layer**, because it contains the real specification rules and is testable without HTTP:

- **AuthServiceTest**: validation, duplicate username, hashing, login success/failure
- **MediaServiceTest**: validation, ownership checks, repository failure handling
- **RatingServiceTest**: stars validation, duplicate rating conflict, ownership, confirm moderation, like rules
- **FavoritesServiceTest**: media existence, duplicate favorites, remove not found
- **RecommendationServiceTest**: fallback behavior, limit behavior, scoring preference checks

This coverage ensures that:
- specification rules (ownership, moderation, 1-like rule, 1-rating rule) are verified
- major error cases return expected exceptions (mapped to 4xx/5xx by controllers)

# 3) Problems encountered and how they were solved

**Docker compose not found**
**Problem**: `docker compose up -d` failed with `no configuration file provided: not found`
**Cause**: compose file was not in project root
**Fix**: use explicit file path
```plaintext
docker compose -f ./docker/docker-compose.yml up -d
```

**Java version mismatch (switch arrow syntax)**
**Problem**: compilation errors when using `case "x" -> ...`
**Cause**: arrow switch requires newer Java than 11
**Fix**: Maven compiler set to Java 17

**`return` inside static initializer**
**Problem**: `return` outside of method in `static{}` block
**Cause**: Java does not allow return in static init blocks
**Fix**: refactored to if/else flow without return

**LocalDateTime JSON serialization**
**Problem**: Jackson could not serialize/deserialize `LocalDateTime`
**Fix**: added dependency `jackson-datatype-jsr310` and registered module in `JsonUtil`

**Like rule and DB consistency**
**Problem**: preventing double likes reliably
**Fix**: added `rating_likes` table with UNIQUE `(rating_id, user_id)` + transaction that inserts like-row and 
increments `likes_count` in one unit

**Repository/service mismatches during development**
**Problem**: missing methods or naming mismatches (e.g. `incrementLikes`)
**Fix**: aligned service logic with repository method `likeRating(...)` and kept repository as single source of truth
for like handling

- - - 

# 4) Estimated time tracking for each major part of the project
- Project setup (repo, IntelliJ, Maven): ~ 3h
- Docker/Postgres setup + schema init: ~ 6h
- Config setup (properties, DatabaseConfig, ServerConfig): ~ 6-7h
- Repository layer (User/Media/Rating/Favorites): ~ 12h
- Auth flow (register, login, token): ~ 8h
- Media endpoints (CRUD, auth protected): ~ 7h
- Rating system (create/update/delete/confirm/like): ~ 9h
- Favorites + user profile/history: ~ 5h
- Recommendations: ~ 5h
- Postman testing + fixes: ~ 3h
- Unit tests (setup + implementation planned): 10h
- Protocol writing + cleanup: ~ 4h

- - - 

# Notes on documentation and submission
- Git history is used as development trace
- Submission includes:
  - Source code (zip)
  - README with GitHub link
  - Postman collection
  - This protocol document

- - -

