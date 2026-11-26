package com.example.todoapp;

import java.time.LocalDate;

import lombok.Data;

@Data
public class DailyCompletedCount {

    private LocalDate completedDate;
    private int count;
}
