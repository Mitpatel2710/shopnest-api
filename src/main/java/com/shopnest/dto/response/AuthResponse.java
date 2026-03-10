package com.shopnest.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;         // milliseconds
    private Long userId;
    private String email;
    private String firstName;
    private String role;
}