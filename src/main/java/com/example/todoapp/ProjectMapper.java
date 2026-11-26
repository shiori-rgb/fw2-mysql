package com.example.todoapp;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectMapper {

    List<Project> findAll(@Param("includeArchived") boolean includeArchived);

    Project findById(Long id);

    int insert(Project project);

    int update(Project project);

    int deleteById(Long id);

    int archive(@Param("id") Long id, @Param("archived") boolean archived);

    int toggleFavorite(Long id);

    List<ProjectSection> findSections(Long projectId);

    int insertSection(ProjectSection section);

    List<Todo> findTasks(Long projectId);

    int countTasks(Long projectId);

    int countCompletedTasks(Long projectId);
}
