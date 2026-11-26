package com.example.todoapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SettingsController {
    private final AppSettingsService service;

    public SettingsController(AppSettingsService service) { this.service = service; }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("settings", service.get());
        return "settings";
    }

    @PostMapping("/settings/general")
    public String saveGeneral(@ModelAttribute("settings") AppSettings settings, RedirectAttributes redirect) {
        service.save(settings);
        redirect.addFlashAttribute("settingsSaved", true);
        return "redirect:/settings";
    }
}
