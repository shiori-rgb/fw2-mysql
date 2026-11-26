package com.example.todoapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductivityMapper {
    List<ProductivityTask> findTasks(@Param("from") LocalDate from, @Param("to") LocalDate to,
            @Param("category") String category, @Param("projectId") Long projectId);

    long countScheduleChanges(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
            @Param("category") String category, @Param("projectId") Long projectId);

    List<String> findCategories();
}
