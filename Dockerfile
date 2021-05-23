FROM gradle:jdk11 AS builder
WORKDIR /app
COPY . .
RUN gradle build

FROM openjdk:11
COPY --from=builder /app/build/libs/keymanager-grpc-0.1-all.jar /app/app.jar
EXPOSE 50051
ENTRYPOINT ["java","-jar","/app/app.jar"]