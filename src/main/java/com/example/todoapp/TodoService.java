package com.example.todoapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TodoService {

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public List<Todo> search(String keyword, String category, String order) {
        return search(keyword, category, order, null, null);
    }

    public List<Todo> search(String keyword, String category, String order, boolean showCompleted) {
        return search(keyword, category, order, showCompleted, null, null);
    }

    public List<Todo> search(String keyword, String category, String order, boolean showCompleted,
            Integer offset, Integer limit) {
        return search(keyword, category, order, showCompleted, false, offset, limit);
    }

    public List<Todo> search(String keyword, String category, String order, boolean showCompleted,
            boolean trash, Integer offset, Integer limit) {
        return search(keyword, category, order, null, null, showCompleted, false, trash, offset, limit);
    }

    public int count(String keyword, String category, boolean showCompleted, boolean trash) {
        return count(keyword, category, null, null, showCompleted, false, trash);
    }

    public List<Todo> search(String keyword, String category, String order, LocalDate from, LocalDate to) {
        return search(keyword, category, order, from, to, true, false, false, null, null);
    }

    public List<Todo> search(String keyword, String category, String order, LocalDate from, LocalDate to,
            boolean showCompleted, boolean completedOnly, boolean trash, Integer offset, Integer limit) {
        return todoMapper.search(keyword, category, order, from, to, showCompleted, completedOnly, offset, limit,
                trash);
    }

    public int count(String keyword, String category, LocalDate from, LocalDate to, boolean showCompleted,
            boolean completedOnly, boolean trash) {
        return todoMapper.count(keyword, category, showCompleted, completedOnly, from, to, trash);
    }

    public Todo findById(Long id) {
        return todoMapper.findById(id);
    }

    public int create(Todo todo) {
        int result = todoMapper.insert(todo);
        log.info("Todo registration completed: id={}", todo.getId());
        return result;
    }

    public int update(Todo todo) {
        Todo existing = todoMapper.findById(todo.getId());
        boolean wasCompleted = existing != null && Boolean.TRUE.equals(existing.getCompleted());
        boolean isCompleted = Boolean.TRUE.equals(todo.getCompleted());
        if (!wasCompleted && isCompleted) {
            todo.setCompletedAt(LocalDateTime.now());
        } else if (wasCompleted && !isCompleted) {
            todo.setCompletedAt(null);
        } else if (existing != null) {
            todo.setCompletedAt(existing.getCompletedAt());
        }
        if (existing != null) {
            todo.setActualSeconds(existing.getActualSeconds());
            todo.setTimerStartedAt(existing.getTimerStartedAt());
            todo.setTimerStatus(existing.getTimerStatus());
            todo.setScheduleChangeCount(existing.getScheduleChangeCount());
        }
        int result = todoMapper.update(todo);
        log.info("Todo update completed: id={}", todo.getId());
        return result;
    }

    public int delete(Long id) {
        int result = todoMapper.deleteById(id);
        log.info("Todo deletion completed: id={}", id);
        return result;
    }

    public int restore(Long id) {
        int result = todoMapper.restoreById(id);
        log.info("Todo restoration completed: id={}", id);
        return result;
    }

    public int togglePinned(Long id) {
        int result = todoMapper.togglePinned(id);
        log.info("Todo pin toggled: id={}", id);
        return result;
    }

    public Todo startTimer(Long id) {
        Todo todo = requireTodo(id);
        todo.setTimerStatus("RUNNING");
        todo.setTimerStartedAt(LocalDateTime.now());
        todoMapper.updateTimer(todo);
        return findById(id);
    }

    public Todo pauseTimer(Long id) {
        Todo todo = requireTodo(id);
        accumulateRunningTime(todo);
        todo.setTimerStatus("PAUSED");
        todo.setTimerStartedAt(null);
        todoMapper.updateTimer(todo);
        return findById(id);
    }

    public Todo stopTimer(Long id) {
        Todo todo = requireTodo(id);
        accumulateRunningTime(todo);
        todo.setTimerStatus("STOPPED");
        todo.setTimerStartedAt(null);
        todoMapper.updateTimer(todo);
        return findById(id);
    }

    public Todo updateSchedule(Todo todo) {
        todoMapper.updateSchedule(todo);
        return findById(todo.getId());
    }

    private Todo requireTodo(Long id) {
        Todo todo = findById(id);
        if (todo == null) {
            throw new IllegalArgumentException("Todo not found: " + id);
        }
        return todo;
    }

    private void accumulateRunningTime(Todo todo) {
        int seconds = todo.getActualSeconds() == null ? 0 : todo.getActualSeconds();
        if ("RUNNING".equals(todo.getTimerStatus()) && todo.getTimerStartedAt() != null) {
            seconds += (int) Math.max(0, Duration.between(todo.getTimerStartedAt(), LocalDateTime.now()).toSeconds());
        }
        todo.setActualSeconds(seconds);
    }
}
