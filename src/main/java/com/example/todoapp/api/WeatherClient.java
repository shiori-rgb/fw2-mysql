package com.example.todoapp.api;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class WeatherClient {

    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast";
    private static final double TOKYO_LATITUDE = 35.6895;
    private static final double TOKYO_LONGITUDE = 139.6917;

    public WeatherResult fetch(LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            return new WeatherResult(Map.of(), true);
        }

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);

        RestClient restClient = RestClient.create(new RestTemplate(requestFactory));
        Map<String, Object> response;
        try {
            response = restClient.get()
                    .uri(WEATHER_API_URL
                            + "?latitude=" + TOKYO_LATITUDE
                            + "&longitude=" + TOKYO_LONGITUDE
                            + "&daily=weather_code"
                            + "&timezone=Asia/Tokyo"
                            + "&start_date=" + from
                            + "&end_date=" + to)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
        } catch (RestClientException exception) {
            return new WeatherResult(Map.of(), true);
        }

        if (response == null || !(response.get("daily") instanceof Map<?, ?> daily)
                || !(daily.get("time") instanceof List<?> dates)
                || !(daily.get("weather_code") instanceof List<?> codes)) {
            return new WeatherResult(Map.of(), true);
        }

        Map<String, String> weather = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(dates.size(), codes.size()); i++) {
            if (dates.get(i) instanceof String dateText && codes.get(i) instanceof Number code) {
                weather.put(dateText, classify(code.intValue()));
            }
        }
        return new WeatherResult(weather, false);
    }

    private String classify(int code) {
        if (code == 0) {
            return "sunny";
        }
        if (code >= 1 && code <= 3 || code == 45 || code == 48) {
            return "cloudy";
        }
        return "rainy";
    }

    public record WeatherResult(Map<String, String> weather, boolean unavailable) {
    }
}
