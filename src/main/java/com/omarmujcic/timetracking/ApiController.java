package com.omarmujcic.timetracking;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final JdbcTemplate jdbcTemplate;

    public ApiController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/status")
    public Map<String, String> status() {
        String database = jdbcTemplate.queryForObject("select current_database()", String.class);

        return Map.of(
            "backend", "UP",
            "database", database == null ? "UNKNOWN" : database
        );
    }
}
