package com.dnt.auth.controller;

import com.dnt.auth.dto.UserResponse;
import com.dnt.auth.model.Role;
import com.dnt.auth.model.User;
import com.dnt.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String publicId) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .publicId(user.getPublicId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles().stream().map(Role::name).collect(Collectors.toSet()))
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
