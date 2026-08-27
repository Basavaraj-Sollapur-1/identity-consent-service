package com.echolife.identity.dto; import jakarta.validation.constraints.*; public record LoginRequest(@Email @NotBlank String email,@NotBlank String password){}
