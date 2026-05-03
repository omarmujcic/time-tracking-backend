# Time Tracking App

Spring Boot Java project generated for Maven.

## Requirements

- Java 21
- Maven 3.9+
- PostgreSQL running from the workspace `docker-compose.yml`

## Run

```sh
JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-amzn" mvn spring-boot:run
```

Then open:

```text
http://localhost:8080/health
http://localhost:8080/api/status
```
