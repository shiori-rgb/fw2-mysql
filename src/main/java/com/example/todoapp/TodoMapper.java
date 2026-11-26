package com.example.todoapp;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoMapper {

    List<Todo> search(@Param("keyword") String keyword, @Param("category") String category,
            @Param("order") String order, @Param("from") LocalDate from, @Param("to") LocalDate to,
            @Param("showCompleted") boolean showCompleted, @Param("completedOnly") boolean completedOnly,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit, @Param("trash") boolean trash);

    int count(@Param("keyword") String keyword, @Param("category") String category,
            @Param("showCompleted") boolean showCompleted, @Param("completedOnly") boolean completedOnly,
            @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("trash") boolean trash);

    int insert(Todo todo);

    Todo findById(Long id);

    int update(Todo todo);

    int deleteById(Long id);

    int restoreById(Long id);

    int togglePinned(Long id);

    int updateTimer(Todo todo);

    int updateSchedule(Todo todo);
}
