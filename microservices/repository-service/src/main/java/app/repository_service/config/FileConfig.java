package app.repository_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class FileConfig {

    @Bean
    public Set<String> allowedTypes(){

        return new HashSet<>(
                Set.of(
                        "image/png",
                        "image/jpeg",
                        "image/gif",
                        "image/webp",
                        "application/pdf",
                        "application/xml",
                        "application/csv",
                        "application/json",
                        "application/msword",
                        "application/java-archive",
                        "application/octet-stream",
                        "application/javascript",
                        "application/x-httpd-php",
                        "application/rls-services+xml",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/vnd.ms-powerpoint",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        "text/x-java-source",
                        "text/csv",
                        "text/xml",
                        "text/plain",
                        "text/x-c",
                        "text/html",
                        "text/css",
                        "text/javascript",
                        "video/mp2t"
                        )
        );

    }
}
