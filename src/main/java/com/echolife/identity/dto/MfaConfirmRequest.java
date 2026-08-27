package com.echolife.identity.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
public record MfaConfirmRequest(@NotBlank @Pattern(regexp = "\\d{6}") String code) {}
