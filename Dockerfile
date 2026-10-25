# Stage 1: Build WAR with Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
# List target to verify WAR creation in build logs
RUN ls -la /app/target/

# Stage 2: Tomcat Container
FROM tomcat:10.1-jdk17-temurin

# Disable Tomcat internal shutdown port to avoid Render probe warnings
RUN sed -i 's/<Server port="8005" shutdown="SHUTDOWN">/<Server port="-1" shutdown="SHUTDOWN">/g' /usr/local/tomcat/conf/server.xml

# Clear all default apps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the generated WAR directly as ROOT.war
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Verify ROOT.war exists inside webapps
RUN ls -la /usr/local/tomcat/webapps/

EXPOSE 8080
CMD ["catalina.sh", "run"]