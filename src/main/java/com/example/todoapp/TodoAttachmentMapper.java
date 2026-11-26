package com.example.todoapp;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoAttachmentMapper {

    List<TodoAttachment> findByTodoId(Long todoId);

    TodoAttachment findById(Long id);

    int insert(TodoAttachment attachment);

    int deleteById(Long id);

    int deleteByTodoId(Long todoId);
}
