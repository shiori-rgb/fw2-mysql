package com.example.todoapp;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

@Service
public class ProductivityService {
    private final ProductivityMapper mapper;

    public ProductivityService(ProductivityMapper mapper) { this.mapper = mapper; }

    public ProductivityReport report(LocalDate from, LocalDate to, String category, Long projectId, String granularity) {
        if (from == null) from = LocalDate.now().withDayOfMonth(1);
        if (to == null) to = from.plusMonths(1).minusDays(1);
        if (to.isBefore(from)) { LocalDate tmp = from; from = to; to = tmp; }
        List<ProductivityTask> tasks = mapper.findTasks(from, to, category, projectId);
        ProductivityReport r = new ProductivityReport();
        r.setFrom(from.toString()); r.setTo(to.toString());
        long total = tasks.size(), completed = tasks.stream().filter(t -> Boolean.TRUE.equals(t.getCompleted())).count();
        long incomplete = total - completed;
        long overdue = tasks.stream().filter(t -> !Boolean.TRUE.equals(t.getCompleted()) && t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now())).count();
        long planned = tasks.stream().mapToLong(t -> t.getPlannedMinutes() == null ? 0 : t.getPlannedMinutes()).sum();
        long actualSeconds = tasks.stream().mapToLong(t -> t.getActualSeconds() == null ? 0 : t.getActualSeconds()).sum();
        long actual = Math.round(actualSeconds / 60.0);
        r.setTotalCount(total); r.setCompletedCount(completed); r.setIncompleteCount(incomplete); r.setOverdueCount(overdue);
        r.setCompletionRate(total == 0 ? null : completed * 100.0 / total);
        r.setPlannedMinutes(planned); r.setActualMinutes(actual);
        r.setAverageActualMinutes(completed == 0 ? null : actual / (double) completed);
        r.setEstimateAccuracy(planned == 0 ? null : Math.max(0, 100 - Math.abs(actual - planned) * 100.0 / planned));
        r.setScheduleChangeCount(mapper.countScheduleChanges(from.atStartOfDay(), to.plusDays(1).atStartOfDay(), category, projectId));
        r.setOnTimeCompletionRate(rate(tasks, true));
        r.setOnTimeExecutionRate(executionRate(tasks));
        r.setStreakDays(streak(tasks, to));
        LocalDate previousTo = from.minusDays(1), previousFrom = previousTo.minusDays(ChronoUnit.DAYS.between(from, to));
        List<ProductivityTask> previous = mapper.findTasks(previousFrom, previousTo, category, projectId);
        long previousCompleted = previous.stream().filter(t -> Boolean.TRUE.equals(t.getCompleted())).count();
        r.setPreviousCompletedCount(previousCompleted);
        r.setPreviousCompletionRate(previous.isEmpty() ? null : previousCompleted * 100.0 / previous.size());
        buildSeries(r, tasks, from, to, granularity);
        buildBreakdowns(r, tasks);
        return r;
    }

    private Double rate(List<ProductivityTask> tasks, boolean due) {
        List<ProductivityTask> eligible = tasks.stream().filter(t -> Boolean.TRUE.equals(t.getCompleted()) && t.getCompletedAt() != null && (due ? t.getDueDate() != null : t.getScheduledDate() != null)).toList();
        if (eligible.isEmpty()) return null;
        long ok = eligible.stream().filter(t -> due ? !t.getCompletedAt().toLocalDate().isAfter(t.getDueDate()) : t.getCompletedAt().toLocalDate().equals(t.getScheduledDate())).count();
        return ok * 100.0 / eligible.size();
    }
    private Double executionRate(List<ProductivityTask> tasks) { return rate(tasks, false); }

    private int streak(List<ProductivityTask> tasks, LocalDate end) {
        Map<LocalDate, Boolean> completed = new HashMap<>();
        tasks.stream().filter(t -> Boolean.TRUE.equals(t.getCompleted()) && t.getCompletedAt() != null).forEach(t -> completed.put(t.getCompletedAt().toLocalDate(), true));
        int n = 0; for (LocalDate d = end; completed.containsKey(d); d = d.minusDays(1)) n++; return n;
    }

    private void buildSeries(ProductivityReport r, List<ProductivityTask> tasks, LocalDate from, LocalDate to, String granularity) {
        Map<String, List<ProductivityTask>> groups = new TreeMap<>();
        for (ProductivityTask t : tasks) { LocalDate d = t.getScheduledDate() != null ? t.getScheduledDate() : (t.getDueDate() != null ? t.getDueDate() : t.getCompletedAt() == null ? null : t.getCompletedAt().toLocalDate()); if (d == null) continue; String key = key(d, granularity); groups.computeIfAbsent(key, k -> new ArrayList<>()).add(t); }
        groups.forEach((k, list) -> { long p=list.stream().mapToLong(t -> t.getPlannedMinutes()==null?0:t.getPlannedMinutes()).sum(); long a=Math.round(list.stream().mapToLong(t -> t.getActualSeconds()==null?0:t.getActualSeconds()).sum()/60.0); long c=list.stream().filter(t -> Boolean.TRUE.equals(t.getCompleted())).count(); r.getTrend().add(new ProductivityReport.SeriesPoint(k,p,a,list.isEmpty()?0:c*100.0/list.size())); });
    }
    private String key(LocalDate d, String g) { if ("year".equals(g)) return d.getYear()+""; if ("month".equals(g)) return d.getYear()+"-"+String.format("%02d", d.getMonthValue()); if ("week".equals(g)) return d.getYear()+"-W"+d.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()); return d.toString(); }

    private void buildBreakdowns(ProductivityReport r, List<ProductivityTask> tasks) {
        Map<String,Long> cats=new TreeMap<>(), projects=new TreeMap<>(), weekdays=new TreeMap<>(), priorities=new TreeMap<>(), heat=new HashMap<>();
        for (ProductivityTask t:tasks) { long m=Math.round((t.getActualSeconds()==null?0:t.getActualSeconds())/60.0); cats.merge(t.getCategory()==null?"(なし)":t.getCategory(),m,Long::sum); projects.merge(t.getProjectName()==null?"(なし)":t.getProjectName(),m,Long::sum); if(t.getCompletedAt()!=null) weekdays.merge(t.getCompletedAt().getDayOfWeek().toString(),m,Long::sum); if(t.getPlannedStartTime()!=null) { LocalDate activityDate=t.getCompletedAt()!=null?t.getCompletedAt().toLocalDate():(t.getScheduledDate()!=null?t.getScheduledDate():t.getDueDate()); if(activityDate!=null) heat.merge(activityDate.getDayOfWeek().getValue()+"-"+t.getPlannedStartTime().getHour(),1L,Long::sum); } }
        cats.forEach((k,v)->r.getCategoryMinutes().add(new ProductivityReport.NamedValue(k,v))); projects.forEach((k,v)->r.getProjectMinutes().add(new ProductivityReport.NamedValue(k,v))); weekdays.forEach((k,v)->r.getWeekdayMinutes().add(new ProductivityReport.NamedValue(k,v))); heat.forEach((k,v)->{String[] x=k.split("-"); r.getHeatmap().add(new ProductivityReport.HeatPoint(k,Integer.parseInt(x[0]),Integer.parseInt(x[1]),v));});
        for(int p=1;p<=3;p++){ final int x=p; List<ProductivityTask> q=tasks.stream().filter(t->t.getPriority()!=null&&t.getPriority()==x).toList(); long c=q.stream().filter(t->Boolean.TRUE.equals(t.getCompleted())).count(); r.getPriorityRates().add(new ProductivityReport.NamedValue("P"+p,q.isEmpty()?0:Math.round(c*100.0/q.size()))); }
    }
}
