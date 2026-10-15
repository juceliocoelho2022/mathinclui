package br.com.mathinclui.auth.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(AuthProblemException.class)
    ProblemDetail handleAuthProblem(AuthProblemException exception) {
        var detail = ProblemDetail.forStatusAndDetail(exception.status(), exception.getMessage());
        detail.setTitle("Falha de autenticação");
        return detail;
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        var detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle("Dados inválidos");
        detail.setDetail("Revise os campos informados e tente novamente.");
        return detail;
    }
}
