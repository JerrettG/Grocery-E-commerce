FROM openjdk:17
ENV STRIPE_API_KEY=${STRIPE_API_KEY}
ENV STRIPE_PUBLIC_KEY=${STRIPE_PUBLIC_KEY}
ENV STRIPE_WEBHOOK_SECRET=${STRIPE_WEBHOOK_SECRET}
CMD stripe login --$STRIPE_API_KEY
CMD stripe listen --forward-to localhost:8084/stripe/events
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} user-interface.jar
ENTRYPOINT ["java","-jar","/user-interface.jar"]

EXPOSE 8084