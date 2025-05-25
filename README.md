# Orquestación de contenedores con Kubernetes

Definimos nuestros microservicios, cada uno con sus dependencias. En este escenario cada proyecto tendra su repositorio de archivos (eventualmente tambien habra asignación de 
proyectos a usuarios)
- Microservicio de manejo de archivos
- Miocroservicio de proyectos

<br>

## Comunicación entre microservicios (Webflux)

WebClient permite establecer comunicación entre servicios, enviando requests desde un microservicio a otro. Crearemos un nuevo servicio para implementar dicha lógica de aplicación.

Agregamos la dependencia.

```java
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webflux</artifactId>
		</dependency>
```

<br>

Crearemos un archivo de configuración desde el microservicio que enviara requests a otros

```java
package app.repository_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://146.190.171.239:8082") // URL del microservicio de Grupos
//                .baseUrl("http://localhost:8082") 
                .build();
    }
}
```

<br>

Creamos un servicio donde agregaremos los métodos necesarios para realizar las consultas necesarias, utilizando WebClient. 

```java
package app.repository_service.service;

import app.repository_service.dto.ProjectDto;
import app.repository_service.exception.FileException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProjectValidatorService {

    private final WebClient webClient;

    public boolean projectExists(Long projectId) {
        try {
            ProjectDto project = webClient.get()
                    .uri("/api/projects/{id}", projectId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        if (response.statusCode() == HttpStatus.NOT_FOUND) {
                            return Mono.empty();
                        }
                        return response.createException().flatMap(Mono::error);  // ← Más simple
                    })
                    .bodyToMono(ProjectDto.class)
                    .block();

            return project != null;
        } catch (WebClientResponseException e) {
            throw new FileException("Consulta a microservicio: no se encontro proyecto con ID: " + projectId);
        }
    }


}

```

<br>

Desde la capa de servicio del microservicio, agregamos inyectamos el bean al estado (inyección de dependencia por constructor) y lo utilizamos

```java
private void validateGroup(long groupId) {
    if(!projectValidatorService.projectExists(groupId)){
        throw new FileException("No se encontro el grupo con ID: " + groupId);
    }
}
```

<br>

Importante: utilizar un objeto DTO para la deserialización del objeto en nuestro microservicio.

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDto {
    private Long id;

    ...
}
```

<br>

## Docker

Esta vez subiremos nuestras imagenes a repositorios de docker-hub. Para ello primero configuraremos un token de acceso. Iniciamos sesión en [Dockerhub] nos dirigimos a Account setting > Personal access token. Y creamos uno nuevo configurando los permisos, descripción, etc.

Desde la terminal ejecutamos el comando que nos arroja el propio Docker (y guardamos bien el token)
`docker login -u nuestro-usuario-de-docker`

Cuando nos pida contraseña pegamos el token.

A partir de ahora podremos subir las imagenes generadas con suma facilidad.

<br>

<img src="https://github.com/CristopherLodbrok117/microservices-with-kubernetes/blob/317884ceb9d0afceffb3ac747c547556bc680cca/screenshots/30%20-%20access%20dockerhub.png" alt="token" width="700">

<br>

<br>

## Creación de imagenes

Desde la carpeta de cada microservicio ejecutamos los siguientes comandos. Crearemos un Dockerfile multi moduloen la raiz, al mismo nivel de los microservicios. Tiene la configuración
para construir cualquiera de los dos microservicios dependiendo el que se solicite

DOckerfile multi modulo

```java
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
FROM eclipse-temurin:21-jdk-jammy as file-service

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
```

<br>

Una vez creados desde ejecutamos los siguientes comandos para generar las imagenes

```java
docker build -f Dockerfile -t ragnarlodbrokv/project-service:latest --target project-service . --no-cache
docker build -f Dockerfile -t ragnarlodbrokv/file-service:latest --target file-service . --no-cache
```

<br>

<br>

<img src="" alt="" width="700">

<br>


<br>

<img src="" alt="" width="700">

<br>

Ahora subimos a nuestro Dockerhub las imagenes, ejecutnado

```java
docker push ragnarlodbrokv/project-service:latest
docker push ragnarlodbrokv/file-service:latest
```

<br>

<br>

<img src="" alt="" width="700">

<br>

<br>

<img src="" alt="" width="700">

<br>

<br>

<img src="" alt="" width="700">

<br>

<br>

<img src="" alt="" width="700">

<br>

<br>

<img src="" alt="" width="700">

<br>


<br>

## Creación de manifiestos

Kubernetes (K8s) es un orquestador de contenedores que automatiza el despliegue, escalado y gestión de aplicaciones en contenedores.

- Pods: La unidad más pequeña en Kubernetes. Un Pod agrupa uno o más contenedores que comparten recursos (red, almacenamiento) y se ejecutan en el mismo nodo.

- Manifiestos: Archivos YAML/JSON que definen la configuración deseada de los recursos en Kubernetes (ej: Pods, Deployments, Services).

Finalmente creamos los manifiestos necesarios para levantar nuestros servicios, utilizando las imagenes que creamos.

### Project-service

Configmap: Almacena configuraciones no sensibles (ej: variables de entorno, endpoints).
URLs de APIs, límites de tiempo, flags de features.

```java
apiVersion: v1
kind: ConfigMap
metadata:
  name: project-config
data:
  SPRING_DATASOURCE_URL: "jdbc:mysql://mysql-service:3306/projects_db?useSSL=false"
  SPRING_DATASOURCE_USERNAME: "bilbo"
  SPRING_DATASOURCE_PASSWORD: "bilbobolson117"
  SPRING_JPA_HIBERNATE_DDL_AUTO: "update"
  SERVER_PORT: "8082"

```

<br>

Deployment: Define cómo se despliega la aplicación: réplicas, imagen del contenedor, recursos como CPU y memoria y sus limites.

```java
apiVersion: apps/v1
kind: Deployment
metadata:
  name: project-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: project-service
  template:
    metadata:
      labels:
        app: project-service
    spec:
      containers:
        - name: project-service
          image: ragnarlodbrokv/project-service:v1.0
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8082
          envFrom:
            - configMapRef:
                name: project-config  # Usa el ConfigMap
          resources:
            requests:
              cpu: "500m"
              memory: "512Mi"
            limits:
              cpu: "1"
              memory: "1Gi"
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8082
            initialDelaySeconds: 30
            periodSeconds: 10
```

<br>

Service: Expone la aplicación dentro/fuera del cluster.

```java
apiVersion: v1
kind: Service
metadata:
  name: project-service
spec:
  selector:
    app: project-service
  ports:
    - protocol: TCP
      port: 8082
      targetPort: 8082
  type: ClusterIP
```

<br>

HPA (Horizontal Pod Autoscaler): Escala automáticamente los pods basado en métricas (CPU/RAM). Con esta configuración escala si supera el 40% de uso de CPU o memoria

```java
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: project-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: project-deployment
  minReplicas: 1  # Mínimo de pods
  maxReplicas: 3  # Máximo de pods
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 40  # Escala si el uso supera el 40%
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 40  # Escala si el uso supera el 40%
```

<br>

### File-service

ConfigMap: Configuración para conexión a base de datosm, rutas de almacenamiento, asi como algunas variables de entorno que nos permitiran configurar multipart correctamente

```java
apiVersion: v1
kind: ConfigMap
metadata:
  name: file-config
data:
  SPRING_DATASOURCE_URL: "jdbc:mysql://mysql-service:3306/files_db?useSSL=false"
  SPRING_DATASOURCE_USERNAME: "bilbo"
  SPRING_DATASOURCE_PASSWORD: "bilbobolson117"
  SPRING_JPA_HIBERNATE_DDL_AUTO: "update"

  # Configuración Multipart (¡crítico!)
  SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE: "15MB"
  SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE: "15MB"
  SPRING_SERVLET_MULTIPART_RESOLVE_LAZILY: "true"

  # Ubicación de archivos (¡importante para Kubernetes!)
  SINALOA_REPO_LOCATION: "/app/uploads"  # Usa ruta absoluta

  # Server
  SERVER_PORT: "8081"
```

<br>

Deployment: Despliega los pods que gestionan archivos.

```java
apiVersion: apps/v1
kind: Deployment
metadata:
  name: file-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: file-service
  template:
    metadata:
      labels:
        app: file-service
    spec:
      containers:
        - name: file-service
          image: ragnarlodbrokv/file-service:v1.0
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8081
          envFrom:
            - configMapRef:
                name: file-config
          volumeMounts:
            - name: uploads-volume
              mountPath: /app/uploads
          resources:
            requests:
              cpu: "500m"
              memory: "512Mi"
            limits:
              cpu: "1"
              memory: "1Gi"
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8081
            initialDelaySeconds: 30
      volumes:
        - name: uploads-volume
          persistentVolumeClaim:
            claimName: file-uploads-pvc
```

<br>

HPA: Escala según demanda de solicitudes de archivos.

```java
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: file-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: file-deployment
  minReplicas: 1
  maxReplicas: 3
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 40
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 40
```

<br>

PVC (PersistentVolumeClaim): Solicita almacenamiento persistente para los archivos. Permitiendo conservarlos ante reinicios de pods

```java
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: file-uploads-pvc
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi  # Ajusta según necesidades
```

<br>

Service: Permite acceder al servicio desde otros pods o externamente

```java
apiVersion: v1
kind: Service
metadata:
  name: file-service
spec:
  selector:
    app: file-service
  ports:
    - protocol: TCP
      port: 8081
      targetPort: 8081
  type: ClusterIP
```

<br>

### Mysql-service

Deployment: Despliega el contenedor de MySQL con configuraciones iniciales. Configuramos dos bases de datos para nuestro proyecto y un usuario con permisos para ambas.

```java
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql-deployment
spec:
  template:
    spec:
      containers:
        - name: mysql
          image: mysql:8.0
          env:
            - name: MYSQL_ROOT_PASSWORD
              value: "root1234"
            - name: MYSQL_DATABASE  # Base de datos principal (opcional)
              value: "projects_db"
            - name: MYSQL_USER
              value: "bilbo"
            - name: MYSQL_PASSWORD
              value: "bilbobolson117"
            # ¡Nuevo! Crear una segunda base de datos al iniciar
            - name: MYSQL_EXTRA_DATABASES
              value: "files_db"
            # Script para otorgar permisos al usuario en ambas DBs
            - name: MYSQL_EXTRA_INIT_SCRIPT
              value: |
                GRANT ALL PRIVILEGES ON files_db.* TO 'bilbo'@'%';
                FLUSH PRIVILEGES;
          ports:
            - containerPort: 3306
          volumeMounts:
            - name: mysql-data
              mountPath: /var/lib/mysql
```

<br>

PVC: Almacena los datos de la base de datos de forma persistente.

```java
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 5Gi
```

<br>

Service: Expone MySQL internamente (solo accesible dentro del cluster) a traves del puerto 3306.

```java
apiVersion: v1
kind: Service
metadata:
  name: mysql-service
spec:
  selector:
    app: mysql
  ports:
    - protocol: TCP
      port: 3306
      targetPort: 3306
  type: ClusterIP
```

<br>


## Aplicación de los manifiestos

Ejecutamos los comandos respetando el orden para aplicar los manifiestos y levantar los servicios configurados (desde la carpeta correcta)

### Servicio mysql:

```java
kubectl apply -f configmap.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
```

<br>

Verificamos los pods

```java
kubectl get pods -l app=project-service
```

<br>

Consultar logs

```java
kubectl logs -f <pod-name>
```

<br>

### Microservicio de proyectos

Ejecutamos los siguientes comandos para aplicar los manifiestos

```java
kubectl apply -f configmap.yaml
kubectl apply -f pvc.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl apply -f hpa.yaml

```

<br>

Verificar el HPA

```java
kubectl get hpa
```

<br>

Generamos carga para prueba de resiliencia y escalamiento

```java
kubectl run -it --rm load-generator --image=busybox -- /bin/sh -c "while true; do wget -q -O- http://project-service:8082/api/projects; done"
```

<br>

### Nicroservicio de gestor de archivos

Ejecutamos los recursos

```java
kubectl apply -f configmap.yaml
kubectl apply -f pvc.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl apply -f hpa.yaml

```

En cada archivo subido se comunica el microservicio de archivos con el microservicio de proyectos
