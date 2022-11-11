FROM openjdk:17 as buildstage
WORKDIR /Product-Service
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw package
COPY target/*.jar productEntity-service.jar

FROM openjdk:17
COPY --from=buildstage /Product-Service/productEntity-service.jar .
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/productEntity-service.jar"]

EXPOSE 8081