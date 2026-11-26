package com.example.todoapp;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ProductivityReport {
    private String from;
    private String to;
    private Long totalCount;
    private Long completedCount;
    private Long incompleteCount;
    private Long overdueCount;
    private Long scheduleChangeCount;
    private Double completionRate;
    private Integer streakDays;
    private Long plannedMinutes;
    private Long actualMinutes;
    private Double averageActualMinutes;
    private Double estimateAccuracy;
    private Double onTimeExecutionRate;
    private Double onTimeCompletionRate;
    private Long previousCompletedCount;
    private Double previousCompletionRate;
    private final List<SeriesPoint> trend = new ArrayList<>();
    private final List<NamedValue> categoryMinutes = new ArrayList<>();
    private final List<NamedValue> projectMinutes = new ArrayList<>();
    private final List<NamedValue> weekdayMinutes = new ArrayList<>();
    private final List<HeatPoint> heatmap = new ArrayList<>();
    private final List<NamedValue> priorityRates = new ArrayList<>();

    public record SeriesPoint(String label, long planned, long actual, double completionRate) {}
    public record NamedValue(String label, long value) {}
    public record HeatPoint(String label, int day, int hour, long value) {}
}
