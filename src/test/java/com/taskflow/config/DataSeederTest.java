package com.taskflow.config;

import com.taskflow.entity.Task;
import com.taskflow.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    TaskRepository repository;

    @Captor
    ArgumentCaptor<List<Task>> saved;

    @InjectMocks
    DataSeeder seeder;

    @Test
    void seedsThreeTasksWhenTableIsEmpty() {
        when(repository.count()).thenReturn(0L);

        seeder.run();

        verify(repository).saveAll(saved.capture());
        assertEquals(3, saved.getValue().size());
    }

    @Test
    void doesNotSeedWhenTasksAlreadyExist() {
        when(repository.count()).thenReturn(1L);

        seeder.run();

        verify(repository, never()).saveAll(anyList());
    }
}
