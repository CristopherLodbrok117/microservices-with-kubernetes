# Usa Java 21 (Temurin LTS)
FROM eclipse-temurin:21-jdk-jammy

# Directorio de trabajo
WORKDIR /app

# Copia el wrapper de Maven y dependencias
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY .env /app/.env

# 2. Copia el POM padre a una ubicación específica
COPY ../pom.xml ./../pom.xml

# 3. Instala el POM padre usando relativePath
RUN chmod +x mvnw && \
    ./mvnw install -f ./../pom.xml

# 4. Descarga dependencias
RUN ./mvnw dependency:go-offline -B

# Copia el código fuente y compila
COPY src/ src/
RUN ./mvnw clean package -DskipTests && \
    mv target/*.jar /app/projects.jar && \
    rm -rf target/ .mvn/ mvnw* pom.xml

# Puerto expuesto
EXPOSE 8082

# Volumen para uploads (si tu microservicio guarda archivos aquí)
# VOLUME /app/uploads

# Configuración de seguridad y ejecución directa
ENTRYPOINT ["java", "-Djdk.tls.client.protocols=TLSv1.2", "-jar", "projects.jar"]