# MathInclui v0.5 — Acessibilidade Adaptativa e Banco Pedagógico Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Entregar a v0.5 com acessibilidade adaptativa calculada no backend, preferências persistentes do aluno, adaptações por atribuição/aluno e um banco pedagógico reutilizável com biblioteca oficial, biblioteca pessoal, BNCC, tags, cópia profunda de atividades e banco de questões.

**Architecture:** Manter o monólito modular. Os módulos existentes continuam responsáveis pelo ciclo da v0.4; `acessibilidade` centraliza composição de apoio e `biblioteca` centraliza descoberta/reutilização de conteúdo. O backend permanece a fonte de verdade para ownership, composição de acessibilidade, cópias e gabarito. O frontend recebe DTOs efetivos e não replica regras críticas. Persistência evolui somente por migrations Flyway aditivas V9–V12.

**Tech Stack:** Java 21, Spring Boot 3.5.6, Spring Security/JWT, Spring Data JPA, PostgreSQL 17, Flyway, JUnit 5, Mockito, Testcontainers, React 19, TypeScript 5.9, Axios, React Router, Vite 7, Vitest + Testing Library, Docker/Nginx.

**Spec:** `docs/superpowers/specs/2026-10-16-mathinclui-v0.5-acessibilidade-biblioteca-design.md`

## Global Constraints

- Não alterar migrations `V1`–`V8`; a v0.5 começa em `V9`.
- Preservar os literais persistidos existentes: `TamanhoFonte = PADRAO | GRANDE | EXTRA_GRANDE` e `NivelApoio = PADRAO | MODERADO | AMPLIADO`. A ordem concreta é `PADRAO < GRANDE < EXTRA_GRANDE` e `PADRAO < MODERADO < AMPLIADO`; não renomear enums para termos conceituais usados na spec.
- A composição de acessibilidade é monotônica: uma camada posterior pode aumentar apoio, nunca reduzi-lo abaixo do mínimo pedagógico já definido.
- `null` em overrides significa herança. Um `false` armazenado não pode reduzir um `true` exigido por outra camada na configuração efetiva.
- Preferências do aluno persistem somente para fonte, leitura em voz e apoio visual; podem aumentar/ativar recursos opcionais, nunca reduzir mínimos.
- Adaptação individual só existe em atribuição destinada a **turma**. Atribuição direta a aluno deve rejeitar override individual com `409 CONFLICT`.
- Em atribuição de turma, o aluno do override individual deve estar ativo, pertencer à turma atribuída e ser do professor autenticado. Recurso cruzado/inacessível retorna `404`, seguindo o padrão atual; role inadequada continua `403` via SecurityConfig.
- Atividades v0.4 sem BNCC, tags ou overrides continuam válidas.
- Conteúdo `SISTEMA` é somente leitura. Conteúdo pessoal de outro professor não é visível nem copiável.
- Cópias são independentes da origem. Proveniência é informativa; FKs de origem devem ser nullable com `ON DELETE SET NULL`, nunca cascade destrutivo nem bloqueio da exclusão da fonte.
- Cópia de atividade, cópia de modelo e importação de múltiplas questões são transacionais.
- Importação de questões anexa após a maior `ordem` existente, preserva a ordem recebida e rejeita IDs repetidos no payload com `400`.
- Alterar classificação BNCC/tags de atividade com histórico é alteração histórica e deve usar `validarSemHistorico`.
- O aluno continua sem receber gabarito, alternativa correta ou explicação antes da condição permitida pela v0.4.
- Leitura em voz usa `window.speechSynthesis` quando disponível, sem autoplay; ausência do recurso não pode quebrar a resolução.
- `repeticaoGuiada=true` muda a apresentação do retry para um fluxo orientado (“Vamos por partes” + dica disponível + nova tentativa), sem IA e sem criar uma segunda tentativa global.
- A biblioteca oficial inicial é pequena e representativa dos 6º, 7º e 8º anos; não tenta cobrir o currículo inteiro.
- Sem IA generativa, busca semântica, Elasticsearch, ranking, perfil `ADMIN`, sincronização automática de cópias, workflow colaborativo de publicação ou dados clínicos.

## Review Focus

- **Compatibilidade de enums persistidos:** nenhuma migration/DTO introduz `NORMAL`, `BAIXO`, `MEDIO` ou `ALTO` no banco; Tasks 1–3.
- **Escopo do override individual:** somente atribuição de turma e aluno da turma correta; Task 3.
- **Independência/atomicidade:** apagar/editar fonte não afeta cópia e rollback não deixa dados parciais; Tasks 4–6.
- **Segurança/não vazamento:** outro professor não vê conteúdo pessoal, aluno não acessa biblioteca/dados alheios e DTO do aluno não ganha gabarito; Tasks 3, 5, 6 e 12.
- **Fallback de acessibilidade:** preferência não reduz mínimo, repetição guiada é determinística e ausência de `speechSynthesis` não quebra a tela; Task 11.

## Preflight antes da implementação

```bash
cd backend && mvn test
cd ../frontend && npm install && npm run test:run && npm run build
cd .. && python scripts/verify_project.py && python scripts/verify_v03.py && python scripts/verify_v04.py
```

Esperado: baseline verde. Se falhar sem mudanças v0.5, parar e diagnosticar antes da Task 1.

---

### Task 1: Schema V9 de acessibilidade e contrato de migration

**Files:**
- Create: `backend/src/main/resources/db/migration/V9__create_accessibility_v05.sql`
- Create: `backend/src/test/java/br/com/mathinclui/migration/V9AccessibilityMigrationIT.java`
- Modify: `backend/src/test/java/br/com/mathinclui/migration/V7MigrationIT.java`

**Interfaces:**
- `preferencias_interface_aluno(aluno_id PK/FK, tamanho_fonte, leitura_voz, apoio_visual, atualizado_em)`.
- `adaptacoes_atribuicao(atribuicao_id PK/FK, tamanho_fonte_minima, leitura_voz_minima, apoio_visual_minimo, dicas_passo_a_passo, repeticao_guiada, questoes_por_tela, tempo_ampliado, nivel_apoio)`.
- `adaptacoes_aluno_atribuicao(id PK, atribuicao_id, aluno_id, mesmos campos nullable, UNIQUE(atribuicao_id, aluno_id))`.
- Enums de banco usam apenas `PADRAO/GRANDE/EXTRA_GRANDE` e `PADRAO/MODERADO/AMPLIADO`; `questoes_por_tela` nullable ou 1–10.

- [ ] **Step 1: RED** — Criar `V9AccessibilityMigrationIT` cobrindo tabelas, FKs, unicidade, range, enum inválido e literais atuais. Em `V7MigrationIT`, remover somente a asserção de “versão corrente = 8”; preservar regras V7/V8.
- [ ] **Step 2: Rodar RED** — `cd backend && mvn -Dtest=V9AccessibilityMigrationIT,V7MigrationIT test`. Esperado: FAIL por V9 ausente.
- [ ] **Step 3: Implementar V9** — Apenas `CREATE TABLE/INDEX`; V1–V8 intocadas. `ON DELETE CASCADE` para preferências/adaptações que não fazem sentido sem aluno/atribuição.
- [ ] **Step 4: GREEN** — repetir comando; esperado PASS.
- [ ] **Step 5: Commit**

```bash
git add backend/src/main/resources/db/migration/V9__create_accessibility_v05.sql backend/src/test/java/br/com/mathinclui/migration/V9AccessibilityMigrationIT.java backend/src/test/java/br/com/mathinclui/migration/V7MigrationIT.java
git commit -m "feat: add v0.5 accessibility schema"
```

---

### Task 2: Domínio de acessibilidade, preferências e algoritmo efetivo

**Files:**
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/domain/PreferenciaInterfaceAluno.java`
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/domain/AdaptacaoAtribuicao.java`
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/domain/AdaptacaoAlunoAtribuicao.java`
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/domain/ConfiguracaoAcessibilidadeEfetiva.java`
- Create repositories under: `backend/src/main/java/br/com/mathinclui/acessibilidade/repository/`
- Create: `PreferenciaInterfaceRequest.java`, `PreferenciaInterfaceResponse.java`, `ConfiguracaoAcessibilidadeResponse.java`, `PreferenciaInterfaceService.java`, `AcessibilidadeEfetivaService.java`, `AlunoAcessibilidadeController.java` under `backend/src/main/java/br/com/mathinclui/acessibilidade/`
- Create tests: `backend/src/test/java/br/com/mathinclui/acessibilidade/AcessibilidadeEfetivaServiceTest.java`, `PreferenciaInterfaceServiceTest.java`

**Interfaces:**
```java
ConfiguracaoAcessibilidadeEfetiva calcular(Long alunoId, Long atribuicaoId)
PreferenciaInterfaceResponse buscar(Long alunoId)
PreferenciaInterfaceResponse atualizar(Long alunoId, PreferenciaInterfaceRequest request)
```
HTTP aluno: `GET /api/v1/aluno/atribuicoes/{id}/acessibilidade`, `GET/PUT /api/v1/aluno/preferencias-interface`.

- [ ] **Step 1: RED merge** — Testar fonte mínima, aumento de fonte, OR de booleanos de apoio, `min(questoesPorTela)`, maior `NivelApoio`, `null` herdado e atribuição não destinada ao aluno = 404.
- [ ] **Step 2: RED preferências** — Testar defaults `PADRAO/false/false`, upsert do próprio aluno e DTO com somente fonte/leitura/apoio visual.
- [ ] **Step 3: Rodar RED** — `cd backend && mvn -Dtest=AcessibilidadeEfetivaServiceTest,PreferenciaInterfaceServiceTest test`.
- [ ] **Step 4: Implementar** — Reusar enums atuais e comparar por funções explícitas, não `ordinal()` como contrato. Validar `atribuicao.destinadaAo` antes do merge. Controller extrai `ALUNO:<id>` do JWT e não aceita identidade arbitrária.
- [ ] **Step 5: GREEN** — repetir testes.
- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/br/com/mathinclui/acessibilidade backend/src/test/java/br/com/mathinclui/acessibilidade
git commit -m "feat: calculate effective student accessibility"
```

---

### Task 3: APIs do professor para adaptações e detalhe do aluno

**Files:**
- Create: `AdaptacaoRequest.java`, `AdaptacaoResponse.java`, `AdaptacaoAtribuicaoService.java`, `AdaptacaoAtribuicaoController.java` em `backend/src/main/java/br/com/mathinclui/acessibilidade/`
- Modify: `backend/src/main/java/br/com/mathinclui/tentativa/AlunoAtividadeDetalheResponse.java`
- Modify: `backend/src/main/java/br/com/mathinclui/tentativa/AlunoAtividadeService.java`
- Create tests: `backend/src/test/java/br/com/mathinclui/acessibilidade/AdaptacaoAtribuicaoServiceTest.java`, `backend/src/test/java/br/com/mathinclui/tentativa/AlunoAtividadeAccessibilityTest.java`

**Interfaces:**
- `GET/PUT /api/v1/atribuicoes/{id}/adaptacao`
- `GET/PUT/DELETE /api/v1/atribuicoes/{id}/alunos/{alunoId}/adaptacao`
- Service: `buscarGeral`, `salvarGeral`, `buscarIndividual`, `salvarIndividual`, `excluirIndividual`, sempre com `professorId` autenticado.
- `AlunoAtividadeDetalheResponse` troca `perfilPedagogico` por `ConfiguracaoAcessibilidadeResponse acessibilidade`.

- [ ] **Step 1: RED** — Professor B não altera A; atribuição direta + override individual = 409; aluno fora da turma/inacessível = 404; turma correta = sucesso; merge final não reduz perfil.
- [ ] **Step 2: Rodar RED** — `cd backend && mvn -Dtest=AdaptacaoAtribuicaoServiceTest,AlunoAtividadeAccessibilityTest test`.
- [ ] **Step 3: Implementar ownership** — Usar `AtribuicaoService.obterDoProfessor`; individual exige `turmaId != null`, `AlunoService.obterDoProfessor` e `aluno.getTurmaId().equals(atribuicao.getTurmaId())`.
- [ ] **Step 4: Integrar detalhe** — `AlunoAtividadeService` usa `AcessibilidadeEfetivaService`; preservar DTO de questões sem resposta correta/alternativa correta.
- [ ] **Step 5: GREEN + regressão** — rodar testes da task e testes de `tentativa` existentes.
- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/br/com/mathinclui/acessibilidade backend/src/main/java/br/com/mathinclui/tentativa backend/src/test/java/br/com/mathinclui/acessibilidade backend/src/test/java/br/com/mathinclui/tentativa/AlunoAtividadeAccessibilityTest.java
git commit -m "feat: add assignment accessibility overrides"
```

---

### Task 4: Schema V10 do banco pedagógico, proveniência, BNCC e tags

**Files:**
- Create: `backend/src/main/resources/db/migration/V10__create_pedagogical_library_v05.sql`
- Create: `backend/src/test/java/br/com/mathinclui/migration/V10LibraryMigrationIT.java`
- Modify: `backend/src/main/java/br/com/mathinclui/atividade/domain/Atividade.java`
- Modify: `backend/src/main/java/br/com/mathinclui/questao/domain/Questao.java`
- Create: `HabilidadeBncc.java`, `TagProfessor.java`, `TipoOrigemBiblioteca.java`, `QuestaoModelo.java`, `AlternativaModelo.java` em `backend/src/main/java/br/com/mathinclui/biblioteca/domain/`
- Create repositories under: `backend/src/main/java/br/com/mathinclui/biblioteca/repository/`

**Schema:**
- `habilidades_bncc(id, codigo UNIQUE, ano_escolar, unidade_tematica, objeto_conhecimento, descricao)`.
- N:N `atividade_habilidade_bncc` e `questao_modelo_habilidade_bncc`.
- `tags_professor(id, professor_id, nome, nome_normalizado, UNIQUE(professor_id,nome_normalizado))`, joins `atividade_tag` e `questao_modelo_tag` com cascade apenas nas tabelas de relação.
- `questoes_modelo`/`alternativas_modelo` com origem `SISTEMA|PROFESSOR`, ownership coerente, tipos/dificuldades atuais, dicas, explicação, imagem, limite, ano e conteúdo.
- `atividades.origem_atividade_id` self-FK nullable `ON DELETE SET NULL` + `copiado_em`.
- `questoes.origem_questao_modelo_id` nullable `ON DELETE SET NULL` + `copiado_em`.
- `questoes_modelo.origem_modelo_id` self-FK nullable `ON DELETE SET NULL` + `copiado_em`.

- [ ] **Step 1: RED migration** — Cobrir tabelas, ownership, alternativa correta única, resposta obrigatória para NUMERICA/TEXTO_CURTO, tag normalizada por professor, N:N e deleção da fonte mantendo cópia com proveniência null.
- [ ] **Step 2: Rodar RED** — `cd backend && mvn -Dtest=V10LibraryMigrationIT test`.
- [ ] **Step 3: Implementar V10** — Criar tabelas independentes → relações → `ALTER` de atividades/questões. Adicionar índices de ano/unidade, código, origem/professor, conteúdo, dificuldade e FKs.
- [ ] **Step 4: Mapear JPA mínimo** — Sem herança JPA complexa. Preservar construtores existentes e adicionar factories/getters de proveniência.
- [ ] **Step 5: GREEN** — repetir teste.
- [ ] **Step 6: Commit**

```bash
git add backend/src/main/resources/db/migration/V10__create_pedagogical_library_v05.sql backend/src/main/java/br/com/mathinclui/biblioteca backend/src/main/java/br/com/mathinclui/atividade/domain/Atividade.java backend/src/main/java/br/com/mathinclui/questao/domain/Questao.java backend/src/test/java/br/com/mathinclui/migration/V10LibraryMigrationIT.java
git commit -m "feat: add pedagogical library schema"
```

---

### Task 5: Biblioteca de atividades, BNCC, tags, classificação e deep copy

**Files:**
- Create DTOs/services/controllers em `backend/src/main/java/br/com/mathinclui/biblioteca/`: `PageResponse`, `BibliotecaAtividadeResumoResponse`, `BibliotecaAtividadeDetalheResponse`, `HabilidadeBnccResponse`, `TagResponse`, `ClassificacaoAtividadeRequest`, `ClassificacaoAtividadeResponse`, `BibliotecaAtividadeService`, `ClassificacaoAtividadeService`, `TagService`, `BnccService`, `BibliotecaController`
- Modify: `backend/src/main/java/br/com/mathinclui/auth/security/SecurityConfig.java`
- Create tests: `BibliotecaAtividadeServiceTest.java`, `ClassificacaoAtividadeServiceTest.java`, `TagServiceTest.java`

**HTTP:**
- `GET /api/v1/biblioteca/atividades?origem=&ano=&unidadeTematica=&objetoConhecimento=&habilidade=&conteudo=&dificuldade=&tag=&busca=&page=0&size=20`
- `GET /api/v1/biblioteca/atividades/{id}`
- `POST /api/v1/biblioteca/atividades/{id}/copiar`
- `GET /api/v1/biblioteca/bncc?ano=6|7|8`
- `GET/POST /api/v1/biblioteca/tags`, `DELETE /api/v1/biblioteca/tags/{id}`
- `GET/PUT /api/v1/atividades/{id}/classificacao`, PUT `{habilidadeIds:[], tagIds:[]}`.
- Paginação `page>=0`, `1<=size<=100`, default 20; busca `ILIKE` e ordem determinística `titulo,id`.

Tag normalizada = Unicode NFC + trim + colapso de espaços + lowercase `Locale.ROOT`, preservando acentos.

- [ ] **Step 1: RED** — Sistema publicado + próprias aparecem; pessoal de outro professor não; deep copy gera `PROFESSOR/publicada=false`, copia questões/alternativas/BNCC; cópia é independente; tags isoladas; classificação com histórico = 409.
- [ ] **Step 2: Rodar RED** — `cd backend && mvn -Dtest=BibliotecaAtividadeServiceTest,ClassificacaoAtividadeServiceTest,TagServiceTest test`.
- [ ] **Step 3: Implementar busca/DTOs** — Query no banco, não filtro em memória. Detalhe para professor pode conter gabarito.
- [ ] **Step 4: Deep copy `@Transactional`** — Copiar base, habilidades, tags do mesmo professor quando fonte pessoal, questões/alternativas, ordem/limites; proveniência imediata; sem chamadas HTTP internas.
- [ ] **Step 5: Classificação/tags/security** — `/api/v1/biblioteca/**` → `ROLE_PROFESSOR`; tags devem pertencer ao professor; `PUT classificacao` usa `validarSemHistorico`.
- [ ] **Step 6: GREEN** — repetir testes.
- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/br/com/mathinclui/biblioteca backend/src/main/java/br/com/mathinclui/auth/security/SecurityConfig.java backend/src/test/java/br/com/mathinclui/biblioteca
git commit -m "feat: add searchable activity library"
```

---

### Task 6: Banco de questões, salvar/copy/import e rollback transacional

**Files:**
- Create: `QuestaoModeloResponse.java`, `SalvarQuestaoNoBancoRequest.java`, `ImportarQuestoesRequest.java`, `QuestaoModeloService.java`, `QuestaoBibliotecaController.java` em `backend/src/main/java/br/com/mathinclui/biblioteca/`
- Modify: `backend/src/main/java/br/com/mathinclui/questao/QuestaoController.java`
- Create: `backend/src/test/java/br/com/mathinclui/biblioteca/QuestaoModeloServiceTest.java`
- Create: `backend/src/test/java/br/com/mathinclui/biblioteca/BibliotecaTransactionIT.java`
- Create: `backend/src/test/java/br/com/mathinclui/support/SpringPostgresIntegrationTest.java`

**HTTP:**
- `GET /api/v1/biblioteca/questoes?...`, `GET /api/v1/biblioteca/questoes/{id}`.
- `POST /api/v1/biblioteca/questoes/{id}/copiar` → modelo pessoal independente.
- `POST /api/v1/atividades/{atividadeId}/questoes/{questaoId}/salvar-no-banco` com `anoEscolar`, `conteudo`, `dificuldade`, `habilidadeIds`, `tagIds`.
- `POST /api/v1/atividades/{atividadeId}/questoes/importar` com `{questoesModeloIds:[...]}` → novas questões concretas.

- [ ] **Step 1: RED unitário** — Salvar questão cria modelo independente; alternativas/gabarito ficam em área professor; sistema → cópia pessoal; modelo pessoal de outro professor = 404; import apenda ordem; ID duplicado = 400; target com histórico = 409; editar modelo não muda questão importada.
- [ ] **Step 2: Rodar RED** — `cd backend && mvn -Dtest=QuestaoModeloServiceTest test`.
- [ ] **Step 3: Implementar** — “Salvar no banco” é leitura/cópia e pode partir de atividade com histórico; importar é mutação e exige `validarSemHistorico`. Copiar corretamente múltipla escolha vs numérica/texto.
- [ ] **Step 4: RED/GREEN transacional** — `SpringPostgresIntegrationTest` usa `@SpringBootTest`, PostgreSQL 17, datasource/JWT secret via `@DynamicPropertySource`. `BibliotecaTransactionIT` prova rollback tanto de cópia de atividade quanto de importação de múltiplas questões ao falhar na segunda gravação; contagens antes/depois iguais.
- [ ] **Step 5: GREEN** — `cd backend && mvn -Dtest=QuestaoModeloServiceTest,BibliotecaTransactionIT test`.
- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/br/com/mathinclui/biblioteca backend/src/main/java/br/com/mathinclui/questao/QuestaoController.java backend/src/test/java/br/com/mathinclui/biblioteca backend/src/test/java/br/com/mathinclui/support/SpringPostgresIntegrationTest.java
git commit -m "feat: add reusable question bank"
```

---

### Task 7: BNCC validada, V11 e biblioteca oficial V12

**Files:**
- Create: `docs/references/bncc-matematica-6-8.md`
- Create: `backend/src/main/resources/db/migration/V11__seed_bncc_math_6_8.sql`
- Create: `backend/src/main/resources/db/migration/V12__seed_official_library_v05.sql`
- Create: `backend/src/test/java/br/com/mathinclui/migration/V12OfficialLibraryIT.java`

**Sources/contract:**
- Fonte primária oficial MEC: `https://basenacionalcomum.mec.gov.br/images/BNCC_EI_EF_110518_versaofinal_site.pdf`.
- Conferir erratas: `https://basenacionalcomum.mec.gov.br/images/implementacao/ERRATAS-BNCC_site.pdf`.
- Documento de referência registra URL, acesso, páginas/trechos e dados transcritos. Nenhum código BNCC é inferido da memória.
- V11 semeia apenas as habilidades usadas pela amostra, pelo menos duas por ano.
- V12 cria pelo menos duas atividades `SISTEMA` por ano (6 no total) e também **modelos oficiais de questão**, pelo menos dois por ano e cobrindo no conjunto `MULTIPLA_ESCOLHA`, `NUMERICA` e `TEXTO_CURTO`. Modelos têm BNCC validada; questões concretas das atividades podem apontar para esses modelos via proveniência, mas continuam linhas independentes.

- [ ] **Step 1: Validar fonte antes do SQL** — Criar `docs/references/bncc-matematica-6-8.md`; registrar erratas relevantes.
- [ ] **Step 2: RED** — `V12OfficialLibraryIT` exige Flyway 12, BNCC 6/7/8, atividades SISTEMA publicadas sem professor, modelos SISTEMA sem professor, três tipos de questão, alternativa correta única, associações válidas e nenhum placeholder.
- [ ] **Step 3: Rodar RED** — `cd backend && mvn -Dtest=V12OfficialLibraryIT test`.
- [ ] **Step 4: Implementar V11/V12 determinísticos** — Resolver relações por códigos/títulos controlados; não depender de IDs serial preexistentes.
- [ ] **Step 5: GREEN migrations** — `cd backend && mvn -Dtest=V9AccessibilityMigrationIT,V10LibraryMigrationIT,V12OfficialLibraryIT,V7MigrationIT test`.
- [ ] **Step 6: Commit**

```bash
git add docs/references/bncc-matematica-6-8.md backend/src/main/resources/db/migration/V11__seed_bncc_math_6_8.sql backend/src/main/resources/db/migration/V12__seed_official_library_v05.sql backend/src/test/java/br/com/mathinclui/migration/V12OfficialLibraryIT.java
git commit -m "feat: seed validated BNCC library content"
```

---

### Task 8: Frontend do professor — Biblioteca

**Files:**
- Create: `frontend/src/services/bibliotecaApi.ts`
- Create: `frontend/src/features/biblioteca/BibliotecaPage.tsx`, `BibliotecaFilters.tsx`, `AtividadeBibliotecaCard.tsx`, `BibliotecaPage.test.tsx`
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/features/auth/SessionPage.tsx`
- Create: `frontend/src/v05.css`

**Interfaces:** `/app/biblioteca`; views Atividades/Questões/Minha biblioteca; filtros viram query params; “Usar atividade” copia uma vez e encaminha à edição da cópia.

- [ ] **Step 1: RED** — Filtros, paginação, vazio/erro, item SISTEMA sem Editar, copy com prevenção de duplo submit.
- [ ] **Step 2: Rodar RED** — `cd frontend && npm run test:run -- BibliotecaPage.test.tsx`.
- [ ] **Step 3: Implementar** — Componentes separados; `App` importa `v05.css`; `SessionPage` do professor ganha Biblioteca.
- [ ] **Step 4: GREEN/build** — teste da page + `npm run build`.
- [ ] **Step 5: Commit**

```bash
git add frontend/src/services/bibliotecaApi.ts frontend/src/features/biblioteca frontend/src/App.tsx frontend/src/features/auth/SessionPage.tsx frontend/src/v05.css
git commit -m "feat: add professor pedagogical library UI"
```

---

### Task 9: Editor — BNCC, tags e reutilização de questões

**Files:**
- Create: `frontend/src/features/atividades/ClassificacaoCurricularFields.tsx`
- Create: `frontend/src/features/atividades/QuestaoLibraryPicker.tsx`, `QuestaoLibraryPicker.test.tsx`
- Modify: `frontend/src/features/atividades/AtividadeEditor.tsx`, `AtividadeEditor.test.tsx`
- Modify: `frontend/src/services/atividadesApi.ts`, `bibliotecaApi.ts`

**Interfaces:** Classificação é carregada/salva em API separada; “Buscar na biblioteca” seleciona múltiplos modelos e importa; “Salvar no banco” coleta metadados do modelo; após import, recarregar questões.

- [ ] **Step 1: RED** — seleção múltipla, import único, reload, classificação sem alterar payload base e salvar-no-banco com IDs curriculares.
- [ ] **Step 2: Rodar RED** — `cd frontend && npm run test:run -- QuestaoLibraryPicker.test.tsx AtividadeEditor.test.tsx`.
- [ ] **Step 3: Implementar com extração** — Não inflar `AtividadeEditor`; classificação/picker são componentes próprios.
- [ ] **Step 4: GREEN/build**.
- [ ] **Step 5: Commit** — `git commit -m "feat: reuse curricular content in activity editor"`.

---

### Task 10: Frontend do professor — adaptações na atribuição

**Files:**
- Create: `frontend/src/services/acessibilidadeApi.ts`
- Create: `frontend/src/features/atividades/AdaptacaoAtribuicaoPanel.tsx`, `AdaptacaoAtribuicaoPanel.test.tsx`
- Modify: `frontend/src/features/atividades/AtribuirAtividadePanel.tsx`, `AtribuirAtividadePanel.test.tsx`

**Interfaces:** Form geral nullable (`null=herdar`); criar atribuição primeiro e depois PUT adaptação; personalização individual só para destino TURMA. Falha ao salvar adaptação após atribuição deve informar estado parcial e **não repetir POST de atribuição**.

- [ ] **Step 1: RED** — herança, geral após criação, individual só turma, aluno correto e falha parcial sem duplicar atribuição.
- [ ] **Step 2: Rodar RED** — `cd frontend && npm run test:run -- AdaptacaoAtribuicaoPanel.test.tsx AtribuirAtividadePanel.test.tsx`.
- [ ] **Step 3: Implementar** — Manter `CreateAtribuicaoRequest` enxuto; adaptação é segundo passo.
- [ ] **Step 4: GREEN/build**.
- [ ] **Step 5: Commit** — `git commit -m "feat: configure assignment accessibility"`.

---

### Task 11: Experiência adaptada do aluno e preferências persistentes

**Files:**
- Create: `frontend/src/features/aluno/AccessibilityToolbar.tsx`, `AccessibilityToolbar.test.tsx`, `MinhasPreferenciasPage.tsx`, `MinhasPreferenciasPage.test.tsx`
- Modify: `frontend/src/features/aluno/QuestaoCard.tsx`, `QuestaoCard.test.tsx`, `ResolverAtividadePage.tsx`, `ResolverAtividadePage.test.tsx`
- Modify: `frontend/src/services/alunoAtividadesApi.ts`, `acessibilidadeApi.ts`
- Modify: `frontend/src/App.tsx`, `frontend/src/features/auth/SessionPage.tsx`, `frontend/src/v05.css`

**Interfaces:**
- `AlunoAtividadeDetalhe` usa `acessibilidade: ConfiguracaoAcessibilidade`, não perfil bruto.
- `/app/minhas-preferencias` edita preferência global persistida.
- Solver carrega também a preferência atual para toolbar. Depois de `PUT` da preferência durante atividade, chama novamente `GET /aluno/atribuicoes/{id}/acessibilidade`; frontend não calcula o merge localmente.
- Toolbar só muda fonte/leitura/apoio visual. Recurso obrigatório explica “apoio definido para esta atividade”.

- [ ] **Step 1: RED toolbar/prefs** — aumentar fonte, mínimo não reduzido, recursos obrigatórios não desligam, PUT persiste, novo acesso carrega preferência.
- [ ] **Step 2: RED repetição/fallback** — `repeticaoGuiada=true` + erro mostra “Vamos por partes” e dica, sem criar mecanismo de IA; sem `window.speechSynthesis`, card continua renderizável/respondível; manter regressão de não vazamento de `respostaCorreta`.
- [ ] **Step 3: Rodar RED** — `cd frontend && npm run test:run -- AccessibilityToolbar.test.tsx MinhasPreferenciasPage.test.tsx QuestaoCard.test.tsx ResolverAtividadePage.test.tsx`.
- [ ] **Step 4: Implementar** — `QuestaoCard` usa config efetiva; speech apenas por ação do usuário; visual support por classes determinísticas; retry guiado usa dica já retornada pelo backend.
- [ ] **Step 5: GREEN/build**.
- [ ] **Step 6: Commit** — `git commit -m "feat: adapt student activity experience"`.

---

### Task 12: Segurança, versão, verificador, CI e aceite final

**Files:**
- Create: `backend/src/test/java/br/com/mathinclui/security/V05AuthorizationIT.java`
- Create: `scripts/verify_v05.py`
- Modify: `backend/pom.xml`
- Modify: `frontend/package.json`
- Modify: `backend/src/main/java/br/com/mathinclui/system/SystemInfoProvider.java`
- Modify: `README.md`
- Delete: `.github/workflows/v04-ci.yml`
- Create: `.github/workflows/v05-ci.yml`

**Release contract:** backend/frontend/system-info = `0.5.0`. CI roda em push `feature/v0.5-acessibilidade-biblioteca` e PR para `main`: backend tests, frontend tests/build, verificadores e Docker v0.5. Checkout usa `fetch-depth: 0` para permitir verificar V1–V8 contra `origin/main`.

- [ ] **Step 1: Preparar teste de segurança** — Adicionar dependência test `org.springframework.security:spring-security-test` no `pom.xml` e escrever `V05AuthorizationIT` com MockMvc/JWT. Cobrir ALUNO→biblioteca=403, PROFESSOR→preferências de aluno=403, cross-owner=404, aluno alheio=404 e conteúdo SISTEMA imutável.
- [ ] **Step 2: Rodar RED/GREEN** — `cd backend && mvn -Dtest=V05AuthorizationIT test`, corrigindo somente gaps v0.5; depois `mvn test`.
- [ ] **Step 3: `verify_v05.py`** — Verificar V9–V12, módulos, rotas frontend, `salvar-no-banco`, matcher `/biblioteca/**` e, com Git disponível, falhar se `git diff --name-only origin/main...HEAD` incluir qualquer arquivo `V1__` a `V8__`. CI deve fazer checkout completo para essa regra ser determinística.
- [ ] **Step 4: Versões/README** — `backend/pom.xml`, `frontend/package.json` e `SystemInfoProvider` → `0.5.0`; README documenta v0.5, V9–V12, rotas, testes, E2E e corrige v0.4 para concluída.
- [ ] **Step 5: CI** — Criar `v05-ci.yml` Java 21/Node 22/Python 3.13 e Docker `mathinclui-backend:v0.5`, `mathinclui-frontend:v0.5`; remover `v04-ci.yml` no mesmo commit para não duplicar CI de PR para main.
- [ ] **Step 6: Verificação completa**

```bash
cd backend && mvn test
cd ../frontend && npm install && npm run test:run && npm run build
cd ..
python scripts/verify_project.py
python scripts/verify_v03.py
python scripts/verify_v04.py
python scripts/verify_v05.py
docker build -t mathinclui-backend:v0.5 ./backend
docker build -t mathinclui-frontend:v0.5 ./frontend
```

Esperado: tudo PASS/exit 0. Se Docker/Testcontainers não estiver disponível localmente, não declarar release pronta; confirmar no CI.

- [ ] **Step 7: E2E** — Professor: login → biblioteca → filtra oficial → copia atividade → importa questão → edita/publica → atribui à turma → apoio geral → override individual. Aluno: login → config efetiva → aumenta recurso permitido → erra/recebe apoio guiado → conclui → logout/login → preferência permanece. Confirmar que fonte oficial/modelo não mudaram.
- [ ] **Step 8: Commit release**

```bash
git add backend frontend scripts README.md .github/workflows
git commit -m "chore: prepare MathInclui v0.5 release"
```

- [ ] **Step 9: Review pré-PR** — Usar `superpowers:requesting-code-review` (ou revisão equivalente) com foco nos cinco itens de Review Focus. Corrigir Important/Critical com TDD e repetir Step 6 antes de abrir PR.

---

## Ordem de execução e dependências

Tasks 1–3 são sequenciais (acessibilidade). Task 4 cria a base da biblioteca; Tasks 5–6 dependem dela; Task 7 depende dos contratos do banco estarem estáveis. Tasks 8–11 só começam depois dos respectivos contratos HTTP backend estarem verdes. Task 12 fecha a release.

O plano é único de propósito: acessibilidade e biblioteca são módulos distintos, mas convergem no editor, atribuição, experiência do aluno, sequência Flyway e E2E. Separar em branches independentes aumentaria integração/rebase sem benefício proporcional.

## Definição de pronto da v0.5

A branch só vai a PR quando critérios da spec estiverem cobertos por teste/E2E, backend não vazar gabarito, ownership cruzado estiver bloqueado, migrations rodarem do zero em PostgreSQL 17, regressões v0.4 estiverem verdes, frontend buildar, verificadores passarem, Docker construir e BNCC/biblioteca oficial estiverem ancoradas na fonte MEC registrada no repositório.