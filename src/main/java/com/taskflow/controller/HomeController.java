package com.taskflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "service", "taskflow-api",
                "status", "UP",
                "endpoints", new String[]{"GET /api/tasks", "GET /api/tasks/{id}", "POST /api/tasks"});
    }
}
