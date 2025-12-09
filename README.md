# Media Ratings Platform API (MRP)

Standalone RESTful backend application for managing and rating media content.
Built as a Java project for the FH Technikum Vienna course (WS 2025).

- - -

This project is a REST-based HTTP server that exposes an API for potential frontends
(Web, Mobile, Console). The frontend itself is **not** part of this project (at least currently).\

The platform allows users to:

- register and log in with unique credentials
- manage media entries (CRUD)
- rate media (1-5 stars) and write comments
- like other users' ratings
- mark media as favorites
- view personal statistics and history
- receive content recommendations based on previous ratings and content simililarity

Data is stored in a **PostgreSQL** database and accessed via Java code.
Authentication /authorization is handled with **token-based securiy** using HTTP Bearer tokens.

## Features

### User Management

- User registration with unique username & password
- Login endpoint returning a token (e.g. `username-mrpToken`)
- Authentication via HTTP `Authorization: Bearer <token>`
- View and edit user profile
- Personal statistics (e.g. total ratings, average score, favorite genre)
- Public leaderboard of the most active users

### Media Management

A **media entry** represents a:

- movie
- series
- or game

Each media entry contains:

- title
- description
- media type (movie / series / game)
- release year
- genre(s)
- age restriction
- list of ratings
- calculated average score

Only the **creator** of a media entry can update or delete it.

Supported operations:

- Create media (POST)
- Read media (GET)
- Update media (PUT/PATCH)
- Delete media (DELETE)
- Search media by title (partial matching)
- Filter by genre, media type, year, age restriction, rating
- Sort by title, year, or score
- Mark/unmark media as favorites

### Ratings & Likes

A rating:

- belongs to exactly one user and one media entry
- contains:
    - star value (1–5)
    - optional comment
    - timestamp
- can be edited or deleted by its creator
- can be liked by other users (max. 1 like per rating)
- must be confirmed by the creator before the comment becomes publicly visible  
  (moderation feature – ratings without confirmation are not shown publicly)

Users can also view:

- their complete rating history
- their list of favorite media entries

### Recommendations

The server can return recommendations based on:

- genre similarity to previously highly rated media
- content similarity (matching genres, media type, and age restriction)

---

## Technical Stack

Planned stack:

- **Language:** Java (17+)
- **Build tool:** Maven or Gradle (TBD)
- **Database:** PostgreSQL
- **HTTP layer:** Java HTTP server / lightweight helper (no Spring, no ASP.NET, no JSP/JSF)
- **JSON serialization:** e.g. Jackson
- **Testing:** JUnit (unit tests)

Frameworks like Spring, ASP.NET, JSP/JSF are **not allowed** according to the course specification.

---

## HTTP & Authorization

The API follows standard HTTP semantics and returns correct status codes:

- `2xx` – success
- `4xx` – client errors (invalid input, missing/invalid authentication, forbidden, not found, …)
- `5xx` – server errors (e.g. database unavailable)

### Login Example

```http
POST /api/users/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "Username": "mustermann",
  "Password": "max"
}
```