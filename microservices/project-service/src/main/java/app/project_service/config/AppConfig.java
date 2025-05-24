package app.project_service.config;

import app.project_service.model.Project;
import app.project_service.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    private final ProjectRepository repo;

    @Bean
    CommandLineRunner initDatabase (){
        return args -> {
            List<Project> projects = List.of(
                    Project.builder()
                            .name("Proyecto final")
                            .description("Proyecto con microservicios")
                            .createdAt(LocalDateTime.now())
                            .isActive(true)
                            .build(),

                    Project.builder()
                            .name("Actividad integradora")
                            .description("Portafolio de evidencias")
                            .createdAt(LocalDateTime.now())
                            .isActive(true)
                            .build(),

                    Project.builder()
                            .name("Proyecto final v2")
                            .description("Proyecto con kubernetes")
                            .createdAt(LocalDateTime.now())
                            .isActive(true)
                            .build()
            );

            repo.saveAll(projects);
        };
    }
}
