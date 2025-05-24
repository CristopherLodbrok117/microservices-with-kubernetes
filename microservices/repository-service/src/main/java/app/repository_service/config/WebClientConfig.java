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
//                .baseUrl("http://localhost:8082") // URL del microservicio de Grupos
                .build();
    }
}
