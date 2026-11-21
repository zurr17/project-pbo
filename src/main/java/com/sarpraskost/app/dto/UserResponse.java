package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarpraskost.app.entity.Role;
import com.sarpraskost.app.entity.User;

public record UserResponse(
    Long id,
    String nama,
    String email,
    Role role,
    @JsonProperty("nomor_kamar") String nomorKamar
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getNama(),
            user.getEmail(),
            user.getRole(),
            user.getNomorKamar()
        );
    }
}
