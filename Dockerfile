FROM openjdk:17-alpine
COPY build/libs/HotelMS.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]