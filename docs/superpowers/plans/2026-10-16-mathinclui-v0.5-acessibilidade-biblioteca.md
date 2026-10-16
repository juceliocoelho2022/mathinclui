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
- `null` em overrides significa herança; `false` só é permitido como valor explícito quando não reduzir um apoio já exigido pela composição.
- Preferências do aluno persistem somente para fonte, leitura em voz e apoio visual; elas podem ativar/aumentar recursos opcionais, mas nunca desligar/reduzir mínimos pedagógicos.
- Adaptação individual por aluno só existe em atribuição destinada a **turma**. Atribuição direta a aluno deve rejeitar override individual com `409 CONFLICT`.
- Para override individual em atribuição de turma, o aluno precisa estar ativo e pertencer àquela turma; caso contrário, responder `404`/`403` conforme o padrão de ownership já usado no projeto.
- Atividades v0.4 sem BNCC, tags ou overrides continuam válidas e funcionais.
- Conteúdo `SISTEMA` é somente leitura para professor. Conteúdo pessoal de outro professor não é visível nem copiável.
- Cópias são independentes da origem. Proveniência é informativa: nenhuma FK de origem pode bloquear exclusão legítima da fonte ou apagar a cópia; usar FK nullable com `ON DELETE SET NULL` quando houver vínculo.
- Cópia de atividade, cópia de modelo e importação de múltiplas questões são transacionais.
- Ao importar questões em atividade, anexar após a maior `ordem` existente e preservar a ordem da seleção recebida. IDs de modelo repetidos no mesmo payload são inválidos (`400`) para evitar duplicação acidental.
- A alteração de classificação curricular/tags de atividade com histórico de aluno é tratada como alteração histórica e deve ser bloqueada pelo mesmo `validarSemHistorico` da v0.4.
- O aluno continua sem receber gabarito, alternativa correta ou explicação antes das condições já definidas na v0.4.
- Leitura em voz usa `window.speechSynthesis` quando disponível, sem autoplay e sem dependência externa; ausência do recurso não pode quebrar a atividade.
- A primeira biblioteca oficial deve ser pequena e representativa dos 6º, 7º e 8º anos, não uma cobertura curricular completa.
- Sem IA generativa, busca semântica, Elasticsearch, ranking, perfil `ADMIN`, sincronização automática de cópias, workflow colaborativo de publicação ou dados clínicos.

## Review Focus

- **Compatibilidade de enums persistidos:** nenhuma migration/DTO deve introduzir `NORMAL`, `BAIXO`, `MEDIO` ou `ALTO` como literais de banco; testes de migration e merge nas Tasks 1–3.
- **Escopo do override individual:** deve existir apenas para atribuições de turma e somente para aluno pertencente à turma atribuída; testes na Task 3.
- **Independência e atomicidade das cópias:** excluir/editar a fonte não altera cópia; falha no meio da cópia/importação não deixa dados parciais; proveniência nunca gera cascade destrutivo; testes nas Tasks 4–6.
- **Segurança e não vazamento:** outro professor não vê conteúdo pessoal; aluno não acessa biblioteca nem dados de outro aluno; DTO do aluno não ganha gabarito por causa da v0.5; testes nas Tasks 3, 5, 6 e 11.
- **Fallback de acessibilidade no frontend:** preferência não reduz mínimo e `speechSynthesis` ausente não causa erro nem bloqueia resposta; testes nas Tasks 10–11.

## Preflight antes da implementação

Executar na branch `feature/v0.5-acessibilidade-biblioteca` antes da Task 1:

```bash
cd backend && mvn test
cd ../frontend && npm install && npm run test:run && npm run build
cd .. && python scripts/verify_project.py && python scripts/verify_v03.py && python scripts/verify_v04.py
```

Esperado: baseline verde. Se falhar sem mudanças v0.5, parar e diagnosticar antes de iniciar TDD da nova versão.

---

### Task 1: Schema V9 de acessibilidade e contrato de migration

**Files:**
- Create: `backend/src/main/resources/db/migration/V9__create_accessibility_v05.sql`
- Create: `backend/src/test/java/br/com/mathinclui/migration/V9AccessibilityMigrationIT.java`
- Modify: `backend/src/test/java/br/com/mathinclui/migration/V7MigrationIT.java`

**Interfaces:**
- Table `preferencias_interface_aluno(aluno_id PK/FK, tamanho_fonte, leitura_voz, apoio_visual, atualizado_em)`.
- Table `adaptacoes_atribuicao(atribuicao_id PK/FK, tamanho_fonte_minima, leitura_voz_minima, apoio_visual_minimo, dicas_passo_a_passo, repeticao_guiada, questoes_por_tela, tempo_ampliado, nivel_apoio)`.
- Table `adaptacoes_aluno_atribuicao(id PK, atribuicao_id, aluno_id, mesmos campos nullable, UNIQUE(atribuicao_id, aluno_id))`.
- Checks de enum devem aceitar somente os literais já persistidos `PADRAO/GRANDE/EXTRA_GRANDE` e `PADRAO/MODERADO/AMPLIADO`.
- `questoes_por_tela`, quando presente, deve ficar entre 1 e 10.
- Preferência do aluno tem defaults `PADRAO/FALSE/FALSE`; overrides usam `NULL` para herança.

- [ ] **Step 1: Escrever o teste de migration em RED**

Criar `V9AccessibilityMigrationIT` estendendo `PostgresIntegrationTest`. Cobrir: Flyway chega pelo menos à V9, criação das três tabelas, FK de aluno/atribuição, unicidade `(atribuicao_id, aluno_id)`, rejeição de `questoes_por_tela=0`, rejeição de enum inexistente e aceitação dos literais atuais.

Também remover de `V7MigrationIT` a asserção frágil de que a versão corrente é exatamente `8`; manter os testes de regras V7/V8. O teste de versão final passa a pertencer à migration mais recente da release.

- [ ] **Step 2: Rodar e confirmar RED**

Run: `cd backend && mvn -Dtest=V9AccessibilityMigrationIT,V7MigrationIT test`

Expected: FAIL porque `V9`/tabelas ainda não existem.

- [ ] **Step 3: Implementar V9**

Criar a migration apenas com `CREATE TABLE/INDEX`; não editar V1. Usar `ON DELETE CASCADE` para preferências e adaptações ligadas a aluno/atribuição, porque esses registros não têm valor histórico independente do dono.

- [ ] **Step 4: Rodar GREEN + regressão de migrations**

Run: `cd backend && mvn -Dtest=V9AccessibilityMigrationIT,V7MigrationIT test`
Expected: PASS.

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
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/repository/PreferenciaInterfaceAlunoRepository.java`
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/repository/AdaptacaoAtribuicaoRepository.java`
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/repository/AdaptacaoAlunoAtribuicaoRepository.java`
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/PreferenciaInterfaceRequest.java`
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/PreferenciaInterfaceResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/ConfiguracaoAcessibilidadeResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/PreferenciaInterfaceService.java`
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/AcessibilidadeEfetivaService.java`
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/AlunoAcessibilidadeController.java`
- Create: `backend/src/test/java/br/com/mathinclui/acessibilidade/AcessibilidadeEfetivaServiceTest.java`
- Create: `backend/src/test/java/br/com/mathinclui/acessibilidade/PreferenciaInterfaceServiceTest.java`

**Interfaces:**

```java
ConfiguracaoAcessibilidadeEfetiva calcular(Long alunoId, Long atribuicaoId)
PreferenciaInterfaceResponse buscar(Long alunoId)
PreferenciaInterfaceResponse atualizar(Long alunoId, PreferenciaInterfaceRequest request)
```

Student HTTP:
- `GET /api/v1/aluno/atribuicoes/{id}/acessibilidade`
- `GET /api/v1/aluno/preferencias-interface`
- `PUT /api/v1/aluno/preferencias-interface`

`ConfiguracaoAcessibilidadeResponse` expõe exatamente: `tamanhoFonte`, `leituraVoz`, `apoioVisual`, `dicasPassoAPasso`, `repeticaoGuiada`, `questoesPorTela`, `tempoAmpliado`, `nivelApoio`.

- [ ] **Step 1: Escrever testes RED do merge**

Cobrir em `AcessibilidadeEfetivaServiceTest`: perfil sem overrides; fonte `GRANDE` + preferência `PADRAO` resulta `GRANDE`; fonte `GRANDE` + preferência `EXTRA_GRANDE` resulta `EXTRA_GRANDE`; boolean obrigatório não pode ser desligado; preferência pode ligar recurso opcional; `questoesPorTela` usa o menor valor pedagógico; `nivelApoio` usa maior nível; `null` herda; atribuição não destinada ao aluno retorna 404.

- [ ] **Step 2: Confirmar RED**

Run: `cd backend && mvn -Dtest=AcessibilidadeEfetivaServiceTest,PreferenciaInterfaceServiceTest test`
Expected: FAIL por classes/serviços ausentes.

- [ ] **Step 3: Implementar entidades/repositórios e composição**

Reusar `TamanhoFonte` e `NivelApoio` existentes. Para comparar níveis, implementar funções explícitas no serviço ou métodos auxiliares sem depender de `ordinal()` persistido como regra implícita. O serviço carrega aluno, atribuição, perfil, override geral, override individual e preferência; valida `atribuicao.destinadaAo(alunoId, turmaId)` antes do merge.

- [ ] **Step 4: Implementar preferência do aluno e controller**

`PreferenciaInterfaceService.buscar` devolve defaults mesmo sem linha persistida. `atualizar` cria/upsert somente a linha do aluno autenticado. O controller extrai `ALUNO:<id>` do JWT seguindo o padrão de `AlunoAtividadeController`; nunca aceita `alunoId` no payload.

- [ ] **Step 5: Rodar GREEN**

Run: `cd backend && mvn -Dtest=AcessibilidadeEfetivaServiceTest,PreferenciaInterfaceServiceTest test`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/br/com/mathinclui/acessibilidade backend/src/test/java/br/com/mathinclui/acessibilidade
git commit -m "feat: calculate effective student accessibility"
```

---

### Task 3: APIs do professor para adaptações e integração com o detalhe do aluno

**Files:**
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/AdaptacaoRequest.java`
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/AdaptacaoResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/AdaptacaoAtribuicaoService.java`
- Create: `backend/src/main/java/br/com/mathinclui/acessibilidade/AdaptacaoAtribuicaoController.java`
- Modify: `backend/src/main/java/br/com/mathinclui/tentativa/AlunoAtividadeDetalheResponse.java`
- Modify: `backend/src/main/java/br/com/mathinclui/tentativa/AlunoAtividadeService.java`
- Create: `backend/src/test/java/br/com/mathinclui/acessibilidade/AdaptacaoAtribuicaoServiceTest.java`
- Create: `backend/src/test/java/br/com/mathinclui/tentativa/AlunoAtividadeAccessibilityTest.java`

**Interfaces:**

Professor HTTP:
- `GET /api/v1/atribuicoes/{id}/adaptacao`
- `PUT /api/v1/atribuicoes/{id}/adaptacao`
- `GET /api/v1/atribuicoes/{id}/alunos/{alunoId}/adaptacao`
- `PUT /api/v1/atribuicoes/{id}/alunos/{alunoId}/adaptacao`
- `DELETE /api/v1/atribuicoes/{id}/alunos/{alunoId}/adaptacao`

Service:
```java
AdaptacaoResponse buscarGeral(Long professorId, Long atribuicaoId)
AdaptacaoResponse salvarGeral(Long professorId, Long atribuicaoId, AdaptacaoRequest request)
AdaptacaoResponse buscarIndividual(Long professorId, Long atribuicaoId, Long alunoId)
AdaptacaoResponse salvarIndividual(Long professorId, Long atribuicaoId, Long alunoId, AdaptacaoRequest request)
void excluirIndividual(Long professorId, Long atribuicaoId, Long alunoId)
```

`AlunoAtividadeDetalheResponse` passa a expor `ConfiguracaoAcessibilidadeResponse acessibilidade` no lugar de `PerfilPedagogicoResponse perfilPedagogico`. A rota separada de acessibilidade continua existindo, mas a tela de resolução não precisa fazer chamada adicional.

- [ ] **Step 1: Escrever testes RED de ownership e escopo**

Cobrir: professor B não altera atribuição de A; override individual em atribuição direta gera 409; aluno individual precisa pertencer à turma da atribuição; override geral aceita herança por null; override individual pode aumentar apoio, mas a composição final nunca reduz o perfil.

- [ ] **Step 2: Confirmar RED**

Run: `cd backend && mvn -Dtest=AdaptacaoAtribuicaoServiceTest,AlunoAtividadeAccessibilityTest test`
Expected: FAIL.

- [ ] **Step 3: Implementar service/controller**

Usar `AtribuicaoService.obterDoProfessor` como boundary de ownership. Em override individual: exigir `atribuicao.getTurmaId()!=null`, carregar aluno via `AlunoService.obterDoProfessor`, comparar `aluno.getTurmaId()` com a turma da atribuição e só então gravar.

- [ ] **Step 4: Integrar configuração efetiva no detalhe do aluno**

Remover a montagem direta de `PerfilPedagogicoResponse` em `AlunoAtividadeService`. Injetar `AcessibilidadeEfetivaService` e devolver o DTO efetivo. Preservar exatamente os campos de questão v0.4 e não adicionar resposta correta/flag `correta` de alternativa.

- [ ] **Step 5: GREEN + regressão do fluxo do aluno**

Run: `cd backend && mvn -Dtest=AdaptacaoAtribuicaoServiceTest,AlunoAtividadeAccessibilityTest,TentativaServiceTest test`
Expected: PASS.

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
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/domain/HabilidadeBncc.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/domain/TagProfessor.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/domain/TipoOrigemBiblioteca.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/domain/QuestaoModelo.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/domain/AlternativaModelo.java`
- Create repositories under: `backend/src/main/java/br/com/mathinclui/biblioteca/repository/`

**Interfaces/schema:**
- `habilidades_bncc(id, codigo UNIQUE, ano_escolar, unidade_tematica, objeto_conhecimento, descricao)`.
- `atividade_habilidade_bncc(atividade_id, habilidade_id, PK composta)`.
- `tags_professor(id, professor_id, nome, nome_normalizado, UNIQUE(professor_id,nome_normalizado))`.
- `atividade_tag(atividade_id, tag_id)` and `questao_modelo_tag(questao_modelo_id, tag_id)`.
- `questoes_modelo` + `alternativas_modelo`, com origem `SISTEMA|PROFESSOR`, ownership coerente, tipos da v0.4, dicas, explicação, imagem, limite, ano, conteúdo e dificuldade.
- `questao_modelo_habilidade_bncc` N:N.
- `atividades.origem_atividade_id` nullable self-FK `ON DELETE SET NULL` + `copiado_em`.
- `questoes.origem_questao_modelo_id` nullable FK para `questoes_modelo` `ON DELETE SET NULL` + `copiado_em`.
- `questoes_modelo.origem_modelo_id` nullable self-FK `ON DELETE SET NULL` + `copiado_em`.

- [ ] **Step 1: Escrever V10MigrationIT em RED**

Cobrir: tabelas/índices; constraints de origem/professor; no máximo uma alternativa correta por modelo; resposta correta obrigatória para NUMERICA/TEXTO_CURTO; unicidade de tag normalizada por professor; duas tags iguais podem existir para professores diferentes; apagar origem deve setar proveniência para null sem apagar cópia; relações N:N rejeitam IDs inexistentes.

- [ ] **Step 2: Confirmar RED**

Run: `cd backend && mvn -Dtest=V10LibraryMigrationIT test`
Expected: FAIL.

- [ ] **Step 3: Implementar V10**

Criar primeiro tabelas independentes, depois tabelas de relação e por último `ALTER TABLE` de `atividades/questoes` para proveniência. Adicionar índices para filtros: `(ano_escolar, unidade_tematica)`, `codigo`, `tipo_origem/professor_id`, `conteudo`, `dificuldade` e FKs de relações.

- [ ] **Step 4: Mapear JPA mínimo**

Mapear entidades/repositories sem criar herança JPA complexa. `Atividade` ganha getters/factory para proveniência; `Questao` ganha `origemQuestaoModeloId/copiadoEm` e método/factory de cópia, preservando o construtor atual.

- [ ] **Step 5: GREEN**

Run: `cd backend && mvn -Dtest=V10LibraryMigrationIT test`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/resources/db/migration/V10__create_pedagogical_library_v05.sql backend/src/main/java/br/com/mathinclui/biblioteca backend/src/main/java/br/com/mathinclui/atividade/domain/Atividade.java backend/src/main/java/br/com/mathinclui/questao/domain/Questao.java backend/src/test/java/br/com/mathinclui/migration/V10LibraryMigrationIT.java
git commit -m "feat: add pedagogical library schema"
```

---

### Task 5: Biblioteca de atividades, classificação curricular, tags e busca

**Files:**
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/PageResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/BibliotecaAtividadeResumoResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/BibliotecaAtividadeDetalheResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/HabilidadeBnccResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/TagResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/ClassificacaoAtividadeRequest.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/ClassificacaoAtividadeResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/BibliotecaAtividadeService.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/ClassificacaoAtividadeService.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/TagService.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/BnccService.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/BibliotecaController.java`
- Modify: `backend/src/main/java/br/com/mathinclui/auth/security/SecurityConfig.java`
- Create tests: `BibliotecaAtividadeServiceTest.java`, `ClassificacaoAtividadeServiceTest.java`, `TagServiceTest.java`

**Interfaces:**
- `GET /api/v1/biblioteca/atividades?origem=&ano=&unidadeTematica=&objetoConhecimento=&habilidade=&conteudo=&dificuldade=&tag=&busca=&page=0&size=20`
- `GET /api/v1/biblioteca/atividades/{id}`
- `POST /api/v1/biblioteca/atividades/{id}/copiar`
- `GET /api/v1/biblioteca/bncc?ano=6|7|8`
- `GET /api/v1/biblioteca/tags`
- `POST /api/v1/biblioteca/tags`
- `DELETE /api/v1/biblioteca/tags/{id}`
- `GET /api/v1/atividades/{id}/classificacao`
- `PUT /api/v1/atividades/{id}/classificacao` payload `{habilidadeIds: number[], tagIds: number[]}`.

Paginação: `page>=0`, `1<=size<=100`, default 20. Busca textual usa PostgreSQL `ILIKE` sobre título/descrição/conteúdo e joins curriculares, sem motor externo.

Normalização de tag: Unicode NFC + `trim` + colapso de espaços + lowercase `Locale.ROOT`, preservando acentos; gravar `nome` para exibição e `nome_normalizado` para unicidade.

- [ ] **Step 1: Testes RED de visibilidade, tags e deep copy de atividade**

Cobrir: lista inclui `SISTEMA` publicado + atividades do professor; nunca atividade pessoal de outro professor; copiar `SISTEMA` cria atividade `PROFESSOR`, `publicada=false`, com questões/alternativas e relações BNCC copiadas; edição da cópia não modifica fonte; tags do professor A não aparecem para B; classificação com histórico retorna 409.

- [ ] **Step 2: Confirmar RED**

Run: `cd backend && mvn -Dtest=BibliotecaAtividadeServiceTest,ClassificacaoAtividadeServiceTest,TagServiceTest test`
Expected: FAIL.

- [ ] **Step 3: Implementar busca e DTOs**

Preferir repository queries/specifications simples e paginação determinística (`titulo,id`) a lógica em memória. Para atividade pessoal, filtrar `professor_id = autenticado`; para sistema, `tipo_origem=SISTEMA AND publicada=true`.

- [ ] **Step 4: Implementar deep copy transacional**

Usar `@Transactional`. Copiar atividade base, habilidades, tags aplicáveis, questões e alternativas. Preservar ordem e limites. Proveniência aponta para a atividade fonte imediata; a nova atividade é rascunho. Não chamar APIs HTTP internamente.

- [ ] **Step 5: Implementar classificação/tags e segurança de rota**

Adicionar `/api/v1/biblioteca/**` ao matcher de `ROLE_PROFESSOR` em `SecurityConfig`. `PUT classificacao` valida ownership e `validarSemHistorico`. IDs de tags devem pertencer ao professor; habilidades são globais.

- [ ] **Step 6: GREEN**

Run: `cd backend && mvn -Dtest=BibliotecaAtividadeServiceTest,ClassificacaoAtividadeServiceTest,TagServiceTest test`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/br/com/mathinclui/biblioteca backend/src/main/java/br/com/mathinclui/auth/security/SecurityConfig.java backend/src/test/java/br/com/mathinclui/biblioteca
git commit -m "feat: add searchable activity library"
```

---

### Task 6: Banco de questões, salvar modelo, copiar modelo e importar em atividade

**Files:**
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/QuestaoModeloResponse.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/SalvarQuestaoNoBancoRequest.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/ImportarQuestoesRequest.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/QuestaoModeloService.java`
- Create: `backend/src/main/java/br/com/mathinclui/biblioteca/QuestaoBibliotecaController.java`
- Modify: `backend/src/main/java/br/com/mathinclui/questao/QuestaoController.java`
- Create: `backend/src/test/java/br/com/mathinclui/biblioteca/QuestaoModeloServiceTest.java`
- Create: `backend/src/test/java/br/com/mathinclui/biblioteca/BibliotecaTransactionIT.java`
- Create: `backend/src/test/java/br/com/mathinclui/support/SpringPostgresIntegrationTest.java`

**Interfaces:**
- `GET /api/v1/biblioteca/questoes?...` com filtros equivalentes e paginação.
- `GET /api/v1/biblioteca/questoes/{id}`.
- `POST /api/v1/biblioteca/questoes/{id}/copiar` cria novo `QuestaoModelo` pessoal independente.
- `POST /api/v1/atividades/{atividadeId}/questoes/{questaoId}/salvar-no-banco` payload com `anoEscolar`, `conteudo`, `dificuldade`, `habilidadeIds`, `tagIds`.
- `POST /api/v1/atividades/{atividadeId}/questoes/importar` payload `{questoesModeloIds:[...]}`; retorna as novas questões concretas.

- [ ] **Step 1: Escrever testes RED de modelo e importação**

Cobrir: salvar uma questão concreta cria modelo independente; múltipla escolha copia alternativas/gabarito apenas na área de professor; copiar modelo de sistema cria modelo pessoal; outro professor não copia modelo pessoal; importação apenda após `max(ordem)`; payload com ID repetido retorna 400; target com histórico retorna 409; alterar modelo não altera questão já importada.

- [ ] **Step 2: Confirmar RED**

Run: `cd backend && mvn -Dtest=QuestaoModeloServiceTest test`
Expected: FAIL.

- [ ] **Step 3: Implementar `QuestaoModeloService`**

Salvar no banco é cópia não destrutiva e pode ocorrer mesmo se a atividade fonte já tiver histórico. Importar para atividade é mutação da atividade e deve exigir `obterEditavelDoProfessor + validarSemHistorico`. Em múltipla escolha, copiar alternativas com exatamente uma correta; em numérica/texto, copiar `respostaCorreta`.

- [ ] **Step 4: Testar rollback transacional com PostgreSQL real**

Criar `SpringPostgresIntegrationTest` com `@SpringBootTest`, PostgreSQL 17 Testcontainers, propriedades dinâmicas de datasource e JWT secret de fixture. Em `BibliotecaTransactionIT`, provocar falha controlada no meio de importação/cópia (por constraint na segunda questão/alternativa) e provar que contagens antes/depois são idênticas.

- [ ] **Step 5: GREEN completo da Task**

Run: `cd backend && mvn -Dtest=QuestaoModeloServiceTest,BibliotecaTransactionIT test`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/br/com/mathinclui/biblioteca backend/src/main/java/br/com/mathinclui/questao/QuestaoController.java backend/src/test/java/br/com/mathinclui/biblioteca backend/src/test/java/br/com/mathinclui/support/SpringPostgresIntegrationTest.java
git commit -m "feat: add reusable question bank"
```

---

### Task 7: Fonte BNCC validada, seed V11 e biblioteca oficial V12

**Files:**
- Create: `docs/references/bncc-matematica-6-8.md`
- Create: `backend/src/main/resources/db/migration/V11__seed_bncc_math_6_8.sql`
- Create: `backend/src/main/resources/db/migration/V12__seed_official_library_v05.sql`
- Create: `backend/src/test/java/br/com/mathinclui/migration/V12OfficialLibraryIT.java`

**Interfaces/data contract:**
- Fonte oficial primária: BNCC Ensino Infantil/Fundamental versão final do MEC: `https://basenacionalcomum.mec.gov.br/images/BNCC_EI_EF_110518_versaofinal_site.pdf`.
- Verificar também erratas oficiais antes de transcrever objetos/habilidades: `https://basenacionalcomum.mec.gov.br/images/implementacao/ERRATAS-BNCC_site.pdf`.
- `docs/references/bncc-matematica-6-8.md` registra URL, data de acesso, páginas/trechos usados, códigos, unidade temática, objeto de conhecimento e descrição exatamente validados.
- V11 semeia somente habilidades utilizadas pela biblioteca oficial inicial, com pelo menos duas habilidades por ano para provar filtros, sem pretensão de catálogo completo.
- V12 cria no mínimo duas atividades oficiais por ano (6 atividades), cobrindo no conjunto `MULTIPLA_ESCOLHA`, `NUMERICA`, `TEXTO_CURTO`, mais de uma dificuldade, dicas e explicações.

- [ ] **Step 1: Criar o documento de validação BNCC antes do SQL**

Conferir cada código/descrição na fonte MEC. Não escrever seed a partir de memória. Registrar qualquer errata que altere o texto selecionado.

- [ ] **Step 2: Escrever `V12OfficialLibraryIT` em RED**

Testar Flyway corrente `12`, existência de registros BNCC 6/7/8, atividades `SISTEMA` publicadas com `professor_id IS NULL`, questões dos três tipos, múltipla escolha com uma correta, associações BNCC válidas e ausência de códigos placeholder.

- [ ] **Step 3: Confirmar RED**

Run: `cd backend && mvn -Dtest=V12OfficialLibraryIT test`
Expected: FAIL porque V11/V12 não existem.

- [ ] **Step 4: Implementar V11 e V12 de forma determinística**

Usar chaves naturais/códigos controlados e subqueries por `codigo`/título onde necessário; evitar depender de IDs serial fixos existentes. Seeds devem ser reaplicáveis apenas pelo mecanismo normal do Flyway, sem `DO UPDATE` que esconda inconsistências.

- [ ] **Step 5: GREEN e suite de migrations**

Run: `cd backend && mvn -Dtest=V9AccessibilityMigrationIT,V10LibraryMigrationIT,V12OfficialLibraryIT,V7MigrationIT test`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add docs/references/bncc-matematica-6-8.md backend/src/main/resources/db/migration/V11__seed_bncc_math_6_8.sql backend/src/main/resources/db/migration/V12__seed_official_library_v05.sql backend/src/test/java/br/com/mathinclui/migration/V12OfficialLibraryIT.java
git commit -m "feat: seed validated BNCC library content"
```

---

### Task 8: Frontend do professor — página Biblioteca

**Files:**
- Create: `frontend/src/services/bibliotecaApi.ts`
- Create: `frontend/src/features/biblioteca/BibliotecaPage.tsx`
- Create: `frontend/src/features/biblioteca/BibliotecaFilters.tsx`
- Create: `frontend/src/features/biblioteca/AtividadeBibliotecaCard.tsx`
- Create: `frontend/src/features/biblioteca/BibliotecaPage.test.tsx`
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/features/auth/SessionPage.tsx`
- Create: `frontend/src/v05.css`

**Interfaces:**
- TypeScript espelha `PageResponse`, resumo/detalhe de atividade, BNCC e tags.
- Rota: `/app/biblioteca` somente para sessão de professor no frontend; backend continua sendo autoridade real.
- Views: `Atividades`, `Questões`, `Minha biblioteca`; filtros são state controlado e viram query params da API.
- Ação `Usar atividade` chama copy, bloqueia duplo clique enquanto pendente e navega/encaminha para edição da nova atividade.

- [ ] **Step 1: Escrever teste RED da página**

Testar autenticação professor, renderização dos filtros, alteração de ano/origem gerando nova consulta, estado vazio/erro, preview básico, copiar atividade uma única vez apesar de clique repetido e ausência de ação Editar em item `SISTEMA`.

- [ ] **Step 2: Confirmar RED**

Run: `cd frontend && npm run test:run -- BibliotecaPage.test.tsx`
Expected: FAIL.

- [ ] **Step 3: Implementar API/componentes e rota**

Não colocar toda UI em `BibliotecaPage`; manter filtros/card separados. Importar `v05.css` em `App.tsx`. Adicionar card “Biblioteca pedagógica” em `SessionPage` do professor.

- [ ] **Step 4: GREEN + build**

Run: `cd frontend && npm run test:run -- BibliotecaPage.test.tsx && npm run build`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/services/bibliotecaApi.ts frontend/src/features/biblioteca frontend/src/App.tsx frontend/src/features/auth/SessionPage.tsx frontend/src/v05.css
git commit -m "feat: add professor pedagogical library UI"
```

---

### Task 9: Editor de atividade — BNCC, tags e reutilização de questões

**Files:**
- Create: `frontend/src/features/atividades/ClassificacaoCurricularFields.tsx`
- Create: `frontend/src/features/atividades/QuestaoLibraryPicker.tsx`
- Create: `frontend/src/features/atividades/QuestaoLibraryPicker.test.tsx`
- Modify: `frontend/src/features/atividades/AtividadeEditor.tsx`
- Modify: `frontend/src/features/atividades/AtividadeEditor.test.tsx`
- Modify: `frontend/src/services/atividadesApi.ts`
- Modify: `frontend/src/services/bibliotecaApi.ts`

**Interfaces:**
- Ao editar atividade existente, carregar/salvar `habilidadeIds` e `tagIds` em chamada separada de classificação.
- “Buscar na biblioteca” abre picker paginado de `QuestaoModelo`, permite múltipla seleção e chama `/questoes/importar`.
- “Salvar no banco” aparece em questão concreta do professor e abre metadados mínimos do modelo (ano, conteúdo, dificuldade, habilidades, tags) antes de chamar `/salvar-no-banco`.
- Após importar, recarregar `listarQuestoes` para refletir ordens reais retornadas pelo backend.

- [ ] **Step 1: Escrever RED para picker e editor**

Cobrir seleção múltipla, importação única, reload de questões, classificação curricular salva sem alterar payload base da atividade, e “Salvar no banco” enviando IDs curriculares escolhidos.

- [ ] **Step 2: Confirmar RED**

Run: `cd frontend && npm run test:run -- QuestaoLibraryPicker.test.tsx AtividadeEditor.test.tsx`
Expected: FAIL.

- [ ] **Step 3: Extrair componentes e integrar**

Evitar crescer ainda mais `AtividadeEditor.tsx`: classificação e picker ficam em componentes próprios. Manter CRUD v0.4 intacto.

- [ ] **Step 4: GREEN + build**

Run: `cd frontend && npm run test:run -- QuestaoLibraryPicker.test.tsx AtividadeEditor.test.tsx && npm run build`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/features/atividades frontend/src/services/atividadesApi.ts frontend/src/services/bibliotecaApi.ts
git commit -m "feat: reuse curricular content in activity editor"
```

---

### Task 10: Frontend do professor — adaptações na atribuição

**Files:**
- Create: `frontend/src/services/acessibilidadeApi.ts`
- Create: `frontend/src/features/atividades/AdaptacaoAtribuicaoPanel.tsx`
- Create: `frontend/src/features/atividades/AdaptacaoAtribuicaoPanel.test.tsx`
- Modify: `frontend/src/features/atividades/AtribuirAtividadePanel.tsx`
- Modify: `frontend/src/features/atividades/AtribuirAtividadePanel.test.tsx`

**Interfaces:**
- Form geral trabalha com valores opcionais (`null = herdar`) para `tamanhoFonteMinima`, `leituraVozMinima`, `apoioVisualMinimo`, `dicasPassoAPasso`, `repeticaoGuiada`, `questoesPorTela`, `tempoAmpliado`, `nivelApoio`.
- Fluxo: criar atribuição primeiro; depois, se houver configuração geral, `PUT /atribuicoes/{id}/adaptacao`.
- Para destino `TURMA`, permitir abrir “Personalizações individuais”, listar alunos da turma e salvar override individual. Para destino `ALUNO`, esconder/desabilitar essa seção, coerente com backend.
- Se salvar adaptação falhar após criação da atribuição, mostrar mensagem explícita de que a atribuição foi criada, mas a adaptação não foi salva; não repetir automaticamente a criação da atribuição.

- [ ] **Step 1: Escrever testes RED**

Cobrir defaults herdados, envio de geral após criação, seção individual apenas para turma, aluno correto no endpoint e tratamento de falha parcial sem criar atribuição novamente.

- [ ] **Step 2: Confirmar RED**

Run: `cd frontend && npm run test:run -- AdaptacaoAtribuicaoPanel.test.tsx AtribuirAtividadePanel.test.tsx`
Expected: FAIL.

- [ ] **Step 3: Implementar painel e integração**

Manter a criação da atribuição como contrato atual; não ampliar `CreateAtribuicaoRequest` com dezenas de campos. Separar o segundo passo de adaptação para reduzir acoplamento e manter compatibilidade.

- [ ] **Step 4: GREEN + build**

Run: `cd frontend && npm run test:run -- AdaptacaoAtribuicaoPanel.test.tsx AtribuirAtividadePanel.test.tsx && npm run build`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/services/acessibilidadeApi.ts frontend/src/features/atividades
git commit -m "feat: configure assignment accessibility"
```

---

### Task 11: Experiência adaptada do aluno e preferências persistentes

**Files:**
- Create: `frontend/src/features/aluno/AccessibilityToolbar.tsx`
- Create: `frontend/src/features/aluno/AccessibilityToolbar.test.tsx`
- Create: `frontend/src/features/aluno/MinhasPreferenciasPage.tsx`
- Create: `frontend/src/features/aluno/MinhasPreferenciasPage.test.tsx`
- Modify: `frontend/src/features/aluno/QuestaoCard.tsx`
- Modify: `frontend/src/features/aluno/QuestaoCard.test.tsx`
- Modify: `frontend/src/features/aluno/ResolverAtividadePage.tsx`
- Modify: `frontend/src/features/aluno/ResolverAtividadePage.test.tsx`
- Modify: `frontend/src/services/alunoAtividadesApi.ts`
- Modify: `frontend/src/services/acessibilidadeApi.ts`
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/features/auth/SessionPage.tsx`
- Modify: `frontend/src/v05.css`

**Interfaces:**
- `AlunoAtividadeDetalhe` passa a ter `acessibilidade: ConfiguracaoAcessibilidade` e deixa de usar `perfilPedagogico` para renderização.
- Rota: `/app/minhas-preferencias`.
- `AccessibilityToolbar` recebe configuração efetiva + preferência atual e só oferece mudanças de interface. UI jamais altera tentativa, dicas, repetição, questões por tela, tempo ou nível de apoio.
- Se `leituraVoz` é obrigatória pelo efetivo, controle explica “apoio definido para esta atividade”; preferência `false` não a desliga na tela corrente.

- [ ] **Step 1: Escrever RED de toolbar/preferências**

Cobrir: aluno pode aumentar fonte; tentativa de escolher menor nível não reduz classe efetiva; leitura/apoio obrigatórios não são desligados; alteração persiste via API; página recarregada usa preferência salva.

- [ ] **Step 2: Adicionar regressão de `speechSynthesis` ausente e gabarito**

Em `QuestaoCard.test.tsx`, simular ambiente sem `window.speechSynthesis` e garantir que render/resposta continuam funcionais. Manter a asserção de que questão recebida pelo aluno não contém `respostaCorreta` antes do feedback final.

- [ ] **Step 3: Confirmar RED**

Run: `cd frontend && npm run test:run -- AccessibilityToolbar.test.tsx MinhasPreferenciasPage.test.tsx QuestaoCard.test.tsx ResolverAtividadePage.test.tsx`
Expected: FAIL.

- [ ] **Step 4: Implementar experiência efetiva**

`ResolverAtividadePage` usa `atividade.acessibilidade`; `QuestaoCard` recebe esse DTO. Speech só é executada quando o usuário aciona “Ouvir”. Apoio visual aplica classes determinísticas. Dicas/repetição seguem o resultado do backend; não gerar passos localmente por IA.

- [ ] **Step 5: GREEN + build**

Run: `cd frontend && npm run test:run -- AccessibilityToolbar.test.tsx MinhasPreferenciasPage.test.tsx QuestaoCard.test.tsx ResolverAtividadePage.test.tsx && npm run build`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/features/aluno frontend/src/services/alunoAtividadesApi.ts frontend/src/services/acessibilidadeApi.ts frontend/src/App.tsx frontend/src/features/auth/SessionPage.tsx frontend/src/v05.css
git commit -m "feat: adapt student activity experience"
```

---

### Task 12: Segurança de contrato, versão, verificador estrutural, CI e aceite final

**Files:**
- Create: `backend/src/test/java/br/com/mathinclui/security/V05AuthorizationIT.java`
- Create: `scripts/verify_v05.py`
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/java/br/com/mathinclui/system/SystemInfoProvider.java`
- Modify: `README.md`
- Delete/replace: `.github/workflows/v04-ci.yml`
- Create: `.github/workflows/v05-ci.yml`

**Interfaces/release contract:**
- Backend/app version `0.5.0`.
- CI v0.5 em push da branch `feature/v0.5-acessibilidade-biblioteca` e PR para `main`.
- Jobs: backend tests, frontend tests/build, structural (`verify_project`, `verify_v03`, `verify_v04`, `verify_v05`), Docker backend/frontend v0.5.
- Não manter dois workflows de release concorrentes para PR de `main`; substituir `v04-ci.yml` por `v05-ci.yml` ao final da branch.

- [ ] **Step 1: Escrever testes RED de autorização v0.5**

Cobrir com contexto Spring/JWT: `ROLE_ALUNO` recebe 403 em `/api/v1/biblioteca/**`; `ROLE_PROFESSOR` não usa endpoints de preferência como aluno; professor B não acessa recurso pessoal de A; aluno A não acessa acessibilidade de atribuição de B; conteúdo `SISTEMA` não aceita mutação por endpoints comuns.

- [ ] **Step 2: Rodar segurança e suite backend**

Run: `cd backend && mvn test`
Expected: novos testes devem inicialmente expor qualquer gap; corrigir apenas gaps v0.5 até GREEN.

- [ ] **Step 3: Criar `verify_v05.py`**

Verificar estruturalmente presença de V9–V12, módulos `acessibilidade/biblioteca`, rotas `/app/biblioteca` e `/app/minhas-preferencias`, endpoint `salvar-no-banco`, SecurityConfig da biblioteca, e ausência de edição das migrations V1–V8 por comparação com base/main quando executado em CI (ou, se o script não tiver Git disponível, verificar nomes/checksums conhecidos documentados na branch).

- [ ] **Step 4: Atualizar versão e README**

`backend/pom.xml` → `0.5.0`; `SystemInfoProvider` → `0.5.0`. README descreve biblioteca, acessibilidade, migrations V9–V12, novas rotas, comandos de teste e critério E2E. Corrigir roadmap: v0.4 concluída; v0.5 implementada apenas quando verificação final passar.

- [ ] **Step 5: Substituir workflow de CI**

Criar `.github/workflows/v05-ci.yml` com Java 21, Node 22, Python 3.13 e Docker builds `mathinclui-backend:v0.5`/`mathinclui-frontend:v0.5`; remover `v04-ci.yml` no mesmo commit para não duplicar CI em PRs futuros.

- [ ] **Step 6: Rodar verificação local completa**

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

Expected: tudo PASS/exit 0. Não declarar release pronta se Docker/Testcontainers não estiverem disponíveis; registrar a limitação e executar no CI.

- [ ] **Step 7: Executar aceite E2E manual/documentado**

Validar em Docker Compose: professor autentica → biblioteca → filtra oficial → copia atividade → importa questão → edita/publica → atribui à turma → adiciona apoio geral → override individual; aluno autentica → recebe config efetiva → aumenta fonte/ativa apoio permitido → erra e recebe dica → conclui → faz logout/login → preferência continua; retornar à fonte e confirmar que conteúdo oficial/modelo não mudou.

- [ ] **Step 8: Commit final da release branch**

```bash
git add backend frontend scripts README.md .github/workflows
git commit -m "chore: prepare MathInclui v0.5 release"
```

- [ ] **Step 9: Review antes do PR**

Executar `superpowers:requesting-code-review` ou revisão equivalente focada nos cinco itens de **Review Focus**. Corrigir achados Important/Critical com TDD e repetir verificação completa antes de abrir PR.

---

## Ordem de execução e dependências

Tasks 1–3 formam o eixo de acessibilidade e devem permanecer sequenciais. Task 4 cria a base relacional da biblioteca; Tasks 5 e 6 dependem dela. Task 7 depende da estrutura de biblioteca estar estável. Tasks 8–11 só começam depois dos contratos HTTP correspondentes estarem verdes no backend. Task 12 só começa quando backend e frontend funcionais da v0.5 estiverem completos.

O plano é intencionalmente único: acessibilidade e biblioteca são dois módulos distintos, mas convergem no editor, na atribuição, na experiência do aluno, na mesma sequência Flyway e no mesmo critério E2E da release. Separá-los em branches independentes criaria mais integração/rebase do que isolamento útil nesta versão.

## Definição de pronto da v0.5

A branch só pode ir para PR quando todos os critérios da spec estiverem cobertos por testes ou pelo E2E documentado, o backend não expuser gabarito antecipado, ownership cruzado estiver bloqueado, as migrations rodarem do zero em PostgreSQL 17, a v0.4 continuar verde, frontend buildar, verificadores estruturais passarem, imagens Docker construírem e a biblioteca oficial/BNCC estiverem baseadas em fonte MEC registrada no repositório.