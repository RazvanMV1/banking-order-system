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

## Performance Results

Tested with Gatling, ramping up to 200 req/s over 90 seconds.

- Throughput: 115 req/s
- Mean response time: 16ms
- 99th percentile: 15ms
- Success rate: 99.99%

Good baseline results but the system starts to struggle at higher concurrency because standard OS threads are expensive and block while waiting for HTTP responses.

## Roadmap

- [x] OrderProcessor + OrderGateway
- [x] Docker Compose setup
- [x] Reversal logic with atomic transactions
- [x] Optimistic Locking
- [x] Performance tests with Gatling
- [ ] V2 - Virtual Threads

---

# Banking Order System - V2

V2 builds on top of V1 with a focus on concurrency and performance. The goal was to reach 20.000 req/min using Java 21 Virtual Threads without changing the business logic.

## What changed

The main bottleneck in V1 was that every incoming request occupied a real OS thread while waiting for HTTP responses from OrderProcessor. At high concurrency, the thread pool was exhausted and requests started queuing up.

In V2, both services use Virtual Threads via TomcatProtocolHandlerCustomizer. This means Tomcat creates a lightweight Virtual Thread for each request instead of borrowing one from a fixed pool. When a Virtual Thread waits for I/O, it gets parked and the underlying OS thread is freed for other work.

OkHttp was also reconfigured. The default dispatcher only allows 5 simultaneous connections per host which was a hard limit at 333 req/s. We raised it to 2000 and tuned the connection pool to reuse existing TCP connections instead of opening new ones on every request.

HikariCP pool size was increased from 10 to 200 and PostgreSQL max_connections raised to 500 to match the increased concurrency on the database layer.

Retry logic with exponential backoff was added to handle Optimistic Locking conflicts gracefully. If two requests try to process the same order at the same time, the loser retries up to 3 times with delays of 100ms, 200ms and 400ms instead of failing immediately.

Both services now have a Global Exception Handler that returns proper HTTP status codes instead of raw stack traces.

## Tech Stack

- Java 21 + Virtual Threads
- Spring Boot 4.0.6
- PostgreSQL 16
- OkHttp 4.12 (Dispatcher with 2000 max connections)
- HikariCP (200 connection pool)
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

Same as V1. When processing an order two things can happen:

- **Correct amount** - order gets marked as PROCESSED
- **Wrong amount** - original order gets REVERSED, a new correct order is created

On top of that, if two requests hit the same order at the same time, Optimistic Locking detects the conflict and the retry mechanism resolves it automatically.

## Performance Results

Tested with Gatling, ramping up to 333 req/s over 30 seconds then holding steady for 60 seconds.

- Throughput: 277 req/s
- Mean response time: 11ms
- 75th percentile: 11ms
- 95th percentile: 16ms
- 99th percentile: 49ms
- Success rate: 100%

That is a 141% improvement in throughput compared to V1, with a lower mean response time and zero errors across 24.990 requests.

## Consistency Test

After the load test we verified the database state directly in PostgreSQL.

- 0 duplicate processed orders
- 0 data corruption
- 0 failed transactions
- All optimistic locking conflicts were resolved automatically by the retry logic

The system processed every request exactly once, even under heavy concurrency.

## Roadmap

- [x] OrderProcessor + OrderGateway
- [x] Docker Compose setup
- [x] Reversal logic with atomic transactions
- [x] Optimistic Locking
- [x] Performance tests with Gatling
- [x] V2 - Virtual Threads
- [x] Retry logic with exponential backoff
- [x] Global error handling
- [x] HikariCP + PostgreSQL tuning
- [x] Consistency test at 277 req/s
