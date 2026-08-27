package com.echolife.identity.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuthSessionCleanupService {
    private final AuthSessionService sessions;
    public AuthSessionCleanupService(AuthSessionService sessions) { this.sessions = sessions; }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanup() { sessions.cleanupExpired(); }
}
