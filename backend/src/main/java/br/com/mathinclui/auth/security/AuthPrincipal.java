package br.com.mathinclui.auth.security;

public record AuthPrincipal(Long id, String displayName, String login, SubjectType subjectType) { public String role() { return subjectType.role(); } }
