package com.example.todoapp.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.example.todoapp.Todo;

public class TodoDto {

    private Long id;
    private String title;
    private String detail;
    private String category;
    private Integer priority;
    private LocalDate dueDate;
    private LocalDate scheduledDate;
    private LocalTime plannedStartTime;
    private LocalTime plannedEndTime;
    private Integer plannedMinutes;
    private Integer actualSeconds;
    private LocalDateTime timerStartedAt;
    private String timerStatus;
    private String projectColor;
    private Integer scheduleChangeCount;
    private Long projectId;
    private Long sectionId;
    private Boolean completed;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TodoDto from(Todo todo) {
        TodoDto dto = new TodoDto();
        dto.id = todo.getId();
        dto.title = todo.getTitle();
        dto.detail = todo.getDetail();
        dto.category = todo.getCategory();
        dto.priority = todo.getPriority();
        dto.dueDate = todo.getDueDate();
        dto.scheduledDate = todo.getScheduledDate();
        dto.plannedStartTime = todo.getPlannedStartTime();
        dto.plannedEndTime = todo.getPlannedEndTime();
        dto.plannedMinutes = todo.getPlannedMinutes();
        dto.actualSeconds = todo.getActualSeconds();
        dto.timerStartedAt = todo.getTimerStartedAt();
        dto.timerStatus = todo.getTimerStatus();
        dto.projectColor = todo.getProjectColor();
        dto.scheduleChangeCount = todo.getScheduleChangeCount();
        dto.projectId = todo.getProjectId();
        dto.sectionId = todo.getSectionId();
        dto.completed = todo.getCompleted();
        dto.completedAt = todo.getCompletedAt();
        dto.createdAt = todo.getCreatedAt();
        dto.updatedAt = todo.getUpdatedAt();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public String getCategory() {
        return category;
    }

    public Integer getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public LocalTime getPlannedStartTime() { return plannedStartTime; }
    public LocalTime getPlannedEndTime() { return plannedEndTime; }
    public Integer getPlannedMinutes() { return plannedMinutes; }
    public Integer getActualSeconds() { return actualSeconds; }
    public LocalDateTime getTimerStartedAt() { return timerStartedAt; }
    public String getTimerStatus() { return timerStatus; }
    public String getProjectColor() { return projectColor; }
    public Integer getScheduleChangeCount() { return scheduleChangeCount; }

    public Long getProjectId() {
        return projectId;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
