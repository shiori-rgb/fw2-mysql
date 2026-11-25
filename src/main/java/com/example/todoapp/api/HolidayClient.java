package com.example.todoapp.api;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

public class HolidayClient {

    private static final String HOLIDAY_API_URL = "https://holidays-jp.github.io/api/v1/date.json";

    public Map<String, String> fetch(LocalDate from, LocalDate to) {
        RestClient restClient = RestClient.create();
        Map<String, String> holidays = restClient.get()
                .uri(HOLIDAY_API_URL)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, String>>() {
                });

        if (from == null && to == null) {
            return holidays;
        }

        Map<String, String> filtered = new LinkedHashMap<>();
        holidays.forEach((dateText, name) -> {
            LocalDate date = LocalDate.parse(dateText);
            boolean afterFrom = from == null || !date.isBefore(from);
            boolean beforeTo = to == null || !date.isAfter(to);
            if (afterFrom && beforeTo) {
                filtered.put(dateText, name);
            }
        });
        return filtered;
    }
}
