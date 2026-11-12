package com.example.todoapp;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoMapper {

    List<Todo> findAll();

    int insert(Todo todo);

    Todo findById(Long id);

    int update(Todo todo);

    int deleteById(Long id);
}
