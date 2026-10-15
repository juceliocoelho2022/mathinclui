package br.com.mathinclui.auth.professor;

public interface PasswordResetNotifier { void notifyReset(String email, String rawToken); }
