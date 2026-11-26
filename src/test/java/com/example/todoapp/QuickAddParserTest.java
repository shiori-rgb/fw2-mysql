package com.example.todoapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuickAddParserTest {

    private QuickAddParser parser;

    @BeforeEach
    void setUp() {
        parser = new QuickAddParser(Clock.fixed(Instant.parse("2026-11-26T03:00:00Z"), ZoneId.of("Asia/Tokyo")));
    }

    @Test
    void parsesTomorrowWithStartAndDuration() {
        QuickAddParseResult result = parser.parse("明日18時からBlenderを2時間");
        assertEquals("Blender", result.title());
        assertEquals(LocalDate.of(2026, 11, 27), result.scheduledDate());
        assertEquals(120, result.plannedMinutes());
        assertEquals("18:00", result.plannedStartTime().toString());
        assertEquals("20:00", result.plannedEndTime().toString());
    }

    @Test
    void parsesTodayAndMinutes() {
        QuickAddParseResult result = parser.parse("今日の20時から英語を30分");
        assertEquals("英語", result.title());
        assertEquals(LocalDate.of(2026, 11, 26), result.scheduledDate());
        assertEquals(30, result.plannedMinutes());
    }

    @Test
    void parsesNextMonday() {
        QuickAddParseResult result = parser.parse("来週月曜日にJavaの勉強");
        assertEquals(LocalDate.of(2026, 11, 30), result.scheduledDate());
        assertEquals("Javaの勉強", result.title());
    }

    @Test
    void parsesDueDateWithoutMakingItScheduledDate() {
        QuickAddParseResult result = parser.parse("12月10日までに課題を提出");
        assertNull(result.scheduledDate());
        assertEquals(LocalDate.of(2026, 12, 10), result.dueDate());
        assertEquals("課題を提出", result.title());
    }

    @Test
    void parsesPriorityAndProject() {
        QuickAddParseResult result = parser.parse("明日の15時から会議 優先度高 #仕事");
        assertEquals(1, result.priority());
        assertEquals("仕事", result.projectName());
        assertFalse(result.recognized().isEmpty());
    }

    @Test
    void doesNotGuessAnUnqualifiedWeekday() {
        QuickAddParseResult result = parser.parse("月曜日に作業");
        assertNull(result.scheduledDate());
        assertFalse(result.warnings().isEmpty());
    }
}
