package com.example.todoapp;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoMapper {

    List<Todo> search(@Param("keyword") String keyword, @Param("category") String category,
            @Param("order") String order);

    int insert(Todo todo);

    Todo findById(Long id);

    int update(Todo todo);

    int deleteById(Long id);
}
