package com.example.todoapp;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Sprint {

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}
