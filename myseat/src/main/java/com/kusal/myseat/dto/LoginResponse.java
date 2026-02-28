package com.kusal.myseat.dto;

import com.kusal.myseat.entity.Role;

public record LoginResponse(
        Long userId,
        String name,
        String email,
        Role role
) {
}
