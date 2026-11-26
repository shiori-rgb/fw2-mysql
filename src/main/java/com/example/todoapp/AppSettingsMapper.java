package com.example.todoapp;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppSettingsMapper {
    AppSettings find();
    int insert(AppSettings settings);
    int update(AppSettings settings);
}
