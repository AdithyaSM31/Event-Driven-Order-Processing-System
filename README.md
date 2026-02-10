# Event-Driven Order Processing System

A production-ready microservices-based e-commerce order processing system built with **Spring Boot**, **Apache Kafka**, and **Spring Cloud**. This system demonstrates modern event-driven architecture patterns with asynchronous communication between services.

## 🏗️ Architecture Overview

```
                                    ┌─────────────────┐
                                    │  API Gateway    │
                                    │   (Port 8080)   │
                                    │  + JWT Auth     │
                                    └────────┬────────┘
                                             │
                        ┌────────────────────┼────────────────────┐
                        │                    │                    │
                 ┌──────▼──────┐     ┌──────▼──────┐     ┌──────▼──────┐
                 │    Order     │     │   Payment   │     │  Inventory  │
                 │   Service    │     │   Service   │     │   Service   │
                 │ (Port 8081)  │     │ (Port 8082) │     │ (Port 8083) │
                 └──────┬───────┘     └──────┬──────┘     └──────┬──────┘
                        │                    │                    │
                        └────────────────────┼────────────────────┘
                                             │
                                   ┌─────────▼──────────┐
                                   │   Apache Kafka     │
                                   │  Message Broker    │
                                   └─────────┬──────────┘
                                             │
                                   ┌─────────▼──────────┐
                                   │   Notification     │
                                   │     Service        │
                                   │   (Port 8084)      │
                                   └────────────────────┘
```

### Supporting Infrastructure

- **Eureka Server** (Port 8761): Service Discovery
- **PostgreSQL** (Port 5432): Persistent storage for orders, payments, inventory
- **Redis** (Port 6379): Caching layer for frequently accessed data
- **Kafka + Zookeeper** (Ports 9092, 2181): Event streaming platform

## ✨ Key Features

### Microservices Architecture
- **Order Service**: Manages order lifecycle (PENDING → PAYMENT_CONFIRMED → CONFIRMED)
- **Payment Service**: Processes payments with 90% simulated success rate
- **Inventory Service**: Reserves and manages product stock
- **Notification Service**: Sends email/SMS notifications for all order events

### Event-Driven Communication
- **Asynchronous messaging** via Apache Kafka topics
- **Loose coupling** between services
- **Event replay capability** for debugging and recovery
- **Scalable** - each service scales independently

### Production-Ready Patterns
- **API Gateway**: Single entry point with JWT authentication
- **Service Discovery**: Eureka for dynamic service registration
- **Caching**: Redis for performance optimization
- **Database per Service**: PostgreSQL instances for data isolation
- **Health Checks**: Actuator endpoints for monitoring

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **Docker** and **Docker Compose**
- **Git**

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd "Event Driven Order Processing System"
```

### 2. Build All Services

**Windows (PowerShell):**
```powershell
./build.ps1
```

**Linux / macOS:**
```bash
chmod +x build.sh
./build.sh
```

These scripts use the included Maven Wrapper to automatically download the correct Maven version, compile all 6 microservices, and create executable JAR files.

### 3. Start Infrastructure with Docker Compose

```bash
docker-compose up -d
```

This starts:
- PostgreSQL with 3 databases (orderdb, paymentdb, inventorydb)
- Redis cache
- Kafka + Zookeeper
- All 6 microservices

### 4. Verify Services are Running

**Check Eureka Dashboard:**
```
http://localhost:8761
```
All services should be registered within 1-2 minutes.

**Check Service Health:**
```bash
curl http://localhost:8081/actuator/health  # Order Service
curl http://localhost:8082/actuator/health  # Payment Service
curl http://localhost:8083/actuator/health  # Inventory Service
curl http://localhost:8084/actuator/health  # Notification Service
```

## 📊 Service Ports

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | Main entry point (JWT required) |
| Eureka Server | 8761 | Service Discovery Dashboard |
| Order Service | 8081 | Order management |
| Payment Service | 8082 | Payment processing |
| Inventory Service | 8083 | Stock management |
| Notification Service | 8084 | Email/SMS notifications |
| PostgreSQL | 5432 | Database |
| Redis | 6379 | Cache |
| Kafka | 9092 | Message broker |

## 🧪 Testing the System

### Step 1: Generate JWT Token

For testing purposes, you can bypass JWT by calling services directly (ports 8081-8084), or use a token generator.

**Test without JWT (Direct Service Call):**
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user123" \
  -d '{
    "items": [
      {
        "productId": "PROD001",
        "productName": "iPhone 15 Pro",
        "quantity": 1,
        "price": 999.99
      }
    ]
  }'
```

### Step 2: Create Inventory (First Time Setup)

Before placing orders, add products to inventory:

```bash
curl -X POST "http://localhost:8083/api/inventory?productId=PROD001&productName=iPhone%2015%20Pro&quantity=100"
curl -X POST "http://localhost:8083/api/inventory?productId=PROD002&productName=MacBook%20Pro&quantity=50"
curl -X POST "http://localhost:8083/api/inventory?productId=PROD003&productName=AirPods%20Pro&quantity=200"
```

### Step 3: Place an Order

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -H "X-User-Id: john.doe@example.com" \
  -d '{
    "items": [
      {
        "productId": "PROD001",
        "productName": "iPhone 15 Pro",
        "quantity": 2,
        "price": 999.99
      },
      {
        "productId": "PROD003",
        "productName": "AirPods Pro",
        "quantity": 1,
        "price": 249.99
      }
    ]
  }'
```

**Expected Response:**
```json
{
  "id": 1,
  "userId": "john.doe@example.com",
  "status": "PENDING",
  "totalAmount": 2249.97,
  "items": [...],
  "createdAt": "2026-02-09T10:30:00",
  "updatedAt": "2026-02-09T10:30:00"
}
```

### Step 4: Watch the Event Flow

**Check Docker logs** to see the event-driven flow:

```bash
# Watch all services
docker-compose logs -f

# Watch specific service
docker-compose logs -f order-service
docker-compose logs -f payment-service
docker-compose logs -f inventory-service
docker-compose logs -f notification-service
```

**You'll see:**
1. ✅ Order Service: Order created (status: PENDING)
2. ✅ Payment Service: Payment processing → Success/Failed
3. ✅ Order Service: Status updated to PAYMENT_CONFIRMED
4. ✅ Inventory Service: Stock reserved
5. ✅ Order Service: Status updated to CONFIRMED
6. ✅ Notification Service: Emails sent at each step

### Step 5: Query Order Status

```bash
# Get order by ID
curl http://localhost:8081/api/orders/1

# Get all orders for user
curl http://localhost:8081/api/orders/user -H "X-User-Id: john.doe@example.com"
```

### Step 6: Check Inventory

```bash
curl http://localhost:8083/api/inventory/PROD001
```

## 🔄 Event Flow Example

When a customer places an order for an iPhone:

```
1. POST /api/orders → Order Service
   └─ Creates order (status: PENDING)
   └─ Publishes: OrderCreatedEvent → Kafka

2. Payment Service (listens to OrderCreatedEvent)
   └─ Processes payment (90% success rate)
   └─ Publishes: PaymentSucceededEvent → Kafka

3. Order Service (listens to PaymentSucceededEvent)
   └─ Updates order status to PAYMENT_CONFIRMED

4. Inventory Service (listens to PaymentSucceededEvent)
   └─ Checks stock availability
   └─ Reserves inventory
   └─ Publishes: InventoryReservedEvent → Kafka

5. Order Service (listens to InventoryReservedEvent)
   └─ Updates order status to CONFIRMED

6. Notification Service (listens to ALL events)
   └─ Sends "Order Created" email
   └─ Sends "Payment Success" email
   └─ Sends "Order Confirmed" email
```

## 🛠️ Development

### Running Services Locally (Without Docker)

1. **Start Infrastructure:**
```bash
# Start only Postgres, Redis, Kafka
docker-compose up -d postgres redis zookeeper kafka
```

2. **Run Services in IDE or Terminal:**
```bash
# Terminal 1: Eureka Server
cd eureka-server
mvn spring-boot:run

# Terminal 2: API Gateway
cd api-gateway
mvn spring-boot:run

# Terminal 3: Order Service
cd order-service
mvn spring-boot:run

# Terminal 4: Payment Service
cd payment-service
mvn spring-boot:run

# Terminal 5: Inventory Service
cd inventory-service
mvn spring-boot:run

# Terminal 6: Notification Service
cd notification-service
mvn spring-boot:run
```

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific service
cd order-service
mvn test

# Run with coverage report
mvn test jacoco:report
```

## 📦 Kafka Topics

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `order-created-topic` | Order Service | Payment, Notification | Order placement event |
| `payment-succeeded-topic` | Payment Service | Order, Inventory, Notification | Successful payment |
| `payment-failed-topic` | Payment Service | Order, Notification | Failed payment |
| `inventory-reserved-topic` | Inventory Service | Order, Notification | Stock reserved |
| `inventory-unavailable-topic` | Inventory Service | Order, Notification | Stock unavailable |

## 🐛 Troubleshooting

### Services not registering with Eureka
- Wait 1-2 minutes for initial registration
- Check `docker-compose logs eureka-server`
- Verify network connectivity: `docker network inspect microservices-network`

### Kafka connection errors
- Ensure Kafka is healthy: `docker-compose ps kafka`
- Check Kafka logs: `docker-compose logs kafka`
- Verify topics exist: `docker exec kafka kafka-topics --list --bootstrap-server localhost:9092`

### Database connection errors
- Check Postgres is running: `docker-compose ps postgres`
- Verify databases created: `docker exec postgres psql -U postgres -c "\l"`
- Check connection settings in `application.yml`

### Order stuck in PENDING status
- Check Payment Service logs: `docker-compose logs payment-service`
- Verify Kafka messages: `docker-compose logs kafka`
- Check if payment event was published

### Serialization/Deserialization Errors (ClassNotFoundException)
If you see `ClassNotFoundException` in service logs:
- Ensure the `spring.json.type.mapping` property in `application.yml` correctly maps the event class name to the full package path.
- This is required because services share event names but define them in different packages.

### Docker Container Health Check Failures
If containers are Unhealthy:
- Ensure `curl` is installed in the Docker image (base image `eclipse-temurin:17-jre-alpine` requires manual installation or a custom build step).
- **Note:** The current `Dockerfile` installs `curl` automatically.

## 🧹 Cleanup

```bash
# Stop all services
docker-compose down

# Remove volumes (deletes all data)
docker-compose down -v

# Remove everything including images
docker-compose down -v --rmi all
```

## 📈 Performance Considerations

- **Redis Caching**: Frequently accessed orders and inventory cached with TTL
- **Kafka Partitioning**: Topics partitioned for parallel processing
- **Database Indexing**: Indexes on `userId`, `productId`, `orderId`
- **Connection Pooling**: HikariCP for efficient DB connections

## 🔐 Security

- **JWT Authentication**: API Gateway validates tokens
- **Service-to-Service**: Internal services communicate without auth (in private network)
- **Environment Variables**: Sensitive data externalized
- **SQL Injection Prevention**: JPA/Hibernate prepared statements

## 🚀 Production Deployment

For production, consider:
- **Kubernetes**: Replace Docker Compose with K8s manifests
- **API Rate Limiting**: Add rate limiters to API Gateway
- **Distributed Tracing**: Integrate Zipkin/Jaeger
- **Centralized Logging**: Add ELK stack
- **Monitoring**: Prometheus + Grafana
- **Secret Management**: Vault or AWS Secrets Manager
- **Database Backups**: Automated backup strategy
- **SSL/TLS**: Enable HTTPS everywhere

## 📚 Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.2 |
| Service Discovery | Spring Cloud Eureka |
| API Gateway | Spring Cloud Gateway |
| Messaging | Apache Kafka |
| Database | PostgreSQL 15 |
| Cache | Redis 7 |
| Security | Spring Security + JWT |
| Containerization | Docker + Docker Compose |
| Build Tool | Maven |
| Testing | JUnit 5 + Mockito |

## 📄 License

This project is licensed under the MIT License.

## 👥 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

**Built with ❤️ using Spring Boot and Event-Driven Architecture**
