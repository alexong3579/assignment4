package com.taskflow.service;

import com.taskflow.entity.Task;
import com.taskflow.exception.TaskNotFoundException;
import com.taskflow.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository repository;

    @InjectMocks
    TaskService service;

    @Test
    void findAllReturnsRepositoryContents() {
        when(repository.findAll()).thenReturn(List.of(new Task(), new Task()));
        assertEquals(2, service.findAllTasks().size());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> service.findTaskById(99L));
    }

    @Test
    void createDefaultsCompletedToFalseAndClearsId() {
        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        Task input = new Task();
        input.setId(5L);
        input.setTitle("x");
        input.setCompleted(null);

        Task saved = service.createTask(input);

        assertNull(saved.getId());
        assertFalse(saved.getCompleted());
    }
}
