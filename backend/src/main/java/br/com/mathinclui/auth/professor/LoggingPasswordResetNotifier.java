package br.com.mathinclui.auth.professor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoggingPasswordResetNotifier implements PasswordResetNotifier {
    private static final Logger log = LoggerFactory.getLogger(LoggingPasswordResetNotifier.class);
    private final boolean logToken;
    public LoggingPasswordResetNotifier(@Value("${app.security.password-reset.log-token:false}") boolean logToken) { this.logToken = logToken; }
    @Override public void notifyReset(String email, String rawToken) { if (logToken) log.warn("DEV ONLY - password reset token for {}: {}", email, rawToken); }
}
