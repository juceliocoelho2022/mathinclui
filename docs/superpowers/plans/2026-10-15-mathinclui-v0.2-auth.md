# MathInclui v0.2 Authentication Implementation Plan

**Goal:** Entregar autenticação ponta a ponta para professor e aluno, com JWT access/refresh, recuperação de senha do professor, PIN com BCrypt, autorização por perfil e telas React de acesso.

**Architecture:** O módulo `auth` concentra emissão/validação de tokens, rate limiting e endpoints públicos. Entidades mínimas de `professor` e `aluno` mapeiam as tabelas existentes; refresh sessions e password reset tokens ganham migração própria.

**Tech Stack:** Java 21, Spring Boot 3.5.6, Spring Security, OAuth2 Resource Server/Jose, JPA, PostgreSQL 17, Flyway, React 19, TypeScript, Vite.

**Spec:** `docs/superpowers/specs/2026-10-15-mathinclui-v0.1-design.md`

## Global Constraints
- Professor autentica com e-mail + senha e possui recuperação de senha.
- Aluno autentica com username + PIN numérico; PIN nunca é armazenado em claro.
- Perfis: `ROLE_PROFESSOR` e `ROLE_ALUNO`.
- BCrypt para senha e PIN.
- Segredos somente por variáveis de ambiente.
