package com.example.todoapp;

import org.springframework.stereotype.Service;

@Service
public class AppSettingsService {
    private final AppSettingsMapper mapper;

    public AppSettingsService(AppSettingsMapper mapper) { this.mapper = mapper; }

    public AppSettings get() {
        AppSettings settings = mapper.find();
        if (settings == null) {
            settings = new AppSettings();
            mapper.insert(settings);
        }
        return settings;
    }

    public void save(AppSettings settings) {
        normalize(settings);
        if (mapper.find() == null) mapper.insert(settings); else mapper.update(settings);
    }

    private void normalize(AppSettings s) {
        if (s.getDefaultWorkMinutes() == null || s.getDefaultWorkMinutes() < 0 || s.getDefaultWorkMinutes() > 1440) s.setDefaultWorkMinutes(30);
        if (s.getDefaultPriority() == null || s.getDefaultPriority() < 1 || s.getDefaultPriority() > 3) s.setDefaultPriority(2);
        if (!"sunday".equals(s.getWeekStart())) s.setWeekStart("monday");
        if (s.getInitialScreen() == null || !s.getInitialScreen().matches("/|/todos|/calendar|/burndown|/productivity")) s.setInitialScreen("/");
        if (s.getCalendarStart() == null || s.getCalendarStart().isBlank()) s.setCalendarStart("08:00");
        if (s.getCalendarEnd() == null || s.getCalendarEnd().isBlank()) s.setCalendarEnd("20:00");
        if (s.getTaskStartNoticeMinutes() == null || s.getTaskStartNoticeMinutes() < 0) s.setTaskStartNoticeMinutes(10);
        if (s.getDueNoticeMinutes() == null || s.getDueNoticeMinutes() < 0) s.setDueNoticeMinutes(60);
        if (s.getQuietStart() == null || s.getQuietStart().isBlank()) s.setQuietStart("22:00");
        if (s.getQuietEnd() == null || s.getQuietEnd().isBlank()) s.setQuietEnd("07:00");
        if (s.getHolidaysEnabled() == null) s.setHolidaysEnabled(true);
        if (s.getNotificationsEnabled() == null) s.setNotificationsEnabled(false);
        if (s.getNotificationSoundEnabled() == null) s.setNotificationSoundEnabled(true);
    }
}
