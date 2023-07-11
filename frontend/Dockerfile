FROM openjdk:17 as buildstage
WORKDIR /frontend
#COPY mvnw .
#COPY .mvn .mvn
#COPY pom.xml .
#COPY src src
#RUN chmod +x mvnw
#RUN ./mvnw package
COPY target/*.jar frontend-web.jar

FROM openjdk:17
COPY --from=buildstage /frontend/frontend-web.jar .
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=dev","/frontend-web.jar"]

EXPOSE 8080