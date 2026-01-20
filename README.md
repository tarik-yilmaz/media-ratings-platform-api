# Media Ratings Platform API (MRP)

Standalone RESTful backend application for managing and rating media content.  
Built as a Java project for the FH Technikum Vienna course **SWEN1 (WS 2025)**.

This project provides a REST-based HTTP server that exposes an API for potential frontends
(Web, Mobile, Console). The frontend itself is **not** part of this project.

**Link for GitHub Repository:**
https://github.com/tarik-yilmaz/media-ratings-platform-api

---

## Features (Overview)

The platform allows users to:

- register and log in with unique credentials
- manage media entries (CRUD)
- rate media (1–5 stars) and optionally write comments
- like other users’ ratings (max. 1 like per user per rating)
- mark media entries as favorites
- view personal profile, statistics, and rating history
- receive content recommendations based on previous ratings and content similarity

Data is persisted in **PostgreSQL** (via JDBC).  
Authentication/authorization is handled with **token-based security** using Bearer tokens.

---

## User Management

- User registration with unique username
- Login endpoint returning a token (stored server-side and mapped to a userId)
- Authentication via HTTP header: `Authorization: Bearer <token>`
- View and edit user profile
- Profile includes statistics (e.g., total ratings, average rating)
- Public leaderboard of the most active users (sorted by rating count)

---

## Media Management

A **media entry** represents a:

- movie
- series
- game

Each entry contains:

- title
- description
- media type (MOVIE / SERIES / GAME)
- release year
- genre(s)
- age restriction
- calculated average score

Ownership rule:
- Only the **creator** of a media entry can update or delete it.

Supported operations:

- Create media (POST)
- Read media (GET)
- Update media (PUT)
- Delete media (DELETE)
- Search by title (partial match)
- Filter by genre, media type, release year, age restriction, minimum rating
- Sort by title, year, or score
- Mark/unmark media as favorites

---

## Ratings, Comment Moderation & Likes

A rating:

- belongs to exactly one user and one media entry
- contains:
    - star value (1–5)
    - optional comment
    - timestamp
- can be edited or deleted by its creator
- can be liked by other users (max. 1 like per user per rating)
- includes a moderation rule:
    - comments are **not publicly visible** until the author confirms them

Users can also view:

- their rating history
- their list of favorite media entries

---

## Recommendations

The server can return recommendations based on:

- genre similarity to previously highly rated media (e.g., >= 4 stars)
- content similarity (media type, age restriction, genre overlap)
- fallback: if no “liked” ratings exist, the server can return top-rated media

---

## Technical Stack

- **Language:** Java 17
- **Build tool:** Maven
- **Database:** PostgreSQL (Docker)
- **HTTP layer:** `com.sun.net.httpserver.HttpServer` (no Spring/JSP/JSF)
- **JSON serialization:** Jackson (`jackson-databind`, `jackson-datatype-jsr310`)
- **Password hashing:** BCrypt (`jbcrypt`)
- **Testing:** JUnit 5

Frameworks like Spring / JSP / JSF are **not allowed** per course specification.

---

## Setup & Run

### 1) Start database (Docker)

From the project root:

```bash
docker compose -f ./docker/docker-compose.yml up -d
```

### Check staus

```bash
docker compose -f ./docker/docker-compose.yml ps
```

### Stop (keep data)

```bash
docker compose -f ./docker/docker-compose.yml down
```

### Reset (delete volumes/data)

```bash
docker compose -f ./docker/docker-compose.yml down -v
docker compose -f ./docker/docker-compose.yml up -d
```

### 2) Run the server

Start the application via IntelliJ or Maven. Example:

```bash
mvn test
mvn package
```

Server starts on the configured port and prints:
- `HTTP Server läuft`
- `Base URL: http://localhost:<port>/api`

- - -

## HTTP & Authorization

The API follows standard HTTP semantics and returns correct status codes:
- `2xx` - success
- `4xx` - client errors (e.g., invalid request)
- `5xx` - server errors (e.g., database unavailable)

### Login Example

```plaintext
POST /api/users/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "username": "mustermann",
  "password": "max"
}
```

**Response (example):**
```plaintext
{ "token": "..." }
```

For subsequent requests:
```plaintext
Authorization: Bearer <token>
```

## Manual API Testing (Postman)
A Postman collection is included (`MRP_Postman_Collection.json`).

Recommended flow:
**1. Register**
- `POST /api/users/register`
**2. Login**
- `POST /api/users/login`
**3. Copy token and use it for all protected endpoints**:
- `Authorization: Bearer <token>`
**4. Test Media CRUD, Ratings, Likes, Favorites, Profile, Leaderboard, Recommendations**

