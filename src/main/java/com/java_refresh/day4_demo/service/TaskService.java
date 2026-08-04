package com.java_refresh.day4_demo.service;

import com.java_refresh.day4_demo.dto.TaskRequestDto;
import com.java_refresh.day4_demo.exceptions.TaskNotFoundException;
import com.java_refresh.day4_demo.exceptions.UserNotFoundException;
import com.java_refresh.day4_demo.model.Task;
import com.java_refresh.day4_demo.model.User;
import com.java_refresh.day4_demo.repository.TaskRepository;
import com.java_refresh.day4_demo.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    //private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    //constructor injection
    public TaskService(TaskRepository taskRepository,  UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public Page<Task> getAllTaskForAdmin(Pageable pageable, Boolean completed){
        if (null != completed){
            return taskRepository.findByCompleted(completed, pageable);
        }
        return taskRepository.findAll(pageable);
    }

    public Page<Task> getAllTask(Pageable pageable, Boolean completed) {
        String loggedInUsername = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        if (null != completed) {
            return taskRepository.findByOwnerUsernameAndCompleted(loggedInUsername, completed, pageable);
        }
        return taskRepository.findByOwnerUsername(loggedInUsername, pageable);
    }

    @Cacheable(value = "tasks", key = "#id + '-' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().authentication.name")
    public Task getTaskById(Long id) {
        String loggedInUsername = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        User loggedInUser = userRepository.findByUsername(loggedInUsername).orElseThrow(()  -> new UserNotFoundException(loggedInUsername));

        Task returnedTask = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException("No task found with id: " + id));

        if ("ADMIN".equals(loggedInUser.getRole())) {
            return returnedTask;
        }

        if (!loggedInUsername.equals(returnedTask.getOwner().getUsername())) {
            throw new TaskNotFoundException("No task found with id: " + id);
        }

        return returnedTask;
    }

    public Task saveTask(TaskRequestDto taskDto) {
        String loggedInUsername = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User owner = userRepository.findByUsername(loggedInUsername).orElseThrow(() -> new UserNotFoundException("No user found with username: " + loggedInUsername));
        Task savedTask = new Task(taskDto.title(), taskDto.completed(), owner);
        return taskRepository.save(savedTask);
    }

    @CacheEvict(value = "tasks", key = "#id + '-' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().authentication.name")
    public Task updateTask(Long id, TaskRequestDto taskDto) {
        String loggedInUsername = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        User loggedInUser = userRepository.findByUsername(loggedInUsername).orElseThrow(()  -> new UserNotFoundException(loggedInUsername));

        Task existingTask = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException("No task found with id: " + id));

        if ("ADMIN".equals(loggedInUser.getRole())) {
            existingTask.setTitle(taskDto.title());
            existingTask.setCompleted(taskDto.completed());
            return taskRepository.save(existingTask);
        }

        if (!loggedInUsername.equals(existingTask.getOwner().getUsername())) {
            throw new TaskNotFoundException("No task found with id: " + id);
        }
        existingTask.setTitle(taskDto.title());
        existingTask.setCompleted(taskDto.completed());
        return taskRepository.save(existingTask);
    }

    @CacheEvict(value = "tasks", key = "#id + '-' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().authentication.name")
    public void deleteTaskById(Long id) {
        String loggedInUsername = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        User loggedInUser = userRepository.findByUsername(loggedInUsername).orElseThrow(()  -> new UserNotFoundException(loggedInUsername));

        Task existingTask = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException("No task found with id: " + id));

        if ("ADMIN".equals(loggedInUser.getRole())) {
            taskRepository.deleteById(existingTask.getId());
            return;
        }

        if (!loggedInUsername.equals(existingTask.getOwner().getUsername())) {
            throw new TaskNotFoundException("No task found with id: " + id);
        }
        taskRepository.deleteById(existingTask.getId());
    }
}
