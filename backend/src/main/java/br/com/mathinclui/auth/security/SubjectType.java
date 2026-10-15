package br.com.mathinclui.auth.security;
public enum SubjectType { PROFESSOR("ROLE_PROFESSOR"), ALUNO("ROLE_ALUNO"); private final String role; SubjectType(String role){this.role=role;} public String role(){return role;} }
