package ru.dnechoroshev.simplecommunicator.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "general")
public class ClientProperties {
    private String clientName;
    private String serverUrlBase;
    private String serverHost;
}
