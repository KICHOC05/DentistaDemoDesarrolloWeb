package com.dnt.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponse {
    private String publicId;
    private String fullName;
    private String email;
    private String phone;
    private String userPublicId;
    private LocalDateTime createdAt;
}
