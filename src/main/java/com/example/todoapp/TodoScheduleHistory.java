package com.example.todoapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Data;

@Data
public class TodoScheduleHistory {
    private Long id;
    private Long todoId;
    private LocalDateTime changedAt;
    private LocalDate oldScheduledDate;
    private LocalDate newScheduledDate;
    private LocalDate oldDueDate;
    private LocalDate newDueDate;
    private LocalTime oldPlannedStartTime;
    private LocalTime newPlannedStartTime;
    private LocalTime oldPlannedEndTime;
    private LocalTime newPlannedEndTime;
    private Integer oldPlannedMinutes;
    private Integer newPlannedMinutes;
}
