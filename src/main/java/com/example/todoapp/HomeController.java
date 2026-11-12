package com.example.todoapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final TodoMapper todoMapper;

    public HomeController(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "やること管理");
        return "index";
    }

    @GetMapping("/todos")
    public String todos(Model model) {
        model.addAttribute("todos", todoMapper.findAll());
        return "todos";
    }

    @GetMapping("/todos/new")
    public String create(@ModelAttribute("todo") Todo todo) {
        return "create";
    }

    @PostMapping("/todos/confirm")
    public String confirm(@ModelAttribute("todo") Todo todo) {
        return "create-confirm";
    }

    @PostMapping("/todos/new")
    public String rewrite(@ModelAttribute("todo") Todo todo) {
        return "create";
    }

    @PostMapping("/todos")
    public String insert(@ModelAttribute("todo") Todo todo, RedirectAttributes redirectAttributes) {
        todoMapper.insert(todo);
        redirectAttributes.addFlashAttribute("message", "登録しました");
        return "redirect:/todos";
    }
}
