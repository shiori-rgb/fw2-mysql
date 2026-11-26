package com.example.todoapp;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductivityController {
    private final ProductivityService service;
    private final ProjectService projectService;
    private final ProductivityMapper mapper;

    public ProductivityController(ProductivityService service, ProjectService projectService, ProductivityMapper mapper) {
        this.service = service; this.projectService = projectService; this.mapper = mapper;
    }

    @GetMapping("/productivity")
    public String page(@RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "day") String granularity, @RequestParam(required = false) String category,
            @RequestParam(required = false) Long projectId, Model model) {
        model.addAttribute("report", service.report(from, to, category, projectId, granularity));
        model.addAttribute("categories", mapper.findCategories());
        model.addAttribute("projects", projectService.findActive());
        model.addAttribute("category", category == null ? "" : category);
        model.addAttribute("projectId", projectId); model.addAttribute("granularity", granularity);
        return "productivity";
    }

    @GetMapping("/productivity.csv")
    public ResponseEntity<byte[]> csv(@RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "day") String granularity, @RequestParam(required = false) String category,
            @RequestParam(required = false) Long projectId) {
        ProductivityReport r = service.report(from, to, category, projectId, granularity);
        StringBuilder csv = new StringBuilder("\uFEFF項目,値\n");
        csv.append("期間,").append(r.getFrom()).append("～").append(r.getTo()).append("\n");
        add(csv,"総タスク数",r.getTotalCount()); add(csv,"完了タスク数",r.getCompletedCount()); add(csv,"未完了タスク数",r.getIncompleteCount()); add(csv,"期限切れタスク数",r.getOverdueCount()); add(csv,"実績作業時間(分)",r.getActualMinutes());
        csv.append("\n推移,予定時間(分),実績時間(分),達成率(%)\n");
        for (ProductivityReport.SeriesPoint p : r.getTrend()) csv.append(p.label()).append(',').append(p.planned()).append(',').append(p.actual()).append(',').append(p.completionRate()).append('\n');
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=productivity.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8)).body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }
    private void add(StringBuilder b, String k, Object v) { b.append(k).append(',').append(v == null ? "データなし" : v).append('\n'); }
}
