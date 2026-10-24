package com.taskflow.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void rootReturnsServiceInfo() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void createThenFetchTask() throws Exception {
        String body = "{\"title\":\"Write tests\",\"priority\":\"HIGH\",\"dueDate\":\"2026-10-20\"}";
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.completed").value(false));

        mvc.perform(get("/api/tasks")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.title=='Write tests')]").exists());
    }

    @Test
    void startupSeedsSampleTasks() throws Exception {
        mvc.perform(get("/api/tasks")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.title=='Set up Docker')]").exists())
                .andExpect(jsonPath("$[?(@.title=='Deploy to Railway')]").exists())
                .andExpect(jsonPath("$[?(@.title=='Submit Assignment 4')]").exists());
    }

    @Test
    void missingTaskReturns404() throws Exception {
        mvc.perform(get("/api/tasks/987654")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: 987654"));
    }

    @Test
    void blankTitleReturns400() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content("{bad"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mvc.perform(get("/api/tasks/abc")).andExpect(status().isBadRequest());
    }
}
