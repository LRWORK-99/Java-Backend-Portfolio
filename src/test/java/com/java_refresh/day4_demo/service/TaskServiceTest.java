package com.java_refresh.day4_demo.service;

import com.java_refresh.day4_demo.dto.TaskRequestDto;
import com.java_refresh.day4_demo.exceptions.TaskNotFoundException;
import com.java_refresh.day4_demo.model.Task;
import com.java_refresh.day4_demo.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    public void getTaskById_shouldReturnTask_whenTaskExists() {
        Task testTask = new Task("Testing Task", false);

        //Tell the mock taskRepo what to do if findByID method is being called
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        Task result = taskService.getTaskById(1L);

        assertEquals(testTask.getTitle(), result.getTitle());

    }

    @Test
    public void getTaskById_shouldThrowException_whenTaskDoesNotExist() {
        when(taskRepository.findById(67L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(67L));
    }

    @Test
    public void saveTask() {
        TaskRequestDto taskRequestDto = new TaskRequestDto("Testing Task Dto", false);

        //invocation.getArgument(0) grabs whatever object was actually passed as the first argument to save(...) — and returns that same object back.
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.saveTask(taskRequestDto);

        assertEquals(taskRequestDto.title(), result.getTitle());
    }

    @Test
    public void updateTask() {
        TaskRequestDto taskRequestDto = new TaskRequestDto("Testing Update Task", false);
        Task testingOriginalTask = new Task("Testing original Task", false);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testingOriginalTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.updateTask(1L, taskRequestDto);

        assertEquals(taskRequestDto.title(), result.getTitle());
    }

    @Test
    public void updateTask_shouldThrowException_whenTaskDoesNotExist() {
        TaskRequestDto taskRequestDto = new TaskRequestDto("Testing Update Task", false);
        when(taskRepository.findById(69L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(69L, taskRequestDto));
    }

    @Test
    public void deleteTask_shouldCallRepositoryDelete() {

        Task taskToDelete = new Task("Testing Task", false);
        taskToDelete.setId(6769L); //To make sure id is not null, otherwise id will be null
        when(taskRepository.findById(6769L)).thenReturn(Optional.of(taskToDelete));

        taskService.deleteTaskById(6769L);
        verify(taskRepository).deleteById(6769L);
    }

}