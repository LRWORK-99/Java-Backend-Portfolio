package com.java_refresh.day4_demo.controller;

import com.java_refresh.day4_demo.dto.TaskRequestDto;
import com.java_refresh.day4_demo.dto.TaskResponseDto;
import com.java_refresh.day4_demo.model.Task;
import com.java_refresh.day4_demo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private TaskResponseDto toResponse(Task task) {
        return new TaskResponseDto(task.getId(), task.getTitle(), task.isCompleted(), task.getOwner().getUsername());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public Page<TaskResponseDto> adminGetAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) Boolean completed
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return taskService.getAllTaskForAdmin(pageable, completed).map(this::toResponse);
    }

    @GetMapping
    public Page<TaskResponseDto> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) Boolean completed
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return taskService.getAllTask(pageable, completed).map(this::toResponse);
    }

    @GetMapping("/{id}")
    public TaskResponseDto getTaskById(@PathVariable Long id) {
        return toResponse(taskService.getTaskById(id));
    }

    @PostMapping
    public ResponseEntity<TaskResponseDto> saveTask(@Valid @RequestBody TaskRequestDto taskDto) {
        Task taskCreated = taskService.saveTask(taskDto);
        URI location = URI.create("/tasks/" + taskCreated.getId());
        return ResponseEntity.created(location).body(toResponse(taskCreated));
    }

    @PutMapping("/{id}")
    public TaskResponseDto updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequestDto taskDto) {
        return toResponse(taskService.updateTask(id, taskDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskById(@PathVariable Long id) {
        taskService.deleteTaskById(id);
        return ResponseEntity.noContent().build();
    }

}
