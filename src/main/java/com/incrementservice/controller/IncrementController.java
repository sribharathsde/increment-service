package com.incrementservice.controller;

import com.incrementservice.dto.IncrementRequest;
import com.incrementservice.service.IncrementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class serves as the entry point of the application.
 * It handles incoming requests and invokes service methods to increment the value associated with a specified key.
 * Handles API requests for incrementing values in Redis.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Slf4j
public class IncrementController {

    private final IncrementService service;

    /**
     * Increments a value in Redis and returns accepted status.
     *
     * @param request the request body containing the key and value
     * @return HTTP status ACCEPTED
     */
    @PostMapping("/increment")
    public ResponseEntity<Void> incrementValue(@Valid @RequestBody IncrementRequest request) {

        log.info("Received request to increment value: key = {}, value = {}", request.getKey(), request.getValue());

        service.saveOrUpdateDataInRedis(request.getKey(), request.getValue());

        log.info("Returning response: HTTP {}", HttpStatus.ACCEPTED);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
