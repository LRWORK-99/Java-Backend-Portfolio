package com.java_refresh.day4_demo.service;

import com.java_refresh.day4_demo.dto.TaskRequestDto;
import com.java_refresh.day4_demo.exceptions.TaskNotFoundException;
import com.java_refresh.day4_demo.model.Task;
import com.java_refresh.day4_demo.model.User;
import com.java_refresh.day4_demo.repository.TaskRepository;
import com.java_refresh.day4_demo.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskService taskService;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User("testUser", "hashedPassword", "USER");
        testUser.setId(1L);

        // Simulate "someone is logged in as testUser" for every test
        lenient().when(authentication.getName()).thenReturn("testUser");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getTaskById_shouldReturnTask_whenTaskExists() {
        Task testTask = new Task("Testing Task", false, testUser);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        //Tell the mock taskRepo what to do if findByID method is being called
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        Task result = taskService.getTaskById(1L);

        assertEquals(testTask.getTitle(), result.getTitle());

    }

    @Test
    public void getTaskById_shouldThrowException_whenTaskDoesNotExist() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(67L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(67L));
    }

    @Test
    public void getTaskById_shouldThrowException_whenTaskBelongsToAnotherUser() {
        User anotherUser = new User("anotherUser", "hashedPassword", "USER");
        anotherUser.setId(2L);

        Task someoneElseTask = new Task("someoneElseTask", false, anotherUser);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(someoneElseTask));

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(5L));
    }

    @Test
    public void saveTask() {
        TaskRequestDto taskRequestDto = new TaskRequestDto("Testing Task Dto", false);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        //invocation.getArgument(0) grabs whatever object was actually passed as the first argument to save(...) — and returns that same object back.
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.saveTask(taskRequestDto);

        assertEquals(taskRequestDto.title(), result.getTitle());
    }

    @Test
    public void updateTask() {
        TaskRequestDto taskRequestDto = new TaskRequestDto("Testing Update Task", false);
        Task testingOriginalTask = new Task("Testing original Task", false, testUser);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testingOriginalTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.updateTask(1L, taskRequestDto);

        assertEquals(taskRequestDto.title(), result.getTitle());
    }

    @Test
    public void updateTask_shouldThrowException_whenTaskDoesNotExist() {
        TaskRequestDto taskRequestDto = new TaskRequestDto("Testing Update Task", false);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(69L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(69L, taskRequestDto));
    }

    @Test
    public void deleteTask_shouldCallRepositoryDelete() {

        Task taskToDelete = new Task("Testing Task", false, testUser);
        taskToDelete.setId(6769L); //To make sure id is not null, otherwise id will be null

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(6769L)).thenReturn(Optional.of(taskToDelete));

        taskService.deleteTaskById(6769L);
        verify(taskRepository).deleteById(6769L);
    }

}