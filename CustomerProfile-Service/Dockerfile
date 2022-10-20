FROM openjdk:17

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} customer-profile-service.jar
ENTRYPOINT ["java","-jar","/customer-profile-service.jar"]

EXPOSE 8080