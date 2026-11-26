package com.example.todoapp;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BurndownController {

    private final SprintService sprintService;

    public BurndownController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @GetMapping("/burndown")
    public String burndown(@RequestParam(name = "sprintId", required = false) Long sprintId, Model model) {
        List<Sprint> sprints = sprintService.findAll();
        Sprint selectedSprint = sprintId == null && !sprints.isEmpty()
                ? sprints.get(0)
                : (sprintId == null ? null : sprintService.findById(sprintId));
        if (selectedSprint == null && !sprints.isEmpty()) {
            selectedSprint = sprints.get(0);
        }

        model.addAttribute("sprints", sprints);
        model.addAttribute("selectedSprint", selectedSprint);
        if (selectedSprint != null) {
            List<BurndownPoint> points = sprintService.createBurndown(selectedSprint);
            model.addAttribute("points", points);
            model.addAttribute("status", sprintService.status(points));
            model.addAttribute("labels", points.stream().map(point -> point.getDate().toString()).toList());
            model.addAttribute("actualValues", points.stream().map(BurndownPoint::getRemaining).toList());
            model.addAttribute("idealValues", points.stream().map(BurndownPoint::getIdeal).toList());
        } else {
            model.addAttribute("points", List.of());
            model.addAttribute("status", "on-track");
            model.addAttribute("labels", List.of());
            model.addAttribute("actualValues", List.of());
            model.addAttribute("idealValues", List.of());
        }
        return "burndown";
    }
}
