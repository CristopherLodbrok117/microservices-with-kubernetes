# Orquestación de contenedores con Kubernetes

Definimos nuestros microservicios, cada uno con sus dependencias. En este escenario cada proyecto tendra su repositorio de archivos (eventualmente tambien habra asignación de 
proyectos a usuarios)
- Microservicio de manejo de archivos
- Miocroservicio de proyectos

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

Crearemos un archivo de configuración desde el microservicio que enviara request a otro

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

Creamos un servicio donde agregaremos los métodos necesarios para realizar las consultas necesarias. 

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




<br>

<img src="" alt="" width="700">

<br>

