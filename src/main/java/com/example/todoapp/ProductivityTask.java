package com.example.todoapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Data;

@Data
public class ProductivityTask {
    private Long id;
    private String category;
    private Integer priority;
    private LocalDate dueDate;
    private LocalDate scheduledDate;
    private LocalTime plannedStartTime;
    private Integer plannedMinutes;
    private Integer actualSeconds;
    private Boolean completed;
    private LocalDateTime completedAt;
    private Long projectId;
    private String projectName;
}
