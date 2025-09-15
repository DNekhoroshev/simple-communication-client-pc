package ru.dnechoroshev.simplecommunicator.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.dnechoroshev.simplecommunicator.configuration.ClientProperties;
import ru.dnechoroshev.simplecommunicator.model.ConnectionDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallProcessor {

    private final RestClient restClient;
    private final ConnectionHandler connectionHandler;
    private final ClientProperties clientProperties;

    public void hangUp() {
        if (connectionHandler.isAlive()) {
            connectionHandler.hangUp();
            String url = UriComponentsBuilder.fromHttpUrl(clientProperties.getServerUrlBase() + "/terminate-call")
                    .queryParam("caller", clientProperties.getClientName())
                    .toUriString();

            ResponseEntity<Void> response = restClient.delete()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity();

            log.debug("Terminated: {}", response.getStatusCode());
        }
    }

    public void call(String callee) {
        String url = UriComponentsBuilder.fromHttpUrl(clientProperties.getServerUrlBase() + "/requestCall")
                .queryParam("caller", clientProperties.getClientName())
                .queryParam("callee", callee)
                .toUriString();

        ResponseEntity<ConnectionDto> response = restClient.get()
                .uri(url)
                .retrieve()
                .toEntity(ConnectionDto.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            ConnectionDto connectionDto = response.getBody();
            log.info("Принят запрос на вызов {}, назначен порт {}", connectionDto.correspondent(), connectionDto.port());
            connectionHandler.handleConnection(connectionDto);
        } else if (response.getStatusCode().isError()) {
            log.error("Ошибка обращения к серверу");
            if (response.getBody() != null) {
                log.error(response.getBody().toString());
            }
        }
    }

    @Scheduled(fixedRate = 5000)
    public void checkForIncomingCall() {

        String url = UriComponentsBuilder.fromHttpUrl(clientProperties.getServerUrlBase() + "/checkInvocation")
                .queryParam("callee", clientProperties.getClientName())
                .toUriString();

        ResponseEntity<ConnectionDto> response = restClient.get()
                .uri(url)
                .retrieve()
                .toEntity(ConnectionDto.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            ConnectionDto connectionDto = response.getBody();
            log.info("Получен вызов от {}, назначен порт {}", connectionDto.correspondent(), connectionDto.port());
            connectionHandler.handleConnection(connectionDto);
        } else if (response.getStatusCode().isError()) {
            log.error(response.getBody().toString());
        }
    }

}
