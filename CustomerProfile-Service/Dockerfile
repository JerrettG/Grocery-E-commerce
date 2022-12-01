FROM openjdk:17 as buildstage
WORKDIR /CustomerProfile-Service
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw package
COPY target/*.jar customer-profile-service.jar

FROM openjdk:17
COPY --from=buildstage /CustomerProfile-Service/customer-profile-service.jar .
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/customer-profile-service.jar"]

EXPOSE 8080