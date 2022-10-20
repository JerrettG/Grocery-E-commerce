FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} cart-service.jar
ENTRYPOINT ["java","-jar","/cart-service.jar"]

EXPOSE 8083