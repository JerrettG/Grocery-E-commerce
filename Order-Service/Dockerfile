FROM openjdk:17 as buildstage
WORKDIR /Order-Service
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw package
COPY target/*.jar orderEntity-service.jar

FROM openjdk:17
COPY --from=buildstage /Order-Service/orderEntity-service.jar .
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/orderEntity-service.jar"]

EXPOSE 8087