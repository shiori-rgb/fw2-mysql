package com.example.todoapp;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String calendar(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            Model model) {
        YearMonth now = YearMonth.now();
        YearMonth current = YearMonth.of(
                year == null ? now.getYear() : year,
                month == null ? now.getMonthValue() : month);
        LocalDate from = current.atDay(1);
        LocalDate to = current.atEndOfMonth();

        model.addAttribute("year", current.getYear());
        model.addAttribute("month", current.getMonthValue());
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("previous", current.minusMonths(1));
        model.addAttribute("next", current.plusMonths(1));
        model.addAttribute("weeks", createWeeks(current));
        return "calendar";
    }

    private List<List<CalendarCell>> createWeeks(YearMonth month) {
        int leadingEmptyCells = month.atDay(1).getDayOfWeek().getValue() % 7;
        int cellCount = ((leadingEmptyCells + month.lengthOfMonth() + 6) / 7) * 7;
        List<List<CalendarCell>> weeks = new ArrayList<>();

        for (int cellIndex = 0; cellIndex < cellCount; cellIndex += 7) {
            List<CalendarCell> week = new ArrayList<>();
            for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
                int day = cellIndex + dayIndex - leadingEmptyCells + 1;
                week.add(day < 1 || day > month.lengthOfMonth()
                        ? null
                        : new CalendarCell(month.atDay(day)));
            }
            weeks.add(week);
        }
        return weeks;
    }

    public record CalendarCell(LocalDate date) {
        public int day() {
            return date.getDayOfMonth();
        }
    }
}
