package com.taskflow.config;

import com.taskflow.entity.Task;
import com.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Seeds a few sample tasks on startup, only when the table is empty. The database is in-memory,
 * so without this the live API returns an empty list after every redeploy.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final TaskRepository taskRepository;

    @Override
    public void run(String... args) {
        if (taskRepository.count() > 0) {
            return;
        }
        taskRepository.saveAll(List.of(
                task("Set up Docker", "Containerize TaskFlow with a multi-stage Dockerfile", "HIGH", true),
                task("Deploy to Railway", "Deploy the image and generate a public domain", "HIGH", true),
                task("Submit Assignment 4", "Add README screenshots and submit the repo URL", "MEDIUM", false)));
    }

    private static Task task(String title, String description, String priority, boolean completed) {
        Task t = new Task();
        t.setTitle(title);
        t.setDescription(description);
        t.setPriority(priority);
        t.setDueDate(LocalDate.of(2026, 10, 20));
        t.setCompleted(completed);
        return t;
    }
}
