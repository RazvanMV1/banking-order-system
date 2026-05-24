# Banking Order System - V1

Order processing system built with Spring Boot and Docker.
This is V1 - without Virtual Threads. V2 will target 20.000 req/min using Java 21 Virtual Threads.

## What it does

Two microservices that communicate via HTTP:

- **OrderGateway** (port 8080) - receives a list of order IDs and sends them to OrderProcessor
- **OrderProcessor** (port 8081) - reads orders from DB, processes them and handles reversals

## Tech Stack

- Java 21
- Spring Boot 4.0.6
- PostgreSQL 16
- OkHttp
- Docker + Docker Compose
- Gatling (Performance Tests)
- Swagger

## How To Run

```bash
git clone https://github.com/RazvanMV1/banking-order-system.git
cd banking-order-system
docker-compose up --build
```

Swagger UI:
- Gateway: http://localhost:8080/swagger-ui/index.html
- Processor: http://localhost:8081/swagger-ui/index.html

## Business Logic

When processing an order two things can happen:

- **Correct amount** - order gets marked as PROCESSED
- **Wrong amount** - original order gets REVERSED, a new correct order is created

## Roadmap

- [x] OrderProcessor + OrderGateway
- [x] Docker Compose setup
- [x] Reversal logic with atomic transactions
- [x] Optimistic Locking
- [x] Performance tests with Gatling
- [ ] V2 - Virtual Threads
