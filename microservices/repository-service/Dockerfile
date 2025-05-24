# Usa Java 21 (Temurin LTS)
FROM eclipse-temurin:21-jdk-jammy

# Directorio de trabajo
WORKDIR /app

# Crea directorio para uploads (¡con permisos correctos!)
RUN mkdir -p /app/uploads && \
    chmod -R 775 /app/uploads && \
    chown -R 1000:1000 /app/uploads

# Copia el wrapper de Maven y dependencias
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY .env /app/.env

# POM padre
COPY ../pom.xml ./parent-pom.xml

# Otorga permisos y descarga dependencias
RUN chmod +x mvnw && \
    ./mvnw install -f parent-pom.xml \
    ./mvnw dependency:go-offline -B

# Copia el código fuente y compila
COPY src/ src/
RUN ./mvnw clean package -DskipTests && \
    mv target/*.jar /app/repository.jar && \
    rm -rf target/ .mvn/ mvnw* pom.xml

# Puerto expuesto
EXPOSE 8081

# Volumen para uploads (debe coincidir con SINALOA_REPO_LOCATION)
VOLUME /app/uploads

# Configuración de seguridad y ejecución directa
# Ejecuta como usuario no-root (buena práctica)
USER 1000
ENTRYPOINT ["java", "-Djdk.tls.client.protocols=TLSv1.2", "-jar", "repository.jar"]