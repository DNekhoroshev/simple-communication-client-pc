package ru.dnechoroshev.simplecommunicator.audio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sound.sampled.*;
import java.io.BufferedOutputStream;
import java.io.OutputStream;

@Component
@Slf4j
public class MicrophoneReader {

    private AudioFormat audioFormat;
    private TargetDataLine targetDataLine;
    private boolean isRecording;

    public MicrophoneReader() {
        // Настройка формата аудио: 16kHz, 16-bit, моно
        audioFormat = new AudioFormat(16000.0f, 16, 1, true, true);
    }

    /**
     * Инициализирует и открывает линию микрофона
     */
    public boolean initializeMicrophone() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

            // Проверяем, поддерживается ли формат
            if (!AudioSystem.isLineSupported(info)) {
                log.error("Микрофон с таким форматом не поддерживается");
                return false;
            }

            targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(audioFormat);
            return true;

        } catch (LineUnavailableException e) {
            log.error("Микрофон недоступен", e);
            return false;
        }
    }

    /**
     * Начинает запись с микрофона
     */
    public Thread startRecording(OutputStream outputStream) {
        if (targetDataLine == null) {
            if (!initializeMicrophone()) {
                return null;
            }
        }

        isRecording = true;
        BufferedOutputStream out = new BufferedOutputStream(outputStream);

        // Запускаем поток для чтения данных
        Thread recordingThread = new Thread(() -> {
            try {
                targetDataLine.start();

                byte[] buffer = new byte[4096];
                int bytesRead;

                while (isRecording) {
                    bytesRead = targetDataLine.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                log.info("Закрытие потока чтения микрофона");
            } catch (Exception e) {
                if (isRecording) {
                    log.error("Ошибка передачи исходящего потока", e);
                } else {
                    log.info("Прекращена передача исходящего потока");
                }
            } finally {
                log.info("Прекращена передача исходящего потока");
            }
        });

        recordingThread.start();
        return recordingThread;
    }

    /**
     * Останавливает запись
     */
    public void stopRecording() {
        isRecording = false;
        if (targetDataLine != null) {
            targetDataLine.stop();
            targetDataLine.close();
        }
    }

}
