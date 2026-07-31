package com.java_refresh.day4_demo.service;

import com.java_refresh.day4_demo.dto.TaskRequestDto;
import com.java_refresh.day4_demo.exceptions.TaskNotFoundException;
import com.java_refresh.day4_demo.model.Task;
import com.java_refresh.day4_demo.repository.TaskRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    //constructor injection
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Page<Task> getAllTask(Pageable pageable, Boolean completed) {
        if (null != completed) {
            return taskRepository.findByCompleted(completed, pageable);
        }
        return taskRepository.findAll(pageable);
    }

    @Cacheable(value = "tasks", key = "#id")
    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException("No task found with id: " + id));
    }

    public Task saveTask(TaskRequestDto taskDto) {
        Task savedTask = new Task(taskDto.title(), taskDto.completed());
        return taskRepository.save(savedTask);
    }

    @CacheEvict(value = "tasks", key = "#id")
    public Task updateTask(Long id, TaskRequestDto taskDto) {
        Task existingTask = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException("No task found with id: " + id));
        existingTask.setTitle(taskDto.title());
        existingTask.setCompleted(taskDto.completed());
        return taskRepository.save(existingTask);
    }

    @CacheEvict(value = "tasks", key = "#id")
    public void deleteTaskById(Long id) {
        Task existingTask = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException("No task found with id: " + id));
        taskRepository.deleteById(existingTask.getId());
    }
}
