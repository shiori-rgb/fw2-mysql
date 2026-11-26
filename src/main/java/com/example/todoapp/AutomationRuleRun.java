package com.example.todoapp;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AutomationRuleRun {
    private Long id;
    private Long ruleId;
    private Long todoId;
    private String runKey;
    private String status;
    private String message;
    private LocalDateTime executedAt;
}
