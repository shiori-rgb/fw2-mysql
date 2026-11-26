package com.example.todoapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AutomationController {
    private final AutomationRuleService service;
    private final ProjectService projectService;

    public AutomationController(AutomationRuleService service, ProjectService projectService) { this.service = service; this.projectService = projectService; }

    @GetMapping("/automation")
    public String list(Model model) { var rules=service.findAll(); Map<Long, java.util.List<AutomationRuleRun>> runs=new HashMap<>(); rules.forEach(r -> runs.put(r.getId(), service.findRuns(r.getId()))); model.addAttribute("rules", rules); model.addAttribute("runsByRule", runs); return "automation"; }

    @GetMapping("/automation/new")
    public String newRule(Model model) { addForm(model, new AutomationRule()); return "automation-form"; }

    @GetMapping("/automation/{id}/edit")
    public String edit(@PathVariable Long id, Model model) { addForm(model, service.findById(id)); return "automation-form"; }

    @PostMapping("/automation/save")
    public String save(@ModelAttribute AutomationRule rule, RedirectAttributes redirect) {
        if (rule.getName() == null || rule.getName().isBlank() || rule.getConditionType() == null || rule.getActionType() == null) {
            redirect.addFlashAttribute("automationError", "ルール名・条件・処理は必須です。"); return "redirect:/automation";
        }
        if (rule.getId() == null) service.save(rule); else service.update(rule);
        redirect.addFlashAttribute("automationSaved", true); return "redirect:/automation";
    }

    @PostMapping("/automation/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirect) { AutomationRule r=service.findById(id); if(r!=null) service.setEnabled(id,!Boolean.TRUE.equals(r.getEnabled())); return "redirect:/automation"; }
    @PostMapping("/automation/{id}/delete")
    public String delete(@PathVariable Long id) { service.delete(id); return "redirect:/automation"; }
    @PostMapping("/automation/{id}/test")
    public String test(@PathVariable Long id, RedirectAttributes redirect) { redirect.addFlashAttribute("testResults", service.test(id)); return "redirect:/automation"; }

    private void addForm(Model model, AutomationRule rule) { model.addAttribute("rule", rule); model.addAttribute("projects", projectService.findActive()); }
}
