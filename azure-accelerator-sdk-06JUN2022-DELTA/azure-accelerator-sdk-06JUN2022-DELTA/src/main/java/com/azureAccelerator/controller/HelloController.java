package com.azureAccelerator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HelloController {
    @GetMapping("/greeting")
    public ResponseEntity<String> getGreeting() {
        return new ResponseEntity<>(
                "Hi I am in getGreeting Method ", HttpStatus.OK);
    }
}
