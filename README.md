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

Desde la carpeta de cada microservicio ejecutamos los siguientes comandos

Microservicio de proyectos

```java
docker build -t ragnarlodbrokv/project-service:v1.0 . --no-cache

docker push ragnarlodbrokv/project-service:v1.0
```

<br>

Microservicio de archivos

```java
docker build -t ragnarlodbrokv/files-service:v1.0 . --no-cache

docker push ragnarlodbrokv/files-service:v1.0
```

<br>

Es buena práctica manejar versionado en lugar de siempre utilizar el tag `latest`. En el caso de Mysql, esta vez no tenemos que descargarla como su despliegue con docker compose, con el manifiesto de k8s automatizamos el proceso.

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

```

<br>

Service: Expone la aplicación dentro/fuera del cluster (ClusterIP, LoadBalancer).

```java

```

<br>

HPA (Horizontal Pod Autoscaler): Escala automáticamente los pods basado en métricas (CPU/RAM). Con esta configuración escala si supera el 40% de uso de CPU o memoria

```java

```

<br>

### File-service

ConfigMap: Configura rutas de almacenamiento, tipos de archivos permitidos.

```java

```

<br>

Deployment: Despliega los pods que gestionan archivos.

```java

```

<br>

HPA: Escala según demanda de solicitudes de archivos.

```java

```

<br>

PVC (PersistentVolumeClaim): Solicita almacenamiento persistente para los archivos. Permitiendo conservarlos ante reinicios de pods

```java

```

<br>

Service: Permite acceder al servicio desde otros pods o externamente

```java

```

<br>

### Mysql-service

Deployment: Despliega el contenedor de MySQL con configuraciones iniciales.

```java

```

<br>

PVC: Almacena los datos de la base de datos de forma persistente.

```java

```

<br>

Service: Expone MySQL internamente (solo accesible dentro del cluster).

```java

```

<br>

```java

```

<br>


<br>

<img src="" alt="" width="700">

<br>

