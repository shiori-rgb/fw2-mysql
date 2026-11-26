package com.example.todoapp;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BurndownPoint {

    private LocalDate date;
    private int remaining;
    private double ideal;
}
