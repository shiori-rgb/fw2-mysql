package com.example.todoapp;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoMapper {

    List<Todo> search(@Param("keyword") String keyword, @Param("category") String category,
            @Param("order") String order, @Param("from") LocalDate from, @Param("to") LocalDate to,
            @Param("showCompleted") boolean showCompleted, @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    int count(@Param("keyword") String keyword, @Param("category") String category,
            @Param("showCompleted") boolean showCompleted);

    int insert(Todo todo);

    Todo findById(Long id);

    int update(Todo todo);

    int deleteById(Long id);
}
