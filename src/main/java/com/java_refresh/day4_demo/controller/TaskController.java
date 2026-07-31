package com.java_refresh.day4_demo.controller;

import com.java_refresh.day4_demo.dto.TaskRequestDto;
import com.java_refresh.day4_demo.model.Task;
import com.java_refresh.day4_demo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    //constructor injection
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public Page<Task> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) Boolean completed
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return taskService.getAllTask(pageable, completed);
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PostMapping
    public ResponseEntity<Task> saveTask(@Valid @RequestBody TaskRequestDto taskDto) {
        Task taskCreated = taskService.saveTask(taskDto);
        URI location = URI.create("/tasks/" + taskCreated.getId());
        return ResponseEntity.created(location).body(taskCreated);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequestDto taskDto) {
        return taskService.updateTask(id, taskDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskById(@PathVariable Long id) {
        taskService.deleteTaskById(id);
        return ResponseEntity.noContent().build();
    }

}
