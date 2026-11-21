package com.sarpraskost.app.dto;

public record LoginRequest(
    String email,
    String password
) {
}
