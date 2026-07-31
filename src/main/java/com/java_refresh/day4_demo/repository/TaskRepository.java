package com.java_refresh.day4_demo.repository;

import com.java_refresh.day4_demo.dto.TaskRequestDto;
import com.java_refresh.day4_demo.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByCompleted(boolean completed, Pageable pageable);
}
