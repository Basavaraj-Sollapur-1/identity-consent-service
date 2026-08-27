package com.echolife.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaDisableRequest(

        @NotBlank(message = "PASSWORD_REQUIRED")
        String password,

        @NotBlank(message = "MFA_CODE_REQUIRED")
        @Pattern(
                regexp = "\\d{6}",
                message = "INVALID_MFA_CODE_FORMAT"
        )
        String code
) {
}