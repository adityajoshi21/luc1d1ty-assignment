# Prerequisities
JDK 18
Docker
Using Jackson cos Lombok doesnt support camel_case by default

# How bring the mockservice up
cd mockserver  
docker compose up  
the mock server will start at port 1080

# How bring the service up
./mvnw clean install -DskipTests  
java -jar target/simple-springboot-app-0.0.1-SNAPSHOT.jar  
The server will start at port 9001

# How to run the tests
./mvnw test  
