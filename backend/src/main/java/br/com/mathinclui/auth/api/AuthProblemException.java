package br.com.mathinclui.auth.api;

import org.springframework.http.HttpStatus;

public class AuthProblemException extends RuntimeException {
    private final HttpStatus status;
    public AuthProblemException(HttpStatus status, String message) { super(message); this.status = status; }
    public HttpStatus status() { return status; }
    public static AuthProblemException invalidCredentials() { return new AuthProblemException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas."); }
}
