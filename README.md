# Fundraiser Management API

This is a Spring Boot RESTful API for managing charity fundraising events and collection boxes. It enables users to register events, track donations in various currencies, assign boxes to events, and convert currencies using a third-party exchange API ([Unirate](https://unirateapi.com)).

---

## Table of Contents

1. [Features](#features)
2. [Getting Started](#getting-started)
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
* Generate financial reports
* Integration with online currency conversion API
* API documentation via Swagger UI
* Input validation and exception handling
* Fully tested with JUnit and Mockito

---

## Getting Started

1. **Clone the repository**

   ```bash
   git clone https://github.com/eryxmiliaris/fundraiser.git
   cd fundraiser
   ```

2. **Set the Unirate API key as an environment variable**

To get a free API key, register at [https://unirateapi.com](https://unirateapi.com). Once registered, you'll find your API key in your account settings.

You can either:

- **Replace** `${UNIRATE_API_KEY}` **directly in** `application.yml` (for quick local testing):

    ```yaml
    currency:
      unirate:
        api-key: your-api-key-here
    ```

- **Or set the** `UNIRATE_API_KEY` **as an environment variable** (recommended):
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

3. **Build and run the application**

   ```bash
   ./mvnw spring-boot:run
   ```

---

## Running Tests

To run the test suite:

```bash
./mvnw test
```

* Unit tests cover services, controllers, conversion client logic, and initialization.
* Mockito and H2 are used for mocking and in-memory DB testing.

---

## API Overview

| Endpoint                                   | Method   | Description                                     |
| ------------------------------------------ | -------- | ----------------------------------------------- |
| `/api/v1/events`                           | `POST`   | Create a new fundraising event                  |
| `/api/v1/events`                           | `GET`    | Get financial report for all events             |
| `/api/v1/boxes`                            | `POST`   | Register a new collection box                   |
| `/api/v1/boxes`                            | `GET`    | List all boxes with assignment and empty status |
| `/api/v1/boxes/{id}`                       | `DELETE` | Unregister a box                                |
| `/api/v1/boxes/{id_box}/events/{id_event}` | `PUT`    | Assign a box to an event                        |
| `/api/v1/boxes/{id}/add-money`             | `PUT`    | Add money to a box                              |
| `/api/v1/boxes/{id}/empty`                 | `POST`   | Transfer box funds to event account             |
| `/api/v1/currencies`                       | `GET`    | List all available currencies                   |

---

## Swagger API Docs

Once the application is running, navigate to:

```
http://localhost:8080/swagger-ui/index.html
```

You can test and explore all endpoints via the Swagger UI.

---

## Project Structure

```
com.vb.fundraiser
├── client              # API client for Unirate currency conversion
├── config              # Configuration beans
├── controller          # REST controllers
├── exception           # Custom exceptions and global handler
├── init                # Currency initializer
├── model
│   ├── common          # Common models
│   ├── dto             # Data Transfer Objects
│   ├── entity          # JPA entities
│   ├── request         # Request payloads
├── repository          # Spring Data JPA repositories
├── service             # Business logic and service layer
└── FundraiserApplication.java
```

---

## Technologies Used

* Java 21
* Spring Boot 3.4.3
* Spring Data JPA + H2 Database
* OpenAPI / Swagger (springdoc-openapi)
* Mockito & JUnit 5
* Maven
* Lombok
