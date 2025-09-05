package ru.dnechoroshev.simplecommunicator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Scanner;

@SpringBootApplication
@EnableScheduling
public class SimpleCommunicationClientPcApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleCommunicationClientPcApplication.class, args);
    }

}
