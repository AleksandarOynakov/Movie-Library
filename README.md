# Movie Library API

A RESTful Spring Boot application for managing a personal movie catalogue. It supports user registration, session-based authentication, role-based access control, MariaDB persistence, validation, and asynchronous movie metadata enrichment through the OMDb API.

## Features

- Register users with BCrypt-hashed passwords
- Authenticate with a server-side session and `JSESSIONID` cookie
- Read movies as a `USER` or `ADMIN`
- Create, replace, and delete movies as an `ADMIN`
- Validate incoming movie and user data
- Return clear HTTP statuses and error messages
- Enrich missing director, release year, and IMDb rating data asynchronously through OMDb
- Persist users and movies in MariaDB
- Unit and MVC/security integration tests with JUnit 5, Mockito, and MockMvc

## Tech stack

- Java 17
- Spring Boot 4.1
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring RestClient
- MariaDB
- Gradle
- JUnit 5, Mockito, and MockMvc

## Requirements

Before starting, install:

- JDK 17
- MariaDB
- Git
- An OMDb API key from [OMDb API](https://www.omdbapi.com/apikey.aspx)

## Getting started

### 1. Clone the repository

```bash
git clone https://github.com/AleksandarOynakov/Movie-Library.git
cd Movie-Library
```

### 2. Create the database

Open MariaDB and create an empty database:

```sql
CREATE DATABASE movie_library;
```

Hibernate creates and updates the application tables automatically because `spring.jpa.hibernate.ddl-auto=update`.

### 3. Configure environment variables

The application reads database credentials and the OMDb key from environment variables.

macOS/Linux:

```bash
export DB_USERNAME="your_mariadb_username"
export DB_PASSWORD="your_mariadb_password"
export OMDB_API_KEY="your_omdb_api_key"
```

Windows PowerShell:

```powershell
$env:DB_USERNAME="your_mariadb_username"
$env:DB_PASSWORD="your_mariadb_password"
$env:OMDB_API_KEY="your_omdb_api_key"
```

The default database connection is:

```text
jdbc:mariadb://localhost:3306/movie_library
```

To use a different host, port, or database name, update `src/main/resources/application.properties`.

### 4. Run the application

macOS/Linux:

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

The API starts at:

```text
http://localhost:8080
```

## Authentication and roles

Authentication is session-based. Registration creates a user with the `USER` role. Login accepts form fields rather than a JSON body and returns a `JSESSIONID` cookie.

| Action | Anonymous | USER | ADMIN |
|---|:---:|:---:|:---:|
| Register and log in | Yes | Yes | Yes |
| Read movies | No | Yes | Yes |
| Create, replace, or delete movies | No | No | Yes |

For local development, register a user and then promote it to `ADMIN` in MariaDB:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE username = 'admin';
```

Log out and log back in after changing the role so the new authority is stored in a fresh session.

## API reference

| Method | Endpoint | Access | Description                       |
|---|---|---|-----------------------------------|
| `POST` | `/api/auth/register` | Public | Register a new user               |
| `POST` | `/api/auth/login` | Public | Login and create a session        |
| `POST` | `/api/auth/logout` | Authenticated | Logout and invalidate the session |
| `GET` | `/api/movies` | USER, ADMIN | List all movies                   |
| `GET` | `/api/movies/{movieId}` | USER, ADMIN | Get one movie                     |
| `POST` | `/api/movies` | ADMIN | Create a movie                    |
| `PUT` | `/api/movies/{movieId}` | ADMIN | Update a movie                    |
| `DELETE` | `/api/movies/{movieId}` | ADMIN | Delete a movie                    |

### Register

```bash
curl -i -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password"
  }'
```

Example response:

```json
{
  "id": 1,
  "username": "testuser",
  "role": "USER"
}
```

### Login

Use `application/x-www-form-urlencoded` and save the session cookie:

```bash
curl -i -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=testuser&password=password"
```

### List movies

```bash
curl -i -b cookies.txt http://localhost:8080/api/movies
```

### Create a movie

All fields except `title` are optional. Missing metadata is looked up asynchronously through OMDb.

```bash
curl -i -b cookies.txt -X POST http://localhost:8080/api/movies \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Matrix",
    "director": "The Wachowskis",
    "year": "1999",
    "rating": 8.7
  }'
```

A minimal request also works:

```json
{
  "title": "Interstellar"
}
```

The title must not be blank and must contain between 4 and 40 characters.

### Update a movie

Fields omitted from the request may become `null` and can subsequently be enriched from OMDb.

```bash
curl -i -b cookies.txt -X PUT http://localhost:8080/api/movies/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Matrix Reloaded",
    "director": "The Wachowskis",
    "year": "2003",
    "rating": 7.2
  }'
```

### Delete a movie

```bash
curl -i -b cookies.txt -X DELETE http://localhost:8080/api/movies/1
```

### Logout

```bash
curl -i -b cookies.txt -X POST http://localhost:8080/api/auth/logout
```

## HTTP responses

| Status | Meaning |
|---:|---|
| `200 OK` | Successful read, update, login, or logout |
| `201 Created` | User or movie created |
| `204 No Content` | Movie deleted |
| `400 Bad Request` | Request validation failed |
| `401 Unauthorized` | Authentication is required or login failed |
| `403 Forbidden` | The authenticated user does not have the `ADMIN` role |
| `404 Not Found` | Movie does not exist |
| `409 Conflict` | Username or movie title already exists |

Error responses currently use plain text, for example:

```text
Movie with id 99 was not found
```

## OMDb enrichment

When a created or updated movie is missing its director, year, or rating, the application starts an asynchronous OMDb lookup. Only missing values are filled; existing values are not overwritten.

Enrichment failures are logged and do not fail the original API request. Because enrichment runs asynchronously, the first response can contain missing values. Retrieve the movie again to see the enriched data.

## Running tests

macOS/Linux:

```bash
./gradlew test
```

Windows:

```powershell
.\gradlew.bat test
```

The test suite covers service behavior, OMDb enrichment, controller endpoints, and security rules.

## Project structure

```text
src/
|-- main/
|   |-- java/com/example/movielibrary/
|   |   |-- clients/
|   |   |-- configuration/
|   |   |-- controllers/
|   |   |-- exceptions/
|   |   |-- helpers/
|   |   |-- models/
|   |   |-- repositories/
|   |   \-- services/
|   \-- resources/
|       \-- application.properties
\-- test/
    \-- java/com/example/movielibrary/
        |-- controllers/
        |-- security/
        \-- services/
```
