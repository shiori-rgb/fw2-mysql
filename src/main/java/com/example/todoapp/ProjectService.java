package com.example.todoapp;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    private final ProjectMapper projectMapper;

    public ProjectService(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    public List<Project> findActive() {
        return withStats(projectMapper.findAll(false));
    }

    public List<Project> findAll() {
        return withStats(projectMapper.findAll(true));
    }

    public Project findById(Long id) {
        Project project = projectMapper.findById(id);
        return project == null ? null : withStats(project);
    }

    public int create(Project project) {
        if (project.getIcon() == null || project.getIcon().isBlank()) project.setIcon("●");
        if (project.getColor() == null || project.getColor().isBlank()) project.setColor("#596579");
        int result = projectMapper.insert(project);
        ProjectSection section = new ProjectSection();
        section.setProjectId(project.getId());
        section.setName("受信トレイ");
        section.setSortOrder(0);
        projectMapper.insertSection(section);
        return result;
    }

    public int update(Project project) {
        return projectMapper.update(project);
    }

    public int delete(Long id) {
        return projectMapper.deleteById(id);
    }

    public int archive(Long id, boolean archived) {
        return projectMapper.archive(id, archived);
    }

    public int toggleFavorite(Long id) {
        return projectMapper.toggleFavorite(id);
    }

    public List<ProjectSection> findSections(Long projectId) {
        return projectMapper.findSections(projectId);
    }

    public int addSection(Long projectId, String name) {
        ProjectSection section = new ProjectSection();
        section.setProjectId(projectId);
        section.setName(name);
        section.setSortOrder(projectMapper.findSections(projectId).size());
        return projectMapper.insertSection(section);
    }

    public List<Todo> findTasks(Long projectId) {
        return projectMapper.findTasks(projectId);
    }

    private Project withStats(Project project) {
        project.setTotalTaskCount(projectMapper.countTasks(project.getId()));
        project.setCompletedTaskCount(projectMapper.countCompletedTasks(project.getId()));
        return project;
    }

    private List<Project> withStats(List<Project> projects) {
        projects.forEach(this::withStats);
        return projects;
    }
}
