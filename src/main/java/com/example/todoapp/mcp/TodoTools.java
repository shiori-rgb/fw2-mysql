package com.example.todoapp.mcp;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import com.example.todoapp.Todo;
import com.example.todoapp.TodoService;
import com.example.todoapp.api.HolidayClient;
import com.example.todoapp.api.TodoDto;

@Component
public class TodoTools {

    private final TodoService todoService;
    private final HolidayClient holidayClient;

    public TodoTools(TodoService todoService, HolidayClient holidayClient) {
        this.todoService = todoService;
        this.holidayClient = holidayClient;
    }

    @McpTool(name = "list_todos", description = "やることの一覧を返す（期間・ジャンルで絞れる）")
    public List<TodoDto> listTodos(
            @McpToolParam(description = "やることの名前で絞り込むキーワード", required = false) String keyword,
            @McpToolParam(description = "ジャンルで絞り込む", required = false) String category,
            @McpToolParam(description = "開始日（yyyy-MM-dd）", required = false) String from,
            @McpToolParam(description = "終了日（yyyy-MM-dd）", required = false) String to) {
        LocalDate fromDate = from == null ? null : LocalDate.parse(from);
        LocalDate toDate = to == null ? null : LocalDate.parse(to);
        return todoService.search(keyword, category, "asc", fromDate, toDate).stream()
                .map(TodoDto::from)
                .toList();
    }

    @McpTool(name = "get_todo", description = "やることを1件返す")
    public TodoDto getTodo(
            @McpToolParam(description = "やることのID", required = true) Long id) {
        Todo todo = todoService.findById(id);
        return todo == null ? null : TodoDto.from(todo);
    }

    @McpTool(name = "add_todo", description = "やることを1件足す")
    public TodoDto addTodo(
            @McpToolParam(description = "やること", required = true) String title,
            @McpToolParam(description = "メモ", required = false) String detail,
            @McpToolParam(description = "ジャンル", required = true) String category,
            @McpToolParam(description = "優先度", required = true) Integer priority,
            @McpToolParam(description = "期限（yyyy-MM-dd）", required = false) String dueDate) {
        validateCategory(category);

        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDetail(detail);
        todo.setCategory(category);
        todo.setPriority(priority);
        todo.setDueDate(parseDate(dueDate));
        todo.setCompleted(false);
        todoService.create(todo);
        return TodoDto.from(todoService.findById(todo.getId()));
    }

    @McpTool(name = "update_todo", description = "やることを1件直す（期限を変えるのもこれ）")
    public TodoDto updateTodo(
            @McpToolParam(description = "やることのID", required = true) Long id,
            @McpToolParam(description = "やること", required = false) String title,
            @McpToolParam(description = "メモ", required = false) String detail,
            @McpToolParam(description = "ジャンル", required = false) String category,
            @McpToolParam(description = "優先度", required = false) Integer priority,
            @McpToolParam(description = "期限（yyyy-MM-dd）", required = false) String dueDate) {
        Todo todo = requireTodo(id);
        if (title != null) {
            todo.setTitle(title);
        }
        if (detail != null) {
            todo.setDetail(detail);
        }
        if (category != null) {
            validateCategory(category);
            todo.setCategory(category);
        }
        if (priority != null) {
            todo.setPriority(priority);
        }
        if (dueDate != null) {
            todo.setDueDate(LocalDate.parse(dueDate));
        }
        todoService.update(todo);
        return TodoDto.from(todoService.findById(id));
    }

    @McpTool(name = "complete_todo", description = "やることを完了にする")
    public TodoDto completeTodo(
            @McpToolParam(description = "やることのID", required = true) Long id) {
        Todo todo = requireTodo(id);
        todo.setCompleted(true);
        todoService.update(todo);
        return TodoDto.from(todoService.findById(id));
    }

    @McpTool(name = "delete_todo", description = "やることを1件消す")
    public boolean deleteTodo(
            @McpToolParam(description = "やることのID", required = true) Long id) {
        requireTodo(id);
        todoService.delete(id);
        return true;
    }

    @McpTool(name = "find_free_days", description = "期間の中で、期限のやることが無く、土日でも祝日でもない「空いている日」を返す。やることの期限を動かす先を決めるのに使う")
    public List<String> findFreeDays(
            @McpToolParam(description = "開始日（yyyy-MM-dd）", required = true) String from,
            @McpToolParam(description = "終了日（yyyy-MM-dd）", required = true) String to) {
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);
        Set<LocalDate> todoDates = todoService.search(null, null, "asc", fromDate, toDate).stream()
                .map(Todo::getDueDate)
                .filter(date -> date != null)
                .collect(Collectors.toSet());
        Set<LocalDate> holidayDates = holidayClient.fetch(null, null).holidays().keySet().stream()
                .map(LocalDate::parse)
                .collect(Collectors.toSet());

        List<String> freeDays = new java.util.ArrayList<>();
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (todoDates.contains(date)
                    || dayOfWeek == DayOfWeek.SATURDAY
                    || dayOfWeek == DayOfWeek.SUNDAY
                    || holidayDates.contains(date)) {
                continue;
            }
            freeDays.add(date.toString());
        }
        return freeDays;
    }

    private LocalDate parseDate(String date) {
        return date == null ? null : LocalDate.parse(date);
    }

    private Todo requireTodo(Long id) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            throw new IllegalArgumentException("Todo not found: " + id);
        }
        return todo;
    }

    private void validateCategory(String category) {
        if (!Set.of("デザイン", "マーケティング", "プログラミング", "資格", "就職活動")
                .contains(category)) {
            throw new IllegalArgumentException("カテゴリーが正しくありません");
        }
    }
}
