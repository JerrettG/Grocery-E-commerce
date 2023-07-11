FROM openjdk:17 as buildstage
WORKDIR /Cart-Service
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw package
COPY target/*.jar cartResponse-service.jar

FROM openjdk:17
COPY --from=buildstage /Cart-Service/cartResponse-service.jar .
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=dev","/cartResponse-service.jar"]

EXPOSE 8080