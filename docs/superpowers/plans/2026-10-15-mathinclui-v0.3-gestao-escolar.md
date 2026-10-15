# MathInclui v0.3 Gestão Escolar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Entregar gestão de turmas, alunos e perfil pedagógico individual para professores autenticados, com isolamento por professor e UI React correspondente.

**Architecture:** Manter o monólito modular Spring Boot. Cada domínio (`turma`, `aluno`, `perfilpedagogico`) expõe controller REST, service e repository próprios. O professor autenticado é identificado pelo subject JWT `PROFESSOR:<id>`; todos os services validam ownership antes de ler ou alterar dados. O frontend usa a sessão JWT existente e uma página única de gestão escolar para o MVP.

**Tech Stack:** Java 21, Spring Boot 3.5.6, Spring Data JPA, Spring Security/JWT, BCrypt, PostgreSQL 17, React 19, TypeScript, Axios.

**Spec:** `docs/superpowers/specs/2026-10-15-mathinclui-v0.1-design.md`

## Global Constraints

- Anos escolares permitidos: 6, 7 e 8.
- Aluno autentica com username + PIN de 4 a 6 dígitos; PIN armazenado somente como BCrypt hash.
- Perfil pedagógico não armazena diagnóstico, laudo ou informação clínica.
- Professor só acessa turmas e alunos próprios.
- Exclusão de turma é soft delete (`ativa=false`).
- Cadastro de aluno cria perfil pedagógico padrão automaticamente.

## Review Focus

- Professor tentando acessar turma/aluno de outro professor deve receber 404/403 sem vazamento de dados.
- Username duplicado deve retornar conflito legível.
- PIN inválido (não numérico ou fora de 4–6 dígitos) deve ser rejeitado antes de persistir.
- Ano escolar fora de 6–8 deve ser rejeitado.
- Atualização de perfil deve aceitar somente enums e limites definidos no schema.

---

### Task 1: Gestão de turmas

**Files:**
- Create: `backend/src/main/java/br/com/mathinclui/turma/domain/Turma.java`
- Create: `backend/src/main/java/br/com/mathinclui/turma/repository/TurmaRepository.java`
- Create: `backend/src/main/java/br/com/mathinclui/turma/TurmaService.java`
- Create: `backend/src/main/java/br/com/mathinclui/turma/TurmaController.java`
- Create DTOs in `backend/src/main/java/br/com/mathinclui/turma/`
- Test contract: `scripts/verify_v03.py`

**Interfaces:**
- Produces REST endpoints `POST/GET /api/v1/turmas`, `GET/PUT/DELETE /api/v1/turmas/{id}`.
- Service methods consume authenticated professor id and enforce ownership.

- [ ] Write failing v0.3 contract checks for turma files/endpoints/ownership query.
- [ ] Run verifier and confirm failure.
- [ ] Implement entity, repository, service, controller and DTOs.
- [ ] Run verifier and confirm turma checks pass.
- [ ] Commit.

### Task 2: Gestão de alunos e PIN

**Files:**
- Modify: `backend/src/main/java/br/com/mathinclui/aluno/domain/Aluno.java`
- Modify: `backend/src/main/java/br/com/mathinclui/aluno/repository/AlunoRepository.java`
- Create: `backend/src/main/java/br/com/mathinclui/aluno/AlunoService.java`
- Create: `backend/src/main/java/br/com/mathinclui/aluno/AlunoController.java`
- Create DTOs in `backend/src/main/java/br/com/mathinclui/aluno/`
- Modify: `scripts/verify_v03.py`

**Interfaces:**
- Produces `POST/GET /api/v1/turmas/{turmaId}/alunos`, `GET/PUT /api/v1/alunos/{id}`, `PATCH /api/v1/alunos/{id}/pin`.
- Uses `PasswordEncoder` for PIN hash e `TurmaRepository` ownership checks.

- [ ] Add failing contract checks for aluno endpoints, BCrypt use and PIN validation.
- [ ] Run verifier and confirm failure.
- [ ] Implement student CRUD subset and PIN reset.
- [ ] Run verifier and confirm checks pass.
- [ ] Commit.

### Task 3: Perfil pedagógico individual

**Files:**
- Create: `backend/src/main/java/br/com/mathinclui/perfilpedagogico/domain/PerfilPedagogico.java`
- Create: `backend/src/main/java/br/com/mathinclui/perfilpedagogico/repository/PerfilPedagogicoRepository.java`
- Create: `backend/src/main/java/br/com/mathinclui/perfilpedagogico/PerfilPedagogicoService.java`
- Create: `backend/src/main/java/br/com/mathinclui/perfilpedagogico/PerfilPedagogicoController.java`
- Create DTOs/enums in `backend/src/main/java/br/com/mathinclui/perfilpedagogico/`
- Modify: `scripts/verify_v03.py`

**Interfaces:**
- Produces `GET/PUT /api/v1/alunos/{id}/perfil-pedagogico`.
- Student creation calls `PerfilPedagogicoService.criarPadrao(alunoId)`.

- [ ] Add failing contract checks for profile fields, endpoint and automatic default creation.
- [ ] Run verifier and confirm failure.
- [ ] Implement profile entity/repository/service/controller.
- [ ] Integrate default profile creation in `AlunoService`.
- [ ] Run verifier and confirm checks pass.
- [ ] Commit.

### Task 4: Frontend de gestão escolar

**Files:**
- Create: `frontend/src/services/gestaoEscolarApi.ts`
- Create: `frontend/src/features/gestao/GestaoEscolarPage.tsx`
- Modify: `frontend/src/features/auth/SessionPage.tsx`
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/styles.css`
- Modify: `scripts/verify_v03.py`

**Interfaces:**
- Professor acessa `/app/gestao`.
- UI lista/cria turmas, seleciona turma, cadastra aluno, redefine PIN e edita perfil pedagógico.

- [ ] Add failing source contract checks for route, service calls and accessible form labels.
- [ ] Run verifier and confirm failure.
- [ ] Implement API client and page React.
- [ ] Add navigation from professor session page.
- [ ] Run verifier and TypeScript syntax checks where available.
- [ ] Commit.

### Task 5: Release v0.3.0

**Files:**
- Modify: `backend/pom.xml`
- Modify: `frontend/package.json`
- Modify: `backend/src/main/java/br/com/mathinclui/system/SystemInfoProvider.java`
- Modify: `README.md`
- Modify: `scripts/verify_project.py` if needed

**Interfaces:**
- Version becomes `0.3.0` consistently.

- [ ] Add failing verifier checks for version consistency/documentation.
- [ ] Update versions and README.
- [ ] Run all static verifiers.
- [ ] Inspect git diff for secrets and `.env` exclusion.
- [ ] Commit release.
