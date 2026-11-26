package com.example.todoapp.api;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.todoapp.Todo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TodoRequest {

    @NotBlank(message = "{todo.title.required}")
    @Size(max = 255, message = "{todo.title.size}")
    private String title;

    @Size(max = 255, message = "{todo.detail.size}")
    private String detail;

    @NotBlank(message = "{todo.category.required}")
    private String category;

    @NotNull(message = "{todo.priority.required}")
    private Integer priority;

    private LocalDate dueDate;

    private LocalDate scheduledDate;
    private LocalTime plannedStartTime;
    private LocalTime plannedEndTime;
    private Integer plannedMinutes;
    private Integer actualSeconds;
    private String timerStatus;

    private Long projectId;

    private Long sectionId;

    private Boolean completed;

    public Todo toTodo() {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDetail(detail);
        todo.setCategory(category);
        todo.setPriority(priority);
        todo.setDueDate(dueDate);
        todo.setScheduledDate(scheduledDate);
        todo.setPlannedStartTime(plannedStartTime);
        todo.setPlannedEndTime(plannedEndTime);
        todo.setPlannedMinutes(plannedMinutes);
        todo.setActualSeconds(actualSeconds);
        todo.setTimerStatus(timerStatus);
        todo.setProjectId(projectId);
        todo.setSectionId(sectionId);
        todo.setCompleted(completed);
        return todo;
    }
}
