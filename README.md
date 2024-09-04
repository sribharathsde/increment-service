
# Increment Service
Webservice which accepts requests to increment a value associated with a given key. 
The service syncs its state to Postgres DB at least every 10 seconds

This increment Service is developed using Java 21, Spring Boot 3, Redis, PostgreSQL, Bucket4j (Rate Limiting), Auth0, Prometheus

## Features

- Increases the value by keeping it in memory (Redis) for 10 seconds, then pushes the sum to PostgreSQL.
- Redis key expiration listener to handle expired keys.
- Logging of incoming requests.
- Rate limiting implemented using Bucket4j. By default application is configured to handle 20 request per minute. You can configure rate-limit.periods & rate-limit.requests in application.yaml file.
- Secured using OAuth2 protocol, integrated with Auth0 for authentication.
- Custom metrics exposed via Prometheus to expose application metrics.

## Prerequisites

- Java 21
- Maven
- Docker and Docker Compose

## Setup and Running the Application

### 1. Clone the Repository
```bash
git clone https://github.com/sribharathsde/increment-service.git
```

### 2. Build the Application
```bash
./mvn clean install
```

### 3. Running the Application
You can run the application using Docker Compose. Go to the project directory, open terminal, and type the following command:
Once all the 5 containers are up, you can proceed to the next point to accessing the application
```bash
docker-compose up --build
```

### 4. Accessing the Application
You can access the application using the following endpoints:
```bash
Generate the bearer token: curl --location 'https://dev-sbl1fbl5jtmx30jo.us.auth0.com/oauth/token' --header 'content-type: application/json' --header 'Cookie: did=s%3Av0%3Afaa77c36-c5e4-4e02-8aa9-995c88398c35.CGkMvXG0%2BhZYxGX%2BSmdDzsHS41caaB2xjc34SGznPS8; did_compat=s%3Av0%3Afaa77c36-c5e4-4e02-8aa9-995c88398c35.CGkMvXG0%2BhZYxGX%2BSmdDzsHS41caaB2xjc34SGznPS8' --data '{"client_id":"cObOzm8XGfB0NEhPbFhfHYg8O90jRf09","client_secret":"ezqXoXMmz1RCARtiFIETQHfWn3cET5DOtV4ZyzxyvrHho9IbE-B8eibxyJO5eg3V","audience":"http://increment.api","grant_type":"client_credentials"}'
```
```bash
Invoke increment endpoint with token: curl --location 'localhost:3333/api/increment' --header 'Content-Type: application/json' --header 'Authorization: Bearer {token}' --data '{"key":"test","value":100}' 
```

### 6. Accessing the Database
To view or manage the data stored in PostgreSQL, you can use a database client like DBeaver. Here are the details you need to connect:
```bash
    URL: localhost
    Port: 5432
    Database: increment_db
    Username: user
    Password: pwd
```

### 7. Rate Limiting
Rate limiting is implemented using Bucket4j. The `RateLimitFilter` excludes requests to the `/actuator` endpoints 
and limits other requests based on the client's IP address. f a client sends more than 5 requests within one minute, 
the application will respond with an HTTP 429 Too Many Requests status code.

### 6. Accessing Prometheus to View Metrics
To view the metrics, you can access the following endpoint in your browser:
```bash
Metrics Endpoint: http://localhost:9090
```
To visualize the metrics, you can use the following Prometheus queries and create graphs:
```bash
 - Total Number of HTTP Requests: sum(http_server_requests_seconds_count{uri!~"/actuator.*|.*UNKNOWN.*"})
 - Total Number of Error Responses: sum by (uri) (http_server_requests_seconds_count{uri!~"/actuator.*|.*UNKNOWN.*"})
 - Total number of requests grouped by HTTP status code: sum by (status) (http_server_requests_seconds_count{uri!~"/actuator.*|.*UNKNOWN.*"})
 - Total number of requests grouped by HTTP method: sum by (method) (http_server_requests_seconds_count{uri!~"/actuator.*|.*UNKNOWN.*"})
 - average duration of requests for each endpoint URI: avg by (uri) (http_server_requests_seconds_sum{uri!~"/actuator.*|.*UNKNOWN.*"} / http_server_requests_seconds_count{uri!~"/actuator.*|.*UNKNOWN.*"})
 - Track the rate of error responses: sum by (status) (http_server_requests_seconds_count{status=~"4..|5..", uri!~"/actuator.*|.*UNKNOWN.*"})
```


### 7. Steps to login to Auth0
Login to autho portal using provided url and credential.
```bash
    Url: https://auth0.com
    Username: Please let me know which email address should I send the invite to. This is to view the setup on Auth0. Please note that logging into Auth0 is not needed to start the applicaiton because it is already preconfigured to run.
    
 ```

### 8. Stop the Application
```bash
docker-compose down
...