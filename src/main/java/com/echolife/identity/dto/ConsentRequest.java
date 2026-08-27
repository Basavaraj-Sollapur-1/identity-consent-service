package com.echolife.identity.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record ConsentRequest(@NotBlank @Size(max=120) String personaId, boolean interactiveAllowed, boolean voiceAllowed, boolean avatarAllowed, boolean textAllowed) {}
