FROM public.ecr.aws/bitnami/java:11-prod
WORKDIR /app
COPY . .
RUN sh gradlew build
EXPOSE 50051
EXPOSE 8081
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/build/libs/keymanager-grpc-0.1-all.jar"]