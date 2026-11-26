package com.example.todoapp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class QuickAddController {

    private final QuickAddParser parser;
    private final TodoService todoService;
    private final ProjectService projectService;

    public QuickAddController(QuickAddParser parser, TodoService todoService, ProjectService projectService) {
        this.parser = parser;
        this.todoService = todoService;
        this.projectService = projectService;
    }

    @GetMapping("/quick-add")
    public String page(Model model) {
        model.addAttribute("projects", projectService.findActive());
        return "quick-add";
    }

    @PostMapping("/api/quick-add/parse")
    @ResponseBody
    public ResponseEntity<?> parse(@RequestBody QuickAddRequest request) {
        QuickAddParseResult result = parser.parse(request == null ? "" : request.text());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("title", result.title());
        response.put("scheduledDate", result.scheduledDate());
        response.put("dueDate", result.dueDate());
        response.put("plannedStartTime", result.plannedStartTime());
        response.put("plannedEndTime", result.plannedEndTime());
        response.put("plannedMinutes", result.plannedMinutes());
        response.put("priority", result.priority());
        response.put("projectName", result.projectName());
        response.put("recognized", result.recognized());
        response.put("warnings", result.warnings());
        if (result.projectName() != null) {
            projectService.findActive().stream()
                    .filter(project -> project.getName().equalsIgnoreCase(result.projectName()))
                    .findFirst()
                    .ifPresent(project -> response.put("projectId", project.getId()));
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/quick-add")
    public String create(
            @RequestParam String title,
            @RequestParam(defaultValue = "プログラミング") String category,
            @RequestParam(defaultValue = "2") Integer priority,
            @RequestParam(required = false) String scheduledDate,
            @RequestParam(required = false) String dueDate,
            @RequestParam(required = false) String plannedStartTime,
            @RequestParam(required = false) String plannedEndTime,
            @RequestParam(required = false) Integer plannedMinutes,
            @RequestParam(required = false) Long projectId,
            RedirectAttributes redirectAttributes) {
        Todo todo = new Todo();
        todo.setTitle(title.trim());
        todo.setCategory(category);
        todo.setPriority(priority);
        todo.setScheduledDate(parseDate(scheduledDate));
        todo.setDueDate(parseDate(dueDate));
        todo.setPlannedStartTime(parseTime(plannedStartTime));
        todo.setPlannedEndTime(parseTime(plannedEndTime));
        todo.setPlannedMinutes(plannedMinutes);
        todo.setProjectId(projectId);
        todo.setCompleted(false);
        todoService.create(todo);
        redirectAttributes.addFlashAttribute("message", "タスクを登録しました");
        return "redirect:/todos";
    }

    private LocalDate parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private LocalTime parseTime(String value) {
        return value == null || value.isBlank() ? null : LocalTime.parse(value);
    }
}
