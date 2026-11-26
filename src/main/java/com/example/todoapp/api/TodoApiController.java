package com.example.todoapp.api;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.example.todoapp.Todo;
import com.example.todoapp.TodoService;

@RestController
public class TodoApiController {

    private final TodoService todoService;

    public TodoApiController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/api/todos")
    public List<TodoDto> list(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "category", defaultValue = "") String category,
            @RequestParam(name = "order", defaultValue = "asc") String order,
            @RequestParam(name = "from", required = false) LocalDate from,
            @RequestParam(name = "to", required = false) LocalDate to) {
        return todoService.search(keyword, category, order, from, to).stream()
                .map(TodoDto::from)
                .toList();
    }

    @GetMapping("/api/todos.csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "category", defaultValue = "") String category,
            @RequestParam(name = "order", defaultValue = "asc") String order,
            @RequestParam(name = "showCompleted", defaultValue = "false") boolean showCompleted,
            @RequestParam(name = "trash", defaultValue = "0") int trash) {
        boolean showTrash = trash == 1;
        List<Todo> todos = todoService.search(keyword, category, order, showCompleted, showTrash, null, null);
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append(csvRow("やること", "ジャンル", "優先度", "期限", "状態", "操作"));
        for (Todo todo : todos) {
            String priority = todo.getPriority() == 1 ? "高" : (todo.getPriority() == 2 ? "中" : "低");
            if (todo.getPriority() == 1) {
                priority += " ★";
            }
            String title = Boolean.TRUE.equals(todo.getPinned()) ? "★ " + todo.getTitle() : todo.getTitle();
            String status = Boolean.TRUE.equals(todo.getCompleted()) ? "完了" : "未完了";
            if (todo.getCompletedAt() != null && Boolean.TRUE.equals(todo.getCompleted())) {
                status += " (" + todo.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + ")";
            }
            csv.append(csvRow(title, todo.getCategory(), priority,
                    todo.getDueDate() == null ? "" : todo.getDueDate().toString(), status, ""));
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDispositionFormData("attachment", "todos.csv");
        return new ResponseEntity<>(csv.toString().getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }

    private String csvRow(String... values) {
        return String.join(",", java.util.Arrays.stream(values)
                .map(this::csvCell)
                .toList()) + "\r\n";
    }

    private String csvCell(String value) {
        String safeValue = value == null ? "" : value;
        if (safeValue.startsWith("=") || safeValue.startsWith("+")
                || safeValue.startsWith("-") || safeValue.startsWith("@")) {
            safeValue = "'" + safeValue;
        }
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }

    @GetMapping("/api/todos/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            return notFound(id);
        }
        return ResponseEntity.ok(TodoDto.from(todo));
    }

    @PostMapping("/api/todos")
    public ResponseEntity<?> create(@Valid @RequestBody TodoRequest request) {
        Todo todo = request.toTodo();
        todoService.create(todo);
        Todo created = todoService.findById(todo.getId());
        URI location = URI.create("/api/todos/" + todo.getId());
        return ResponseEntity.created(location).body(TodoDto.from(created));
    }

    @PutMapping("/api/todos/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody TodoRequest request) {
        Todo existing = todoService.findById(id);
        if (existing == null) {
            return notFound(id);
        }
        Todo todo = request.toTodo();
        todo.setId(id);
        todoService.update(todo);
        return ResponseEntity.ok(TodoDto.from(todoService.findById(id)));
    }

    @DeleteMapping("/api/todos/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Todo existing = todoService.findById(id);
        if (existing == null) {
            return notFound(id);
        }
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<ProblemDetail> notFound(Long id) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, "Todo not found: " + id);
        problem.setInstance(URI.create("/api/todos/" + id));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationError(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "入力に誤りがあります");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errors", exception.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of("field", error.getField(), "message", error.getDefaultMessage()))
                .collect(Collectors.toList()));
        return ResponseEntity.badRequest().body(problem);
    }
}
