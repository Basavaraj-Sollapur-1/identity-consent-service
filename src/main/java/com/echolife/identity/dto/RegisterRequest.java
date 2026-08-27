package com.echolife.identity.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record RegisterRequest(
    @NotBlank @Size(max=100) String name,
    @Email @NotBlank @Size(max=255) String email,
    @NotBlank @Size(min=8,max=128) String password,
    @NotNull LocalDate dateOfBirth,
    @NotBlank @Size(max=20) String preferredLanguage) {}
