package com.example.todoapp;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Project {

    private Long id;

    @NotBlank(message = "{project.name.required}")
    @Size(max = 255, message = "{project.name.size}")
    private String name;

    @Size(max = 1000, message = "{project.description.size}")
    private String description;

    private LocalDate targetDate;
    private String icon;
    private String color;
    private Boolean favorite;
    private Boolean archived;
    private int totalTaskCount;
    private int completedTaskCount;
}
