FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle/ gradle/
RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon || true

COPY . .
RUN ./gradlew clean build -x test

FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=builder /app/build/libs/moa-server-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]