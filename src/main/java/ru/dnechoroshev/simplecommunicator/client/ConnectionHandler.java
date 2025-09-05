package ru.dnechoroshev.simplecommunicator.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.dnechoroshev.simplecommunicator.audio.AudioPlayer;
import ru.dnechoroshev.simplecommunicator.audio.MicrophoneReader;
import ru.dnechoroshev.simplecommunicator.model.ConnectionDto;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConnectionHandler {

    private final AudioPlayer audioPlayer;
    private final MicrophoneReader microphoneReader;

    private final ExecutorService techPool =  Executors.newSingleThreadExecutor();

    private static final String SERVER_ADDRESS = "127.0.0.1";

    @Getter
    private boolean alive;

    public void handleConnection(@NonNull ConnectionDto connection) {
        log.info("Соединяемся с {}:{}", SERVER_ADDRESS, connection.port());
        techPool.submit(() -> {
            try (Socket socket = new Socket(SERVER_ADDRESS, connection.port())) {
                log.info("Соединение установлено");
                alive = true;
                InputStream inputStream = socket.getInputStream();

                Thread inputAudioReaderThread = audioPlayer.playStream(inputStream);
                Thread outputAudioWriterThread = microphoneReader.startRecording(socket.getOutputStream());

                log.info("Начало разговора");
                log.info("1");
                inputAudioReaderThread.join();
                log.info("2");
                outputAudioWriterThread.join();
                log.info("3");
                log.info("Разговор завершен");
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                alive = false;
            }
        });
    }

    public void hangUp() {
        log.info("Вешаем трубку...");
        alive = false;
        audioPlayer.stopPlayback();
        microphoneReader.stopRecording();
    }

}
