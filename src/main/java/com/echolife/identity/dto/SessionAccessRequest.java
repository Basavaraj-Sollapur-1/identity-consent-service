package com.echolife.identity.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record SessionAccessRequest(
    @NotBlank @Size(max=36) String userId,
    @NotBlank @Size(max=120) String personaId,
    @NotBlank @Size(max=40) String mode,
    @NotBlank @Size(max=40) String inputChannel,
    @NotBlank @Size(max=40) String outputChannel) {}
