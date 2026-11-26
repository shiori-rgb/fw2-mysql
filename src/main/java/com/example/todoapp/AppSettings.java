package com.example.todoapp;

import lombok.Data;

@Data
public class AppSettings {
    private Long id;
    private String initialScreen = "/";
    private Integer defaultWorkMinutes = 30;
    private Integer defaultPriority = 2;
    private String weekStart = "monday";
    private String calendarStart = "08:00";
    private String calendarEnd = "20:00";
    private Boolean holidaysEnabled = true;
    private Boolean notificationsEnabled = false;
    private Integer taskStartNoticeMinutes = 10;
    private Integer dueNoticeMinutes = 60;
    private String quietStart = "22:00";
    private String quietEnd = "07:00";
    private Boolean notificationSoundEnabled = true;
}
