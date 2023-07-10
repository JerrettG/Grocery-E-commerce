FROM openjdk:17 as buildstage
WORKDIR /Order-Service
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw package
COPY target/*.jar order-service.jar

FROM openjdk:17
COPY --from=buildstage /Order-Service/order-service.jar .
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=dev","/order-service.jar"]

EXPOSE 8080