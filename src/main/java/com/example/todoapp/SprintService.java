package com.example.todoapp;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class SprintService {

    private final SprintMapper sprintMapper;

    public SprintService(SprintMapper sprintMapper) {
        this.sprintMapper = sprintMapper;
    }

    public List<Sprint> findAll() {
        return sprintMapper.findAll();
    }

    public Sprint findById(Long id) {
        return sprintMapper.findById(id);
    }

    public List<BurndownPoint> createBurndown(Sprint sprint) {
        int total = sprintMapper.countTodos(sprint.getStartDate(), sprint.getEndDate());
        List<DailyCompletedCount> completedCounts = sprintMapper.countCompletedByDate(
                sprint.getStartDate(), sprint.getEndDate());
        Map<LocalDate, Integer> completedByDate = new HashMap<>();
        for (DailyCompletedCount completedCount : completedCounts) {
            completedByDate.put(completedCount.getCompletedDate(), completedCount.getCount());
        }

        long dayCount = ChronoUnit.DAYS.between(sprint.getStartDate(), sprint.getEndDate()) + 1;
        List<BurndownPoint> points = new java.util.ArrayList<>();
        int cumulativeCompleted = 0;
        for (int day = 0; day < dayCount; day++) {
            LocalDate date = sprint.getStartDate().plusDays(day);
            cumulativeCompleted += completedByDate.getOrDefault(date, 0);
            double ideal = dayCount == 1 ? 0 : total * (dayCount - 1 - day) / (double) (dayCount - 1);
            points.add(new BurndownPoint(date, Math.max(0, total - cumulativeCompleted), ideal));
        }
        return points;
    }

    public String status(List<BurndownPoint> points) {
        if (points.isEmpty()) {
            return "on-track";
        }
        BurndownPoint latest = points.get(points.size() - 1);
        if (latest.getRemaining() < latest.getIdeal()) {
            return "ahead";
        }
        if (latest.getRemaining() > latest.getIdeal()) {
            return "delayed";
        }
        return "on-track";
    }
}
