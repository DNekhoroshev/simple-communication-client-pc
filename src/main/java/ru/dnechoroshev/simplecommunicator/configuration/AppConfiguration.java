package ru.dnechoroshev.simplecommunicator.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AppConfiguration {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

}
