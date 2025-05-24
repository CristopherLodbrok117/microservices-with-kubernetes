# Usa Java 21 (Temurin LTS)
FROM eclipse-temurin:21-jdk-jammy

# Directorio de trabajo
WORKDIR /app

# --- Copia los archivos del MICROSERVICIO (ej: project-service/) ---
# 1. Copia el wrapper Maven (desde la carpeta del microservicio)
COPY project-service/.mvn/ .mvn
COPY project-service/mvnw .
COPY project-service/pom.xml .

# 2. Copia el POM padre (desde la raíz)
COPY pom.xml ./parent-pom.xml

# 3. Instala el POM padre y descarga dependencias
RUN chmod +x mvnw && \
    ./mvnw install -f parent-pom.xml && \
    ./mvnw dependency:go-offline -B

# 4. Copia el código fuente (desde la carpeta del microservicio)
COPY project-service/src/ src/

# 5. Construye el JAR
RUN ./mvnw clean package -DskipTests && \
    mv target/*.jar /app/projects.jar

# Puerto expuesto
EXPOSE 8082

ENTRYPOINT ["java", "-Djdk.tls.client.protocols=TLSv1.2", "-jar", "projects.jar"]