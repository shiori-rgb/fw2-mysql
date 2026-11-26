package com.example.todoapp;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class QuickAddParser {

    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");
    private static final Pattern TIME = Pattern.compile("(\\d{1,2})時(?:(\\d{1,2})分)?");
    private static final Pattern HOURS = Pattern.compile("(\\d+)時間(?:半)?");
    private static final Pattern MINUTES = Pattern.compile("(\\d+)分");
    private static final Pattern PRIORITY = Pattern.compile("優先度\\s*(?:が\\s*)?(高|中|低)");
    private static final Pattern PROJECT = Pattern.compile("#([^\\s#]+)");
    private static final Pattern FULL_DATE = Pattern.compile("(?:(\\d{4})年)?(\\d{1,2})月(\\d{1,2})日");
    private static final Pattern WEEKDAY = Pattern.compile("(今週|来週)?(日|月|火|水|木|金|土)曜?日?");

    private final Clock clock;

    public QuickAddParser() {
        this(Clock.system(TOKYO));
    }

    QuickAddParser(Clock clock) {
        this.clock = clock;
    }

    public QuickAddParseResult parse(String input) {
        String remaining = input == null ? "" : input.trim();
        LocalDate today = LocalDate.now(clock);
        LocalDate scheduledDate = null;
        LocalDate dueDate = null;
        LocalTime start = null;
        LocalTime end = null;
        Integer plannedMinutes = null;
        Integer priority = null;
        String projectName = null;
        List<String> recognized = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        Matcher projectMatcher = PROJECT.matcher(remaining);
        if (projectMatcher.find()) {
            projectName = projectMatcher.group(1);
            recognized.add(projectMatcher.group());
            remaining = projectMatcher.replaceAll(" ");
        }

        Matcher priorityMatcher = PRIORITY.matcher(remaining);
        if (priorityMatcher.find()) {
            priority = switch (priorityMatcher.group(1)) {
            case "高" -> 1;
            case "低" -> 3;
            default -> 2;
            };
            recognized.add(priorityMatcher.group());
            remaining = priorityMatcher.replaceAll(" ");
        }

        Matcher durationMatcher = HOURS.matcher(remaining);
        if (durationMatcher.find()) {
            plannedMinutes = Integer.parseInt(durationMatcher.group(1)) * 60;
            if (durationMatcher.group().endsWith("半")) plannedMinutes += 30;
            recognized.add(durationMatcher.group());
            remaining = durationMatcher.replaceAll(" ");
        }
        Matcher minuteMatcher = MINUTES.matcher(remaining);
        if (minuteMatcher.find() && plannedMinutes == null) {
            plannedMinutes = Integer.parseInt(minuteMatcher.group(1));
            recognized.add(minuteMatcher.group());
            remaining = minuteMatcher.replaceAll(" ");
        }

        Matcher timeMatcher = TIME.matcher(remaining);
        if (timeMatcher.find()) {
            int hour = Integer.parseInt(timeMatcher.group(1));
            int minute = timeMatcher.group(2) == null ? 0 : Integer.parseInt(timeMatcher.group(2));
            if (hour > 23 || minute > 59) {
                warnings.add("時刻を確認してください");
            } else {
                start = LocalTime.of(hour, minute);
                if (plannedMinutes != null) end = start.plusMinutes(plannedMinutes);
                recognized.add(timeMatcher.group());
                remaining = timeMatcher.replaceAll(" ");
            }
        }

        DateMatch dateMatch = findDate(remaining, today);
        if (dateMatch != null) {
            if (dateMatch.ambiguous()) {
                warnings.add("日付があいまいです。プレビューで指定してください");
            } else if (dateMatch.due()) {
                dueDate = dateMatch.date();
            } else {
                scheduledDate = dateMatch.date();
            }
            recognized.add(dateMatch.text());
            remaining = remaining.replace(dateMatch.text(), " ");
        }

        if (start != null && plannedMinutes != null && end == null) end = start.plusMinutes(plannedMinutes);
        if (start != null && scheduledDate == null && dueDate == null) {
            warnings.add("開始時刻の日付が指定されていません");
        }
        if (scheduledDate != null && start != null && start.plusMinutes(plannedMinutes == null ? 0 : plannedMinutes).isAfter(LocalTime.MAX)) {
            warnings.add("終了時刻が翌日にまたがります。プレビューで確認してください");
        }

        String title = remaining.replaceAll("\\s+", " ").trim();
        String previousTitle;
        do {
            previousTitle = title;
            title = title.replaceFirst("^(から|に|の|までに)\\s*", "").trim();
        } while (!title.equals(previousTitle));
        title = title.replaceAll("を$", "");
        if (title.isBlank()) warnings.add("タスク名を入力してください");
        return new QuickAddParseResult(title, scheduledDate, dueDate, start, end, plannedMinutes, priority,
                projectName, recognized, warnings);
    }

    private DateMatch findDate(String text, LocalDate today) {
        Matcher relative = Pattern.compile("(今日|明日|明後日)(までに)?").matcher(text);
        if (relative.find()) {
            LocalDate date = switch (relative.group(1)) {
            case "明日" -> today.plusDays(1);
            case "明後日" -> today.plusDays(2);
            default -> today;
            };
            return new DateMatch(relative.group(), date, relative.group(2) != null, false);
        }
        Matcher fullDate = FULL_DATE.matcher(text);
        if (fullDate.find()) {
            int year = fullDate.group(1) == null ? today.getYear() : Integer.parseInt(fullDate.group(1));
            LocalDate date = LocalDate.of(year, Integer.parseInt(fullDate.group(2)), Integer.parseInt(fullDate.group(3)));
            if (fullDate.group(1) == null && date.isBefore(today)) date = date.plusYears(1);
            String suffix = text.substring(fullDate.end());
            return new DateMatch(fullDate.group() + (suffix.startsWith("までに") ? "までに" : ""), date,
                    suffix.startsWith("までに"), false);
        }
        Matcher weekday = WEEKDAY.matcher(text);
        if (weekday.find()) {
            if (weekday.group(1) == null) return new DateMatch(weekday.group(), null, false, true);
            DayOfWeek day = DayOfWeek.of("日月火水木金土".indexOf(weekday.group(2)) == 0 ? 7 : "日月火水木金土".indexOf(weekday.group(2)));
            LocalDate base = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
            if ("来週".equals(weekday.group(1))) base = base.plusWeeks(1);
            return new DateMatch(weekday.group(), base.with(TemporalAdjusters.nextOrSame(day)), false, false);
        }
        if (text.contains("今週") || text.contains("来週")) return new DateMatch("", null, false, true);
        return null;
    }

    private record DateMatch(String text, LocalDate date, boolean due, boolean ambiguous) {
    }
}
