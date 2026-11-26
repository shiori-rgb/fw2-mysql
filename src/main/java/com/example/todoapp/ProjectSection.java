package com.example.todoapp;

import lombok.Data;

@Data
public class ProjectSection {

    private Long id;
    private Long projectId;
    private String name;
    private int sortOrder;
}
