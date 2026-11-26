package com.example.todoapp;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SprintMapper {

    List<Sprint> findAll();

    Sprint findById(Long id);

    int countTodos(@Param("from") LocalDate from, @Param("to") LocalDate to);

    List<DailyCompletedCount> countCompletedByDate(@Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
