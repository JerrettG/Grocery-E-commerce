FROM openjdk:17
RUN curl -sSL https://zipkin.io/quickstart.sh | bash -s
CMD java -jar zipkin.jar

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} api-gateway.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=dev","/api-gateway.jar"]

EXPOSE 8080