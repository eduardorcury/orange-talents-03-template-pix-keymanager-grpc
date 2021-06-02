FROM public.ecr.aws/bitnami/java:11-prod
WORKDIR /app
COPY . .
RUN sh gradlew build
EXPOSE 50051
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/build/libs/*-all.jar"]