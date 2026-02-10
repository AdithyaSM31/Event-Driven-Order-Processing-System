# Test Requests for Order Processing System

## 1. Create Inventory (First Time Setup)

```bash
# Add iPhone 15 Pro
curl -X POST "http://localhost:8083/api/inventory?productId=PROD001&productName=iPhone%2015%20Pro&quantity=100"

# Add MacBook Pro
curl -X POST "http://localhost:8083/api/inventory?productId=PROD002&productName=MacBook%20Pro&quantity=50"

# Add AirPods Pro
curl -X POST "http://localhost:8083/api/inventory?productId=PROD003&productName=AirPods%20Pro&quantity=200"
```

## 2. Create an Order

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -H "X-User-Id: john.doe@example.com" \
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

## 3. Create Order with Multiple Items

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -H "X-User-Id: jane.smith@example.com" \
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

## 4. Get Order by ID

```bash
curl http://localhost:8081/api/orders/1
```

## 5. Get All Orders for User

```bash
curl http://localhost:8081/api/orders/user \
  -H "X-User-Id: john.doe@example.com"
```

## 6. Check Inventory

```bash
curl http://localhost:8083/api/inventory/PROD001
```

## 7. Check Payment Status

```bash
curl http://localhost:8082/api/payments/order/1
```

## 8. Test with PowerShell (Windows)

```powershell
# Create Order
Invoke-RestMethod -Uri "http://localhost:8081/api/orders" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"; "X-User-Id"="user123"} `
  -Body '{"items":[{"productId":"PROD001","productName":"iPhone 15 Pro","quantity":1,"price":999.99}]}'

# Get Order
Invoke-RestMethod -Uri "http://localhost:8081/api/orders/1" -Method GET
```

## Expected Event Flow

After creating an order, watch the logs:

```bash
docker-compose logs -f
```

You should see:
1. **Order Service**: Order created (status: PENDING)
2. **Notification Service**: "Order Created" notification sent
3. **Payment Service**: Processing payment...
4. **Payment Service**: Payment succeeded/failed
5. **Notification Service**: "Payment Success/Failed" notification sent
6. **Order Service**: Status updated to PAYMENT_CONFIRMED
7. **Inventory Service**: Reserving stock...
8. **Inventory Service**: Stock reserved
9. **Notification Service**: "Order Confirmed" notification sent
10. **Order Service**: Status updated to CONFIRMED

## Testing Payment Failures

The Payment Service has a 90% success rate. To see failures:
- Create multiple orders (10-15)
- Approximately 1-2 will fail payment
- Check logs to see failure handling

## Testing Inventory Unavailability

```bash
# Try to order more than available stock
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -H "X-User-Id: test@example.com" \
  -d '{
    "items": [
      {
        "productId": "PROD002",
        "productName": "MacBook Pro",
        "quantity": 100,
        "price": 2499.99
      }
    ]
  }'
```

This will trigger an "Inventory Unavailable" event.
