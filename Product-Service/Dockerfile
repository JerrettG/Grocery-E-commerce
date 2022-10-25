FROM openjdk:17 as buildstage
WORKDIR /Product-Service
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw package
COPY target/*.jar product-service.jar

FROM openjdk:17
COPY --from=buildstage /Product-Service/product-service.jar .
ENTRYPOINT ["java","-jar","/product-service.jar"]

EXPOSE 8081