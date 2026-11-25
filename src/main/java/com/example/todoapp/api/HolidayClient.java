package com.example.todoapp.api;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class HolidayClient {

    private static final String HOLIDAY_API_URL = "https://holidays-jp.github.invalid/api/v1/date.json";

    public HolidayResult fetch(LocalDate from, LocalDate to) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);

        RestClient restClient = RestClient.create(requestFactory);
        Map<String, String> holidays;
        try {
            holidays = restClient.get()
                    .uri(HOLIDAY_API_URL)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, String>>() {
                    });
        } catch (RestClientException exception) {
            return new HolidayResult(Map.of(), true);
        }

        if (from == null && to == null) {
            return new HolidayResult(holidays, false);
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
        return new HolidayResult(filtered, false);
    }

    public record HolidayResult(Map<String, String> holidays, boolean unavailable) {
    }
}
