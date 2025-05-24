# Etapa 1: Build con Maven
FROM maven:3.9-eclipse-temurin-21 as builder

WORKDIR /build

# Copiamos todo el proyecto multi-módulo (asume que el pom.xml padre está aquí)
COPY . .

# Compilamos todos los módulos sin tests
RUN mvn clean package -DskipTests

# Etapa 2: Imagen para project-service
FROM eclipse-temurin:21-jdk-jammy as project-service

WORKDIR /app
COPY --from=builder /build/project-service/target/*.jar app.jar
COPY project-service/.env /app/.env

EXPOSE 8082
ENTRYPOINT ["java", "-Djdk.tls.client.protocols=TLSv1.2", "-jar", "app.jar"]

# Etapa 3: Imagen para repository-service
FROM eclipse-temurin:21-jdk-jammy as files-service

WORKDIR /app

RUN mkdir -p /app/uploads && \
    chmod -R 775 /app/uploads && \
    chown -R 1000:1000 /app/uploads

COPY --from=builder /build/repository-service/target/*.jar app.jar
COPY repository-service/.env /app/.env

EXPOSE 8081
VOLUME /app/uploads

USER 1000
ENTRYPOINT ["java", "-Djdk.tls.client.protocols=TLSv1.2", "-jar", "app.jar"]
