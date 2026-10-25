# Stage 1: Build the WAR file using Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Deploy into Apache Tomcat 10.1
FROM tomcat:10.1-jdk17-temurin

# Disable Tomcat internal shutdown port (set port="8005" to port="-1")
# This prevents Render health check probes from triggering "Invalid shutdown command" warnings
RUN sed -i 's/<Server port="8005" shutdown="SHUTDOWN">/<Server port="-1" shutdown="SHUTDOWN">/g' /usr/local/tomcat/conf/server.xml

# Clear default webapps and deploy our artifact as the root application
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]