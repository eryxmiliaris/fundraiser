# Fundraiser Management API

This is a Spring Boot RESTful API for managing charity fundraising events and collection boxes. It enables users to register events, track donations in various currencies, assign boxes to events, and convert currencies using a third-party exchange API ([Unirate](https://unirateapi.com)).

---

## Table of Contents

1. [Features](#features)
2. [Getting Started & Running](#getting-started--running)
3. [Running Tests](#running-tests)
4. [API Overview](#api-overview)
5. [Swagger API Docs](#swagger-api-docs)
6. [Project Structure](#project-structure)
7. [Technologies Used](#technologies-used)

---

## Features

* Register and manage fundraising events
* Create, assign, and track collection boxes
* Add and convert monetary donations in different currencies
* Auto-convert funds to the event’s currency on box emptying
* Generate financial reports (JSON and HTML)
* Integration with online currency conversion API
* API documentation via Swagger UI
* Input validation and exception handling
* Fully tested with JUnit and Mockito

---

## Getting Started & Running

### 1. Clone the Repository

```bash
git clone https://github.com/eryxmiliaris/fundraiser.git
cd fundraiser
```

### 2. Set the Unirate API Key

To get a free API key, register at [https://unirateapi.com](https://unirateapi.com). Once registered, you'll find your API key in your account settings.

You can either:

A) **Replace** `${UNIRATE_API_KEY}` **directly in** `application.yml` (for quick local testing):

```yaml
currency:
  unirate:
    api-key: your-api-key-here
```

B) **Or set the** `UNIRATE_API_KEY` **as an environment variable** (recommended):

* On macOS/Linux:

```bash
export UNIRATE_API_KEY=your_actual_key
```

* On Windows (CMD):

```cmd
set UNIRATE_API_KEY=your_actual_key
```

* On Windows (PowerShell):

```powershell
$env:UNIRATE_API_KEY = "your_actual_key"
```

### 3. Run the Application

#### Option A: Using Maven

```bash
./mvnw spring-boot:run
```

To preload test data with dev profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Then call:

```http
POST http://localhost:8080/api/v1/test-data
```

#### Option B: Using JAR

If you prefer running as a standalone JAR:

```bash
./mvnw clean package
java -jar target/fundraiser-0.0.1-SNAPSHOT.jar
```

To preload test data with dev profile:

```bash
java -jar target/fundraiser-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

Then call:

```http
POST http://localhost:8080/api/v1/test-data
```

## Running Tests

To run the test suite:

```bash
./mvnw test
```

* Unit tests cover services, controllers, conversion client logic, and initialization.
* Mockito and H2 are used for mocking and in-memory DB testing.

---


## API Overview

| Endpoint                              | Method   | Description                                     |
| ------------------------------------- | -------- | ----------------------------------------------- |
| `/api/v1/events`                      | `GET`    | Get financial report for all events             |
| `/api/v1/events/table`                | `GET`    | Get financial report as styled HTML             |
| `/api/v1/events`                      | `POST`   | Create a new fundraising event                  |
| `/api/v1/boxes`                       | `GET`    | List all boxes with assignment and empty status |
| `/api/v1/boxes`                       | `POST`   | Register a new collection box                   |
| `/api/v1/boxes/{id}`                  | `DELETE` | Unregister a box                                |
| `/api/v1/boxes/{id}/assign?eventId=x` | `PATCH`  | Assign a box to an event                        |
| `/api/v1/boxes/{id}/add-money`        | `PUT`    | Add money to a box                              |
| `/api/v1/boxes/{id}/empty`            | `POST`   | Transfer box funds to event account             |
| `/api/v1/currencies`                  | `GET`    | List all available currencies                   |
| `/api/v1/test-data`                   | `POST`   | Load pre-configured test data into the system   |

---

## Swagger API Docs

Once the application is running, navigate to:

```
http://localhost:8080/swagger-ui/index.html
```

You can test and explore all endpoints via the Swagger UI.

---

## Technologies Used

* Java 21
* Spring Boot 3.4.3
* Spring Data JPA + H2 Database
* OpenAPI / Swagger (springdoc-openapi)
* Mockito & JUnit 5
* Maven
* Lombok
