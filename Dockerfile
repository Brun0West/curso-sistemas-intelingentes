FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./
COPY jade.jar ./

COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar
COPY --from=build /app/jade.jar /app/jade.jar

ENTRYPOINT ["java", "--add-opens=java.xml/com.sun.org.apache.xerces.internal.jaxp=ALL-UNNAMED", "-cp", "/app/app.jar:/app/jade.jar", "jade.Boot"]
