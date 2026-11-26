package com.example.todoapp;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SidebarPageController {

    private final MessageSource messageSource;

    public SidebarPageController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @GetMapping({ "/productivity", "/settings" })
    public String placeholder(Model model, jakarta.servlet.http.HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String key = request.getRequestURI().equals("/settings")
                ? "sidebar.settings.title"
                : "sidebar.productivity.title";
        model.addAttribute("title", messageSource.getMessage(key, null, locale));
        model.addAttribute("description", messageSource.getMessage("sidebar.comingSoon", null, locale));
        return "sidebar-placeholder";
    }
}
