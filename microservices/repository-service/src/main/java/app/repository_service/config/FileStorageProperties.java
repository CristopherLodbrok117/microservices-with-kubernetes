package app.repository_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Getter
@Setter

@Configuration
@ConfigurationProperties(prefix = "sinaloa.repo")
public class FileStorageProperties {

    private String location;

}
