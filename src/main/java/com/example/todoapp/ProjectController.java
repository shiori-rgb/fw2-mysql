package com.example.todoapp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/projects")
    public String projects(Model model) {
        model.addAttribute("projects", projectService.findAll());
        return "projects";
    }

    @GetMapping("/projects/new")
    public String newProject(@ModelAttribute("project") Project project) {
        return "project-form";
    }

    @PostMapping("/projects")
    public String create(@Valid @ModelAttribute("project") Project project, BindingResult result) {
        if (result.hasErrors()) return "project-form";
        projectService.create(project);
        return "redirect:/projects/" + project.getId();
    }

    @GetMapping("/projects/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Project project = projectService.findById(id);
        if (project == null) {
            redirectAttributes.addFlashAttribute("message", "プロジェクトが見つかりません");
            return "redirect:/projects";
        }
        List<ProjectSection> sections = projectService.findSections(id);
        List<Todo> tasks = projectService.findTasks(id);
        Map<Long, List<Todo>> tasksBySection = new LinkedHashMap<>();
        sections.forEach(section -> tasksBySection.put(section.getId(), tasks.stream()
                .filter(task -> section.getId().equals(task.getSectionId())).toList()));
        model.addAttribute("project", project);
        model.addAttribute("sections", sections);
        model.addAttribute("tasksBySection", tasksBySection);
        model.addAttribute("unsectionedTasks", tasks.stream().filter(task -> task.getSectionId() == null).toList());
        return "project-detail";
    }

    @GetMapping("/projects/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Project project = projectService.findById(id);
        if (project == null) return missing(redirectAttributes);
        model.addAttribute("project", project);
        return "project-form";
    }

    @PostMapping("/projects/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("project") Project project,
            BindingResult result) {
        if (result.hasErrors()) return "project-form";
        project.setId(id);
        projectService.update(project);
        return "redirect:/projects/" + id;
    }

    @PostMapping("/projects/{id}/delete")
    public String delete(@PathVariable Long id) {
        projectService.delete(id);
        return "redirect:/projects";
    }

    @PostMapping("/projects/{id}/archive")
    public String archive(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean archived) {
        projectService.archive(id, archived);
        return "redirect:/projects/" + id;
    }

    @PostMapping("/projects/{id}/favorite")
    public String favorite(@PathVariable Long id) {
        projectService.toggleFavorite(id);
        return "redirect:/projects/" + id;
    }

    @PostMapping("/projects/{id}/sections")
    public String addSection(@PathVariable Long id, @RequestParam String name) {
        if (name != null && !name.isBlank()) projectService.addSection(id, name.trim());
        return "redirect:/projects/" + id;
    }

    private String missing(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "プロジェクトが見つかりません");
        return "redirect:/projects";
    }
}
