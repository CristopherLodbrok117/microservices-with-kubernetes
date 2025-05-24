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
