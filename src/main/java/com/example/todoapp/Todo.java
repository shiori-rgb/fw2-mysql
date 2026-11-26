package com.example.todoapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class Todo {

    private Long id;

    @NotBlank(message = "{todo.title.required}")
    @Size(max = 255, message = "{todo.title.size}")
    private String title;

    @Size(max = 255, message = "{todo.detail.size}")
    private String detail;

    @NotBlank(message = "{todo.category.required}")
    private String category;

    @NotNull(message = "{todo.priority.required}")
    private Integer priority;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduledDate;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime plannedStartTime;

    @DateTimeFormat(pattern = "HH:mm")
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
    private LocalDateTime deletedAt;
    private Boolean pinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
