package com.example.todoapp.api;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolidayApiController {

    private final HolidayClient holidayClient;

    public HolidayApiController() {
        this.holidayClient = new HolidayClient();
    }

    @GetMapping("/api/holidays")
    public Map<String, String> holidays(
            @RequestParam(name = "from", required = false) LocalDate from,
            @RequestParam(name = "to", required = false) LocalDate to) {
        return holidayClient.fetch(from, to);
    }
}
