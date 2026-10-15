# MathInclui v0.4 Atividades, Questões e Progresso Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Entregar o fluxo completo da v0.4: professor cria/publica/atribui atividades, aluno resolve com dicas e retentativas, e professor acompanha resultados sem exposição antecipada da resposta correta.

**Architecture:** Manter o monólito modular Spring Boot existente, com domínio separado em `atividade`, `questao`, `atribuicao`, `tentativa`, `resposta`, `correcao` e `progresso`. O backend continua sendo a autoridade para ownership, correção e progressão; o React consome DTOs sem regra de negócio crítica. Persistência evolui apenas por novas migrations Flyway V7+.

**Tech Stack:** Java 21, Spring Boot 3.5.6, Spring Data JPA, Spring Security/JWT, PostgreSQL 17, Flyway, JUnit 5, Mockito, Testcontainers PostgreSQL, React 19, TypeScript 5.9, Axios, Vite 7, Vitest + Testing Library.

**Spec:** `docs/superpowers/specs/2026-10-15-mathinclui-v0.4-atividades-design.md`

## Global Constraints

- Anos escolares permitidos: 6, 7 e 8.
- Tipos de questão: `MULTIPLA_ESCOLHA`, `NUMERICA`, `TEXTO_CURTO`.
- Dificuldades: `BASICA`, `INTERMEDIARIA`, `AVANCADA`.
- Limite padrão de tentativas por atividade: 3; mínimo permitido: 1.
- Limite por questão é opcional e, quando ausente, herda o limite da atividade.
- Até três dicas progressivas; limites maiores que três continuam permitindo novas tentativas sem criar novas dicas.
- Uma única tentativa completa por `atribuicao_id + aluno_id` na v0.4.
- Questão encerrada por acerto ou esgotamento do limite não aceita novas respostas.
- `percentual = quantidade_acertos / total_de_questoes * 100`; erros intermediários não aumentam `quantidade_erros`.
- Correção é exclusiva do backend; resposta correta, marcação correta e explicação reveladora não saem antes do acerto ou esgotamento do limite.
- Professor só manipula dados próprios; atividade `SISTEMA` publicada pode ser atribuída, mas não editada pelo professor.
- Aluno só acessa atribuições diretas ou da própria turma e somente as próprias tentativas.
- Atividade com histórico de tentativa não pode sofrer edição destrutiva que invalide resultados.
- Perfil pedagógico continua sem diagnóstico, laudo ou dado clínico.
- Sem IA generativa, Kafka, microsserviços, ranking, tolerância numérica aproximada ou múltiplas tentativas completas após finalização.

## Review Focus

- **Isolamento multi-professor:** professor B não deve inferir nem acessar atividade, atribuição ou resultado do professor A; testes em Tasks 3, 4 e 6.
- **Vazamento de resposta:** DTO do aluno nunca expõe `respostaCorreta`, `correta` de alternativas ou `explicacao` antes da condição permitida; testes em Task 5.
- **Concorrência/dobro envio:** duas chamadas para iniciar ou responder a mesma questão não podem criar segunda tentativa completa nem dois números iguais no histórico; constraint + testes em Tasks 1 e 5.
- **Entrada numérica ambígua:** formatos como `1,234.5`/`1.234,5` são rejeitados; `2`, `2.0` e `2,00` são equivalentes; testes em Task 5.
- **Integridade histórica:** depois da primeira tentativa de aluno, alteração de tipo/resposta/alternativa correta/ordem ou exclusão de questão é recusada; testes em Task 3.

---

### Task 1: Evolução do schema e base de testes PostgreSQL

**Files:**
- Modify: `backend/pom.xml`
- Create: `backend/src/main/resources/db/migration/V7__extend_learning_flow.sql`
- Create: `backend/src/test/java/br/com/mathinclui/support/PostgresIntegrationTest.java`
- Create: `backend/src/test/java/br/com/mathinclui/migration/V7MigrationIT.java`

**Interfaces:**
- Produces DB columns `atividades.limite_tentativas`, `questoes.limite_tentativas`, `tentativas.status`.
- Produces table `resposta_tentativas(id, tentativa_id, questao_id, numero_tentativa, resposta, correta, dica_liberada, tempo_segundos, respondida_em)`.
- Produces uniqueness on `tentativas(atribuicao_id, aluno_id)` and `resposta_tentativas(tentativa_id, questao_id, numero_tentativa)`.
- Produces partial uniqueness for at most one `alternativas.correta=true` per questão.
- Allows `questoes.resposta_correta` to be null only when `tipo='MULTIPLA_ESCOLHA'`.

- [ ] **Step 1: Write the failing migration integration test**

Add `V7MigrationIT` assertions that Flyway reaches V7 and PostgreSQL rejects: `limite_tentativas=0`, duplicate tentativa for same assignment/student, duplicate correct alternatives, and duplicate historical submission number.

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && mvn -Dtest=V7MigrationIT test`
Expected: FAIL because V7 objects/constraints do not exist.

- [ ] **Step 3: Add Testcontainers support and V7 migration**

Add test-scope dependencies `spring-boot-testcontainers`, `org.testcontainers:junit-jupiter`, `org.testcontainers:postgresql`. `PostgresIntegrationTest` owns one PostgreSQL 17 container and supplies datasource properties. V7 must preserve V1–V6 data and only use `ALTER`/`CREATE` additions.

- [ ] **Step 4: Run migration test**

Run: `cd backend && mvn -Dtest=V7MigrationIT test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/pom.xml backend/src/main/resources/db/migration/V7__extend_learning_flow.sql backend/src/test/java/br/com/mathinclui/support backend/src/test/java/br/com/mathinclui/migration
git commit -m "feat: evolve learning flow schema for v0.4"
```

### Task 2: Domínio persistente de atividades, questões e alternativas

**Files:**
- Create: `backend/src/main/java/br/com/mathinclui/atividade/domain/Atividade.java`
- Create: `backend/src/main/java/br/com/mathinclui/atividade/domain/DificuldadeAtividade.java`
- Create: `backend/src/main/java/br/com/mathinclui/atividade/domain/TipoOrigemAtividade.java`
- Create: `backend/src/main/java/br/com/mathinclui/atividade/repository/AtividadeRepository.java`
- Create: `backend/src/main/java/br/com/mathinclui/questao/domain/Questao.java`
- Create: `backend/src/main/java/br/com/mathinclui/questao/domain/TipoQuestao.java`
- Create: `backend/src/main/java/br/com/mathinclui/questao/domain/Alternativa.java`
- Create: `backend/src/main/java/br/com/mathinclui/questao/repository/QuestaoRepository.java`
- Create: `backend/src/main/java/br/com/mathinclui/questao/repository/AlternativaRepository.java`
- Test: `backend/src/test/java/br/com/mathinclui/atividade/domain/AtividadeTest.java`
- Test: `backend/src/test/java/br/com/mathinclui/questao/domain/QuestaoTest.java`

**Interfaces:**
- `Atividade(Long professorId, String titulo, String descricao, short anoEscolar, String conteudo, DificuldadeAtividade dificuldade, int limiteTentativas)` creates `PROFESSOR`, unpublished activity.
- `void Atividade.atualizar(...)`, `void Atividade.publicar()`, `int Atividade.getLimiteTentativas()`.
- `Questao(Long atividadeId, String enunciado, TipoQuestao tipo, String respostaCorreta, ..., int ordem, Integer limiteTentativas)`.
- `int Questao.limiteEfetivo(int limiteAtividade)` returns override or inherited value.
- Repositories expose professor-scoped queries, ordered questions, alternatives by question, and `existsByAtividadeId` support for history checks later.

- [ ] **Step 1: Write failing domain tests**

Tests: `atividade_deve_iniciar_com_limite_tres_quando_informado_tres`, `questao_deve_herdar_limite_da_atividade_quando_override_nulo`, `questao_deve_usar_override_quando_presente`, `limites_menores_que_um_devem_ser_rejeitados`.

- [ ] **Step 2: Run tests and confirm failure**

Run: `cd backend && mvn -Dtest=AtividadeTest,QuestaoTest test`
Expected: FAIL with classes/methods missing.

- [ ] **Step 3: Implement entities/enums/repositories**

Use the same style already established in `turma/domain` and `aluno/domain`: JPA entity with scalar foreign-key IDs, protected no-arg constructor, explicit domain methods, repository queries enforcing ownership/order.

- [ ] **Step 4: Run tests**

Run: `cd backend && mvn -Dtest=AtividadeTest,QuestaoTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/br/com/mathinclui/atividade backend/src/main/java/br/com/mathinclui/questao backend/src/test/java/br/com/mathinclui/atividade backend/src/test/java/br/com/mathinclui/questao
git commit -m "feat: add activity and question domain"
```

### Task 3: CRUD do professor, editor de questões e publicação

**Files:**
- Create DTOs in `backend/src/main/java/br/com/mathinclui/atividade/`: `CreateAtividadeRequest.java`, `UpdateAtividadeRequest.java`, `AtividadeResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/atividade/AtividadeService.java`
- Create: `backend/src/main/java/br/com/mathinclui/atividade/AtividadeController.java`
- Create DTOs in `backend/src/main/java/br/com/mathinclui/questao/`: `AlternativaRequest.java`, `AlternativaResponse.java`, `CreateQuestaoRequest.java`, `UpdateQuestaoRequest.java`, `QuestaoProfessorResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/questao/QuestaoService.java`
- Create: `backend/src/main/java/br/com/mathinclui/questao/QuestaoController.java`
- Create: `backend/src/test/java/br/com/mathinclui/atividade/AtividadeServiceTest.java`
- Create: `backend/src/test/java/br/com/mathinclui/atividade/AtividadeControllerSecurityIT.java`

**Interfaces:**
- `AtividadeResponse AtividadeService.criar(Long professorId, CreateAtividadeRequest request)`
- `List<AtividadeResponse> AtividadeService.listar(Long professorId)` includes own activities plus published `SISTEMA` activities.
- `AtividadeResponse buscar/atualizar(Long professorId, Long atividadeId, ...)`
- `void excluir(Long professorId, Long atividadeId)` only when no historical attempt.
- `AtividadeResponse publicar(Long professorId, Long atividadeId)` validates every question.
- `QuestaoProfessorResponse QuestaoService.criar/atualizar(...)`; `void excluir(...)`.
- REST paths exactly as spec under `/api/v1/atividades` and `/api/v1/atividades/{id}/questoes`.

- [ ] **Step 1: Write failing service tests**

Cover: professor ownership; system activity read-only; publish fails with zero questions; multiple choice needs >=2 alternatives and exactly one correct; numeric/text require `respostaCorreta`; published activity with existing attempt rejects destructive changes.

- [ ] **Step 2: Run tests to verify failure**

Run: `cd backend && mvn -Dtest=AtividadeServiceTest test`
Expected: FAIL.

- [ ] **Step 3: Implement DTOs/services/controllers**

Controller extracts `PROFESSOR:<id>` from JWT following the existing `TurmaController` pattern. Keep answer-bearing professor DTO separate from future student DTO.

- [ ] **Step 4: Write and run security integration tests**

`AtividadeControllerSecurityIT` asserts ALUNO gets 403 on professor routes and professor B gets 404/403 without resource disclosure on professor A data.

Run: `cd backend && mvn -Dtest=AtividadeServiceTest,AtividadeControllerSecurityIT test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/br/com/mathinclui/atividade backend/src/main/java/br/com/mathinclui/questao backend/src/test/java/br/com/mathinclui/atividade
git commit -m "feat: add professor activity authoring and publishing"
```

### Task 4: Atribuições para turma ou aluno

**Files:**
- Create: `backend/src/main/java/br/com/mathinclui/atribuicao/domain/Atribuicao.java`
- Create: `backend/src/main/java/br/com/mathinclui/atribuicao/domain/StatusAtribuicao.java`
- Create: `backend/src/main/java/br/com/mathinclui/atribuicao/repository/AtribuicaoRepository.java`
- Create: `backend/src/main/java/br/com/mathinclui/atribuicao/CreateAtribuicaoRequest.java`
- Create: `backend/src/main/java/br/com/mathinclui/atribuicao/AtribuicaoResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/atribuicao/AtribuicaoService.java`
- Create: `backend/src/main/java/br/com/mathinclui/atribuicao/AtribuicaoController.java`
- Test: `backend/src/test/java/br/com/mathinclui/atribuicao/AtribuicaoServiceTest.java`

**Interfaces:**
- `AtribuicaoResponse criar(Long professorId, CreateAtribuicaoRequest request)` where request has `atividadeId`, exactly one of `turmaId`/`alunoId`, `Instant dataInicio`, optional `Instant dataLimite`.
- `List<AtribuicaoResponse> listar(Long professorId)`.
- Assignment only accepts published own activity or published `SISTEMA` activity.
- `TurmaService.obterDoProfessor` and `AlunoService.obterDoProfessor` are ownership gates.

- [ ] **Step 1: Write failing assignment tests**

Cover exclusive destination, dates, unpublished activity, foreign turma/aluno, published system activity allowed, own published activity allowed.

- [ ] **Step 2: Run and confirm failure**

Run: `cd backend && mvn -Dtest=AtribuicaoServiceTest test`
Expected: FAIL.

- [ ] **Step 3: Implement assignment domain/API**

Persist `ATIVA` by default; eligibility for student listing also checks `dataInicio <= now`, `status=ATIVA`, and `dataLimite` when present.

- [ ] **Step 4: Run tests**

Run: `cd backend && mvn -Dtest=AtribuicaoServiceTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/br/com/mathinclui/atribuicao backend/src/test/java/br/com/mathinclui/atribuicao
git commit -m "feat: add activity assignments"
```

### Task 5: Correção, tentativas, dicas e experiência API do aluno

**Files:**
- Create: `backend/src/main/java/br/com/mathinclui/tentativa/domain/Tentativa.java`
- Create: `backend/src/main/java/br/com/mathinclui/tentativa/domain/StatusTentativa.java`
- Create: `backend/src/main/java/br/com/mathinclui/tentativa/repository/TentativaRepository.java`
- Create: `backend/src/main/java/br/com/mathinclui/resposta/domain/Resposta.java`
- Create: `backend/src/main/java/br/com/mathinclui/resposta/domain/RespostaTentativa.java`
- Create repositories in `backend/src/main/java/br/com/mathinclui/resposta/repository/`
- Create: `backend/src/main/java/br/com/mathinclui/correcao/CorrecaoService.java`
- Create: `backend/src/main/java/br/com/mathinclui/correcao/CorrecaoResultado.java`
- Create: `backend/src/main/java/br/com/mathinclui/tentativa/TentativaService.java`
- Create: `backend/src/main/java/br/com/mathinclui/tentativa/AlunoAtividadeController.java`
- Create student DTOs in `backend/src/main/java/br/com/mathinclui/tentativa/`
- Modify: `backend/src/main/java/br/com/mathinclui/perfilpedagogico/PerfilPedagogicoService.java`
- Tests: `backend/src/test/java/br/com/mathinclui/correcao/CorrecaoServiceTest.java`, `backend/src/test/java/br/com/mathinclui/tentativa/TentativaServiceTest.java`, `backend/src/test/java/br/com/mathinclui/tentativa/AlunoAtividadeSecurityIT.java`

**Interfaces:**
- `CorrecaoResultado corrigir(Questao questao, String resposta)`; for multiple choice, `resposta` is alternative id text and service validates that the alternative belongs to the question.
- `List<AlunoAtividadeResumoResponse> listarAtividades(Long alunoId)`.
- `AlunoAtividadeDetalheResponse detalhar(Long alunoId, Long atribuicaoId)`; student DTO omits answer keys.
- `TentativaResponse iniciar(Long alunoId, Long atribuicaoId)` returns existing `EM_ANDAMENTO`, returns existing `FINALIZADA` without creating another, or creates the unique attempt.
- `RespostaQuestaoResponse responder(Long alunoId, Long tentativaId, Long questaoId, RespostaQuestaoRequest request)` where request contains `String resposta` and `int tempoSegundos`.
- `TentativaResultadoResponse resultado(Long alunoId, Long tentativaId)`.
- Detail response embeds pedagogical support fields `tamanhoFonte`, `leituraVoz`, `apoioVisual`, `dicasPassoAPasso`, `repeticaoGuiada`, `questoesPorTela`, `tempoAmpliado`, `nivelApoio`; v0.4 still renders one answerable question at a time.

- [ ] **Step 1: Write failing correction tests**

Assertions: multiple-choice correct alternative; alternative from another question rejected; `2`, `2.0`, `2,00` compare equal; ambiguous mixed separators rejected; short text trims/collapses spaces/ignores case/preserves accent distinction.

- [ ] **Step 2: Run correction tests**

Run: `cd backend && mvn -Dtest=CorrecaoServiceTest test`
Expected: FAIL.

- [ ] **Step 3: Implement correction engine minimally**

Numeric path parses normalized input into `BigDecimal` and compares with `compareTo`; no epsilon/tolerance.

- [ ] **Step 4: Write failing attempt/progression tests**

Cover: direct and turma assignment visibility; not-yet-started/deadline rules; resume existing attempt; no second complete attempt; wrong answer creates history row and releases next dica; wrong intermediate answer does not increment final error count; correct answer closes question; exhausted limit closes question and reveals explanation/key; closed question rejects new submissions; finishing last question finalizes attempt and computes percentage.

- [ ] **Step 5: Implement attempt, response persistence and student API**

Each submission inserts `RespostaTentativa` with sequential `numeroTentativa` and updates the consolidated `Resposta`. Use DB uniqueness plus transactional service to guard duplicate state.

- [ ] **Step 6: Verify answer-leak and role security**

`AlunoAtividadeSecurityIT` serializes student DTO before/after allowed reveal and asserts forbidden fields are absent before reveal; professor cannot call `/api/v1/aluno/**`; aluno cannot access another aluno's tentativa.

Run: `cd backend && mvn -Dtest=CorrecaoServiceTest,TentativaServiceTest,AlunoAtividadeSecurityIT test`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/br/com/mathinclui/tentativa backend/src/main/java/br/com/mathinclui/resposta backend/src/main/java/br/com/mathinclui/correcao backend/src/main/java/br/com/mathinclui/perfilpedagogico backend/src/test/java/br/com/mathinclui/correcao backend/src/test/java/br/com/mathinclui/tentativa
git commit -m "feat: add student activity solving flow"
```

### Task 6: Resultados e dashboard do professor

**Files:**
- Create: `backend/src/main/java/br/com/mathinclui/progresso/AtividadeResultadosResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/progresso/AlunoResultadoResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/progresso/QuestaoResultadoResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/progresso/ProgressoService.java`
- Modify: `backend/src/main/java/br/com/mathinclui/atribuicao/AtribuicaoController.java`
- Test: `backend/src/test/java/br/com/mathinclui/progresso/ProgressoServiceTest.java`

**Interfaces:**
- `AtividadeResultadosResponse resultados(Long professorId, Long atribuicaoId)`.
- Response includes total assigned/eligible students, participants, completed, average percentage, per-question final error count/rate, per-student score, time, total submissions and total dicas.
- No ranking field or ordinal placement.

- [ ] **Step 1: Write failing aggregation tests**

Use fixtures with completed and in-progress attempts; assert participation, mean percentage, hardest-question metrics, per-student attempts/dicas/time and professor ownership.

- [ ] **Step 2: Run and confirm failure**

Run: `cd backend && mvn -Dtest=ProgressoServiceTest test`
Expected: FAIL.

- [ ] **Step 3: Implement result queries/aggregation**

Prefer repository projections/targeted queries where counts would otherwise cause N+1; keep aggregation deterministic and read-only.

- [ ] **Step 4: Run tests**

Run: `cd backend && mvn -Dtest=ProgressoServiceTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/br/com/mathinclui/progresso backend/src/main/java/br/com/mathinclui/atribuicao backend/src/test/java/br/com/mathinclui/progresso
git commit -m "feat: add professor activity results"
```

### Task 7: Frontend test harness e clientes API v0.4

**Files:**
- Modify: `frontend/package.json`
- Modify: `frontend/vite.config.ts`
- Create: `frontend/src/test/setup.ts`
- Create: `frontend/src/services/atividadesApi.ts`
- Create: `frontend/src/services/alunoAtividadesApi.ts`
- Test: `frontend/src/services/atividadesApi.test.ts`
- Test: `frontend/src/services/alunoAtividadesApi.test.ts`

**Interfaces:**
- Add scripts `test` and `test:run` using Vitest.
- Professor client maps all authoring/assignment/results endpoints from Tasks 3, 4 and 6.
- Student client maps list/detail/start/respond/result endpoints from Task 5.
- Both reuse the authenticated Axios instance exported by `authApi.ts`.

- [ ] **Step 1: Add test dependencies and failing API-client tests**

Dependencies: `vitest`, `jsdom`, `@testing-library/react`, `@testing-library/jest-dom`, `@testing-library/user-event`. Mock Axios and assert exact method/path/payload contracts.

- [ ] **Step 2: Run tests and confirm failure**

Run: `cd frontend && npm run test:run`
Expected: FAIL because clients do not exist.

- [ ] **Step 3: Implement typed API clients**

Do not duplicate auth/refresh logic; all calls use `api` from `authApi.ts`.

- [ ] **Step 4: Run tests and build**

Run: `cd frontend && npm run test:run && npm run build`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add frontend/package.json frontend/package-lock.json frontend/vite.config.ts frontend/src/test frontend/src/services
git commit -m "test: add frontend harness and v0.4 api clients"
```

### Task 8: UI do professor — atividades, editor, atribuição e resultados

**Files:**
- Create: `frontend/src/features/atividades/AtividadesProfessorPage.tsx`
- Create: `frontend/src/features/atividades/AtividadeEditor.tsx`
- Create: `frontend/src/features/atividades/AtribuirAtividadePanel.tsx`
- Create: `frontend/src/features/atividades/ResultadosAtividadePanel.tsx`
- Create tests beside each component using `*.test.tsx`
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/features/auth/SessionPage.tsx`
- Modify: `frontend/src/styles.css`

**Interfaces:**
- Route: `/app/atividades` for professor only.
- Page states: list → editor → assign → results.
- Editor supports three question types, up to three dicas, activity default attempt limit and per-question override.
- Results shows participation, average, question difficulty indicators and student detail without competitive ranking.

- [ ] **Step 1: Write failing route/role and editor tests**

Tests assert ALUNO session is redirected to `/app`; professor sees `Minhas Atividades`; create form emits correct payload; multiple choice editor requires alternatives; publish/assign actions call API client.

- [ ] **Step 2: Run targeted tests**

Run: `cd frontend && npm run test:run -- AtividadesProfessorPage AtividadeEditor`
Expected: FAIL.

- [ ] **Step 3: Implement professor UI**

Reuse existing visual vocabulary (`page-shell`, `card`, buttons) and add focused v0.4 classes; avoid rewriting v0.3 management page.

- [ ] **Step 4: Add assignment/results tests and implementation**

Assert exclusive turma/aluno destination in the UI and result metrics rendering.

- [ ] **Step 5: Run frontend suite/build**

Run: `cd frontend && npm run test:run && npm run build`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/features/atividades frontend/src/App.tsx frontend/src/features/auth/SessionPage.tsx frontend/src/styles.css
git commit -m "feat: add professor activity workspace"
```

### Task 9: UI do aluno — lista, resolução adaptada, dicas e conclusão

**Files:**
- Create: `frontend/src/features/aluno-atividades/AlunoAtividadesPage.tsx`
- Create: `frontend/src/features/aluno-atividades/ResolverAtividadePage.tsx`
- Create: `frontend/src/features/aluno-atividades/QuestaoCard.tsx`
- Create: `frontend/src/features/aluno-atividades/ResultadoAlunoPage.tsx`
- Create tests beside each component using `*.test.tsx`
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/features/auth/SessionPage.tsx`
- Modify: `frontend/src/styles.css`

**Interfaces:**
- Routes: `/app/atividades-aluno`, `/app/atividades-aluno/:atribuicaoId`, `/app/tentativas/:tentativaId/resultado`.
- Student home groups `Para fazer`, `Em andamento`, `Concluídas`.
- Resolver displays one answerable question at a time with `Questão X de N`, feedback, dica and retry state.
- Pedagogical profile controls CSS/font sizing, visual-support affordances, guided repetition/step-by-step prominence and optional `speechSynthesis` button when `leituraVoz=true`; `tempoAmpliado` never changes assignment deadline.

- [ ] **Step 1: Write failing student list/route tests**

Assert professor session cannot render student solving page; aluno sees grouped activity states and resume CTA for `EM_ANDAMENTO`.

- [ ] **Step 2: Write failing retry/reveal tests**

Mock sequence: wrong response → dica visible and no answer key; another wrong until limit → explanation/key visible; correct response → positive feedback and next question. Also assert one question card is answerable at a time.

- [ ] **Step 3: Implement student pages and adaptive presentation**

Never infer correctness in React. Render only fields returned by backend. `questoesPorTela` remains part of support metadata, but v0.4 honors the explicit one-question solving flow.

- [ ] **Step 4: Run student tests/build**

Run: `cd frontend && npm run test:run -- AlunoAtividades ResolverAtividade QuestaoCard ResultadoAluno && npm run build`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/features/aluno-atividades frontend/src/App.tsx frontend/src/features/auth/SessionPage.tsx frontend/src/styles.css
git commit -m "feat: add inclusive student activity experience"
```

### Task 10: Release v0.4.0, verifier e validação ponta a ponta

**Files:**
- Modify: `backend/pom.xml` version to `0.4.0`
- Modify: `frontend/package.json` version to `0.4.0`
- Modify: `backend/src/main/java/br/com/mathinclui/system/SystemInfoProvider.java` version to `0.4.0`
- Modify: `README.md`
- Create: `scripts/verify_v04.py`
- Test/verify: complete backend/frontend suites and Docker Compose flow

**Interfaces:**
- `GET /api/v1/system/info` returns `0.4.0`.
- README documents v0.4 flow, V7 migration, test commands, professor/student routes and revised roadmap (v0.4 now includes student experience + results).
- `verify_v04.py` checks required files/routes/version strings and forbids clinical fields or accidental answer-key exposure in student DTO source.

- [ ] **Step 1: Write failing structural verifier**

Checks all v0.4 domain/controller/page files, REST route markers, V7 migration markers, version `0.4.0`, frontend routes and student DTO exclusion markers.

- [ ] **Step 2: Run verifier before release edits**

Run: `python scripts/verify_v04.py`
Expected: FAIL until release metadata/docs are complete.

- [ ] **Step 3: Update versions and documentation**

Update roadmap so v0.5/v0.6 no longer claim work already consolidated into v0.4.

- [ ] **Step 4: Run automated verification**

Run:

```bash
cd backend && mvn test
cd ../frontend && npm run test:run && npm run build
cd .. && python scripts/verify_project.py && python scripts/verify_v03.py && python scripts/verify_v04.py
docker compose --env-file .env config -q
docker compose --env-file .env up --build -d
docker compose ps
```

Expected: all automated tests/builds/verifiers pass; PostgreSQL and backend healthy; frontend started.

- [ ] **Step 5: Execute manual E2E acceptance**

Professor: login → create activity → add multiple-choice + numeric + short-text questions → set default/override attempts → publish → assign to turma/student. Student: login → list → start → wrong answer receives progressive dica → retry → correct/exhaust limit → finish. Professor: results show participation, score, submissions, dicas, time and question-error metrics. Finally verify aluno cannot open professor routes and professor B cannot access professor A resources.

- [ ] **Step 6: Commit release preparation**

```bash
git add backend/pom.xml frontend/package.json frontend/package-lock.json backend/src/main/java/br/com/mathinclui/system/SystemInfoProvider.java README.md scripts/verify_v04.py
git commit -m "chore: prepare MathInclui v0.4.0"
```

## Self-review result

- **Spec coverage:** creation/editing/publication, assignment, student list/start/resume/respond, retries/dicas, all correction types, progress/result dashboard, pedagogical presentation, security, historical integrity, migrations, automated tests, E2E and release metadata are each owned by a task.
- **Step granularity:** each task follows fail → implement → verify → commit; no implementation body is prescribed where tests/signatures already determine behavior.
- **Type consistency:** `Long` IDs, `Instant` dates, enum strings and response names are consistent across backend and TypeScript client boundaries.
- **Review Focus coverage:** cross-professor access (Tasks 3/4/6), answer leak (Task 5), duplicate/concurrent state (Tasks 1/5), numeric ambiguity (Task 5), historical mutation (Task 3).
- **Scope discipline:** no IA, Kafka, ranking, approximate numeric matching, second complete attempt, native mobile app or BI was added.
