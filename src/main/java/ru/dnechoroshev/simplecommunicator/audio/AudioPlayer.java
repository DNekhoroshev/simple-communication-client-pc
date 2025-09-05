package ru.dnechoroshev.simplecommunicator.audio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

@Component
@Slf4j
public class AudioPlayer {
    private AudioFormat audioFormat;
    private SourceDataLine sourceDataLine;
    private volatile boolean isPlaying;
    InputStream streamToPlay;

    public AudioPlayer() {
        // Формат аудио: 44.1kHz, 16-bit, стерео (стандартный для воспроизведения)
        audioFormat = new AudioFormat(16000.0f, 16, 1, true, false);
    }

    public AudioPlayer(AudioFormat format) {
        this.audioFormat = format;
    }

    /**
     * Инициализирует линию вывода звука (наушники/колонки)
     */
    public boolean initializeAudioOutput() {
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

            if (!AudioSystem.isLineSupported(info)) {
                log.info("Аудио выход с таким форматом не поддерживается");
                return false;
            }

            sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(audioFormat);
            return true;

        } catch (LineUnavailableException e) {
            log.error("Аудио выход недоступен: ", e);
            return false;
        }
    }

    /**
     * Воспроизводит массив байтов как аудио
     */
    public void playAudio(byte[] audioData) {
        if (sourceDataLine == null) {
            if (!initializeAudioOutput()) {
                return;
            }
        }

        try {
            sourceDataLine.start();
            sourceDataLine.write(audioData, 0, audioData.length);
            sourceDataLine.drain(); // Ждем завершения воспроизведения
            sourceDataLine.stop();

        } catch (Exception e) {
            log.error("Ошибка при воспроизведении: ", e);
        }
    }

    /**
     * Воспроизводит аудио из InputStream в реальном времени
     */
    public Thread playStream(InputStream audioStream) {
        if (sourceDataLine == null) {
            if (!initializeAudioOutput()) {
                return null;
            }
        }

        isPlaying = true;
        streamToPlay = audioStream;
        Thread playThread = new Thread(() -> {
            try {
                sourceDataLine.start();

                byte[] buffer = new byte[4096];
                int bytesRead;

                while (isPlaying && (bytesRead = audioStream.read(buffer)) != -1) {
                    if (bytesRead > 0) {
                        sourceDataLine.write(buffer, 0, bytesRead);
                    }
                }
                log.info("Закрытие потока передачи аудио 1");
                sourceDataLine.drain();
                sourceDataLine.stop();
                log.info("Закрытие потока передачи аудио");
            } catch (IOException e) {
                if (isPlaying) {
                    log.error("Ошибка при чтении потока", e);
                } else {
                    log.info("Поток чтения микрофона закрыт");
                }
            } finally {
                stopPlayback();
            }
        });

        playThread.start();
        return playThread;
    }

    /**
     * Останавливает воспроизведение
     */
    public void stopPlayback() {
        isPlaying = false;
        try {
            if (sourceDataLine != null && sourceDataLine.isOpen()) {
                sourceDataLine.stop();
                sourceDataLine.close();
            }
            streamToPlay.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Проверяет, идет ли воспроизведение
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Возвращает текущий аудио формат
     */
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }
}
