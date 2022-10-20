FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} discovery-server.jar
ENTRYPOINT ["java","-jar","/discovery-server.jar"]

EXPOSE 8761