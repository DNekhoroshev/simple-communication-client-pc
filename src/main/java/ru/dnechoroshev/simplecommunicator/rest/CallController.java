package ru.dnechoroshev.simplecommunicator.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.dnechoroshev.simplecommunicator.client.CallProcessor;

@RestController
@RequestMapping("/call")
@RequiredArgsConstructor
@Slf4j
public class CallController {

    private final CallProcessor callProcessor;

    @GetMapping
    public void call(@RequestParam String callee) {
        callProcessor.call(callee);
    }

    @PostMapping("/hangup")
    public void hangUp() {
        callProcessor.hangUp();
    }

}
