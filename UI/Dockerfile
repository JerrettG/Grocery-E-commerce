FROM openjdk:17 as buildstage
WORKDIR /UI
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw package
COPY target/*.jar user-interface.jar

FROM openjdk:17
COPY --from=buildstage /UI/user-interface.jar .
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/user-interface.jar"]

EXPOSE 8084