# Usa Java 21 (Temurin LTS)
FROM eclipse-temurin:21-jdk-jammy

# Directorio de trabajo
WORKDIR /app

# Copia el wrapper de Maven y dependencias
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY .env /app/.env

# POM padre
COPY ../pom.xml ./parent-pom.xml

# Instalar el POM padre primero
RUN mvn install -f parent-pom.xml

# Otorga permisos y descarga dependencias
RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline -B

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