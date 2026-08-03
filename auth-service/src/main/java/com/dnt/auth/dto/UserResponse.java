package com.dnt.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponse {
    private String publicId;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;
    private Boolean enabled;
    private LocalDateTime createdAt;
}
