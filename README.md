# HotelMS - Hotel Management System

Hotel Management System developed with Spring Boot with JWT Authentication.

To populate the database with mock data, set `application.runner`
in [application.properties](src/main/resources/application.properties) to `true` and adjust your database configuration.
Make sure the `spring.jpa.hibernate.ddl-auto` configuration is adequate depending on the current database state.

A fallback in-memory H2 database configuration is provided for development purposes (commented).

Integration tests may be run with `gradle test` and the configuration environment may be adjusted
at [application-testing.properties](src/main/resources/application-testing.properties).

A suitable JAR may be built with `gradle build`, as well as the respective OpenAPI information
with `gradle generateOpenApiDocs`. The OpenAPI docs endpoint may need to be adjusted at [build.gradle](build.gradle)
depending on the [application.properties](src/main/resources/application.properties) configuration.