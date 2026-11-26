package com.example.todoapp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record QuickAddParseResult(
        String title,
        LocalDate scheduledDate,
        LocalDate dueDate,
        LocalTime plannedStartTime,
        LocalTime plannedEndTime,
        Integer plannedMinutes,
        Integer priority,
        String projectName,
        List<String> recognized,
        List<String> warnings) {
}
