package com.example.todoapp;

import java.util.List;
import java.time.LocalDate;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
public class HomeController {

    private final TodoService todoService;
    private final TodoAttachmentService attachmentService;
    private final MessageSource messageSource;

    public HomeController(TodoService todoService, TodoAttachmentService attachmentService, MessageSource messageSource) {
        this.todoService = todoService;
        this.attachmentService = attachmentService;
        this.messageSource = messageSource;
    }

    @GetMapping("/")
    public String home(Model model) {
        return "index";
    }

    @GetMapping("/todos")
    public String todos(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "category", defaultValue = "") String category,
            @RequestParam(name = "order", defaultValue = "asc") String order,
            @RequestParam(name = "showCompleted", defaultValue = "false") boolean showCompleted,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "trash", defaultValue = "0") int trash,
            @RequestParam(name = "sidebar", defaultValue = "") String sidebar,
            Model model) {
        String selectedCategory = "すべて".equals(category) ? "" : category;
        String selectedOrder = "desc".equals(order) ? "desc" : "asc";
        int pageSize = 10;
        boolean showTrash = trash == 1;
        LocalDate from = null;
        LocalDate to = null;
        boolean completedOnly = false;
        LocalDate today = LocalDate.now();
        switch (sidebar) {
        case "today" -> from = to = today;
        case "tomorrow" -> from = to = today.plusDays(1);
        case "week" -> {
            from = today.minusDays(today.getDayOfWeek().getValue() % 7);
            to = from.plusDays(6);
        }
        case "upcoming" -> from = today.plusDays(1);
        case "overdue" -> to = today.minusDays(1);
        case "completed" -> {
            showCompleted = true;
            completedOnly = true;
        }
        case "all" -> showCompleted = true;
        default -> {
            // 受信トレイやプロジェクトは通常の未完了一覧を表示します。
        }
        }
        if (showTrash) {
            from = null;
            to = null;
            completedOnly = false;
        }
        int totalCount = todoService.count(keyword, selectedCategory, from, to, showCompleted, completedOnly,
                showTrash);
        int totalPages = Math.max(1, (totalCount + pageSize - 1) / pageSize);
        int selectedPage = Math.min(Math.max(page, 1), totalPages);
        int offset = (selectedPage - 1) * pageSize;
        List<Todo> todos = todoService.search(keyword, selectedCategory, selectedOrder, from, to, showCompleted,
                completedOnly, showTrash, offset, pageSize);
        model.addAttribute("todos", todos);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", selectedCategory);
        model.addAttribute("order", selectedOrder);
        model.addAttribute("showCompleted", showCompleted);
        model.addAttribute("page", selectedPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("trash", showTrash ? 1 : 0);
        model.addAttribute("sidebar", sidebar);
        return "todos";
    }

    @GetMapping("/todos/new")
    public String create(@ModelAttribute("todo") Todo todo) {
        return "create";
    }

    @PostMapping("/todos/confirm")
    public String confirm(@Valid @ModelAttribute("todo") Todo todo, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "create";
        }
        return "create-confirm";
    }

    @PostMapping("/todos/new")
    public String rewrite(@ModelAttribute("todo") Todo todo) {
        return "create";
    }

    @PostMapping("/todos")
    public String insert(@ModelAttribute("todo") Todo todo, RedirectAttributes redirectAttributes) {
        todoService.create(todo);
        redirectAttributes.addFlashAttribute("message", message("flash.registered"));
        return "redirect:/todos";
    }

    @GetMapping("/todos/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", message("flash.notFound"));
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        model.addAttribute("id", id);
        model.addAttribute("attachments", attachmentService.findByTodoId(id));
        return "edit";
    }

    @PostMapping("/todos/{id}/confirm")
    public String editConfirm(@PathVariable Long id, @Valid @ModelAttribute("todo") Todo todo,
            BindingResult bindingResult, Model model) {
        model.addAttribute("id", id);
        if (bindingResult.hasErrors()) {
            return "edit";
        }
        return "edit-confirm";
    }

    @PostMapping("/todos/{id}/edit")
    public String editRewrite(@PathVariable Long id, @ModelAttribute("todo") Todo todo, Model model) {
        model.addAttribute("id", id);
        return "edit";
    }

    @PostMapping("/todos/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("todo") Todo todo,
            RedirectAttributes redirectAttributes) {
        todo.setId(id);
        todoService.update(todo);
        redirectAttributes.addFlashAttribute("message", message("flash.saved"));
        return "redirect:/todos";
    }

    @GetMapping("/todos/{id}/delete")
    public String deleteConfirm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", message("flash.notFound"));
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        model.addAttribute("id", id);
        return "delete";
    }

    @PostMapping("/todos/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        todoService.delete(id);
        redirectAttributes.addFlashAttribute("message", message("flash.deleted"));
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/restore")
    public String restore(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        todoService.restore(id);
        redirectAttributes.addFlashAttribute("message", message("flash.restored"));
        return "redirect:/todos?trash=1";
    }

    @PostMapping("/todos/{id}/pin")
    public String togglePin(
            @PathVariable Long id,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "category", defaultValue = "") String category,
            @RequestParam(name = "order", defaultValue = "asc") String order,
            @RequestParam(name = "showCompleted", defaultValue = "false") boolean showCompleted,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "trash", defaultValue = "0") int trash,
            RedirectAttributes redirectAttributes) {
        todoService.togglePinned(id);
        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("category", category);
        redirectAttributes.addAttribute("order", order);
        redirectAttributes.addAttribute("showCompleted", showCompleted);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("trash", trash);
        return "redirect:/todos";
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
