package com.example.todoapp;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.todoapp.api.WeatherClient;

@Service
public class AutomationRuleService {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private final AutomationRuleMapper mapper;
    private final TodoMapper todoMapper;
    private final ProjectService projectService;
    private final WeatherClient weatherClient;

    public AutomationRuleService(AutomationRuleMapper mapper, TodoMapper todoMapper, ProjectService projectService,
            WeatherClient weatherClient) {
        this.mapper = mapper; this.todoMapper = todoMapper; this.projectService = projectService; this.weatherClient = weatherClient;
    }

    public List<AutomationRule> findAll() { return mapper.findAll(); }
    public AutomationRule findById(Long id) { return mapper.findById(id); }
    public List<AutomationRuleRun> findRuns(Long id) { return mapper.findRuns(id); }
    public void save(AutomationRule rule) { if (rule.getEnabled() == null) rule.setEnabled(false); mapper.insert(rule); }
    public void update(AutomationRule rule) { if (rule.getEnabled() == null) rule.setEnabled(false); mapper.update(rule); }
    public void delete(Long id) { mapper.deleteById(id); }
    public void setEnabled(Long id, boolean enabled) { mapper.setEnabled(id, enabled); }

    @Scheduled(fixedDelayString = "${app.automation.interval-ms:60000}")
    public void executeEnabledRules() {
        for (AutomationRule rule : mapper.findAll()) if (Boolean.TRUE.equals(rule.getEnabled())) execute(rule, false);
    }

    public List<String> test(Long id) {
        AutomationRule rule = mapper.findById(id);
        return rule == null ? List.of("ルールが見つかりません。") : execute(rule, true);
    }

    @Transactional
    protected List<String> execute(AutomationRule rule, boolean dryRun) {
        List<Match> matches = matches(rule);
        List<String> result = new ArrayList<>();
        for (Match match : matches) {
            String runKey = match.runKey();
            if (dryRun) { result.add(match.description()); continue; }
            try {
                apply(rule, match.todo());
                AutomationRuleRun run = new AutomationRuleRun(); run.setRuleId(rule.getId()); run.setTodoId(match.todo() == null ? null : match.todo().getId()); run.setRunKey(runKey); run.setStatus("SUCCESS"); run.setMessage(match.description());
                if (mapper.insertRun(run) > 0) mapper.updateExecution(rule.getId());
            } catch (RuntimeException ex) {
                AutomationRuleRun run = new AutomationRuleRun(); run.setRuleId(rule.getId()); run.setTodoId(match.todo() == null ? null : match.todo().getId()); run.setRunKey(runKey); run.setStatus("FAILED"); run.setMessage(ex.getMessage()); mapper.insertRun(run);
            }
        }
        return result;
    }

    private List<Match> matches(AutomationRule r) {
        LocalDateTime now = LocalDateTime.now(); LocalDate today = now.toLocalDate(); List<Match> out = new ArrayList<>();
        String type = safe(r.getConditionType());
        if (List.of("DAILY_TIME", "WEEKLY_TIME", "DAILY_WORK_MINUTES", "DAILY_COMPLETION", "UNCOMPLETED_COUNT", "RAIN_PROBABILITY").contains(type)) {
            boolean ok = switch (type) {
                case "DAILY_TIME" -> timeEquals(r.getConditionValue(), now.toLocalTime());
                case "WEEKLY_TIME" -> weeklyEquals(r.getConditionValue(), now);
                case "DAILY_WORK_MINUTES" -> mapper.sumActualMinutesToday() >= number(r.getConditionValue());
                case "DAILY_COMPLETION" -> mapper.countCompletedToday() >= number(r.getConditionValue());
                case "UNCOMPLETED_COUNT" -> mapper.countUncompleted() > number(r.getConditionValue());
                case "RAIN_PROBABILITY" -> weatherClient.fetchRainProbability(today).orElse(-1) >= number(r.getConditionValue());
                default -> false;
            };
            if (ok) out.add(new Match(null, "global:" + type + ":" + today + ":" + now.toLocalTime().withSecond(0).withNano(0), type + " の条件に一致しました。"));
            return out;
        }
        for (Todo todo : mapper.findActiveTodos()) {
            if (!targetMatches(r, todo)) continue;
            boolean ok = switch (type) {
                case "OVERDUE" -> !Boolean.TRUE.equals(todo.getCompleted()) && todo.getDueDate() != null && todo.getDueDate().isBefore(today);
                case "COMPLETED" -> Boolean.TRUE.equals(todo.getCompleted()) && todo.getCompletedAt() != null && todo.getCompletedAt().isAfter(now.minusMinutes(2));
                case "CREATED" -> todo.getCreatedAt() != null && todo.getCreatedAt().isAfter(now.minusMinutes(2));
                case "START_TIME" -> todo.getScheduledDate() != null && todo.getScheduledDate().equals(today) && todo.getPlannedStartTime() != null && Math.abs(java.time.Duration.between(todo.getPlannedStartTime(), now.toLocalTime()).toMinutes()) < 1;
                case "WORK_MINUTES_OVER" -> todo.getActualSeconds() != null && todo.getActualSeconds() / 60 > number(r.getConditionValue());
                default -> false;
            };
            if (ok) out.add(new Match(todo, "todo:" + todo.getId() + ":" + type + ":" + (todo.getCompletedAt() == null ? today : todo.getCompletedAt()), todo.getTitle() + " に条件が一致しました。"));
        }
        if ("PROJECT_COMPLETION".equals(type) && r.getTargetProjectId() != null) {
            Project p = projectService.findById(r.getTargetProjectId());
            if (p != null && p.getTotalTaskCount() > 0 && p.getCompletedTaskCount() * 100 >= number(r.getConditionValue()) * p.getTotalTaskCount()) out.add(new Match(null, "project:" + p.getId() + ":" + today, p.getName() + " の達成条件に一致しました。"));
        }
        return out;
    }

    private void apply(AutomationRule r, Todo todo) {
        String action = safe(r.getActionType()); String value = r.getActionValue();
        if (todo != null) switch (action) {
            case "CHANGE_PRIORITY" -> mapper.updateTodoPriority(todo.getId(), number(value));
            case "CHANGE_CATEGORY" -> mapper.updateTodoCategory(todo.getId(), value);
            case "MOVE_PROJECT" -> mapper.updateTodoProject(todo.getId(), value == null || value.isBlank() ? null : Long.valueOf(value));
            default -> { }
        }
        if ("CREATE_TASK".equals(action)) { Todo created = new Todo(); created.setTitle(value == null || value.isBlank() ? "自動化ルールで作成されたタスク" : value); created.setCategory(r.getTargetCategory() == null ? "デザイン" : r.getTargetCategory()); created.setPriority(2); created.setCompleted(false); todoMapper.insert(created); }
    }
    private boolean targetMatches(AutomationRule r, Todo t) { return (r.getTargetProjectId() == null || r.getTargetProjectId().equals(t.getProjectId())) && (r.getTargetCategory() == null || r.getTargetCategory().isBlank() || r.getTargetCategory().equals(t.getCategory())); }
    private boolean timeEquals(String value, LocalTime now) { try { return LocalTime.parse(value, TIME).withSecond(0).equals(now.withSecond(0).withNano(0)); } catch (Exception e) { return false; } }
    private boolean weeklyEquals(String value, LocalDateTime now) { if(value==null||!value.contains("|"))return false; String[] p=value.split("\\|",2); try{return DayOfWeek.valueOf(p[0].toUpperCase()).equals(now.getDayOfWeek())&&timeEquals(p[1],now.toLocalTime());}catch(Exception e){return false;} }
    private int number(String s) { try { return Integer.parseInt(s == null ? "0" : s); } catch (NumberFormatException e) { return 0; } }
    private String safe(String s) { return s == null ? "" : s; }
    private record Match(Todo todo, String runKey, String description) { }
}
