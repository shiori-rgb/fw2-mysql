package com.example.todoapp;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AutomationRule {
    private Long id;
    private String name;
    private String conditionType;
    private String conditionValue;
    private String conditionDetail;
    private Long targetProjectId;
    private String targetCategory;
    private String actionType;
    private String actionValue;
    private String actionDetail;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastExecutedAt;
    private Integer executionCount;
}
