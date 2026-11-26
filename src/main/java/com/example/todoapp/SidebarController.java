package com.example.todoapp;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SidebarController {

    private final TodoService todoService;

    public SidebarController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/api/sidebar/counts")
    public Map<String, Integer> counts() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() % 7);
        LocalDate weekEnd = weekStart.plusDays(6);
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("inbox", incomplete(null, null));
        counts.put("today", incomplete(today, today));
        counts.put("tomorrow", incomplete(today.plusDays(1), today.plusDays(1)));
        counts.put("week", incomplete(weekStart, weekEnd));
        counts.put("upcoming", incomplete(today.plusDays(1), null));
        counts.put("overdue", incomplete(null, today.minusDays(1)));
        counts.put("all", incomplete(null, null));
        counts.put("completed", todoService.count(null, null, null, null, true, true, false));
        counts.put("deleted", todoService.count(null, null, null, null, false, false, true));
        return counts;
    }

    private int incomplete(LocalDate from, LocalDate to) {
        return todoService.count(null, null, from, to, false, false, false);
    }
}
