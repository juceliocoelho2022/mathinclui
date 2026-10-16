# MathInclui v0.4.0

**Matemática por diferentes caminhos.**

O MathInclui é uma plataforma inclusiva para aprendizagem de Matemática do 6º ao 8º ano. A v0.4 consolida o ciclo completo de atividade: o professor cria questões, publica e atribui; o aluno resolve com tentativas e dicas progressivas; o professor acompanha participação e resultados sem ranking competitivo.

## Release v0.4.0

### Professor

- cria e edita atividades próprias;
- usa questões `MULTIPLA_ESCOLHA`, `NUMERICA` e `TEXTO_CURTO`;
- define limite padrão de tentativas, com sobrescrita opcional por questão;
- cadastra até três dicas progressivas e explicação;
- publica atividades válidas;
- atribui para uma turma inteira ou para um aluno específico;
- acompanha participação, acertos, erros finais, submissões, dicas e tempo.

### Aluno

- acessa atividades destinadas diretamente a ele ou à sua turma;
- vê estados **Para fazer**, **Em andamento** e **Concluídas**;
- retoma uma tentativa em andamento;
- responde uma questão por vez;
- recebe dica após erro quando disponível;
- pode tentar novamente até o limite configurado;
- só recebe explicação e resposta correta após acertar ou esgotar o limite;
- visualiza o próprio resultado final.

## Segurança e integridade

- JWT separa `ROLE_PROFESSOR` e `ROLE_ALUNO`;
- professor só administra recursos próprios;
- atividade de origem `SISTEMA` publicada pode ser atribuída, mas não editada pelo professor;
- aluno só acessa suas próprias atribuições/tentativas;
- o frontend do aluno não recebe gabarito antecipado;
- correção é exclusiva do backend;
- atividade com histórico de aluno bloqueia alterações destrutivas;
- uma única tentativa completa é permitida por atribuição/aluno na v0.4;
- perfil pedagógico não armazena diagnóstico, laudo ou informação clínica.

## Correção

- **Múltipla escolha:** o aluno envia o ID da alternativa e o backend valida que ela pertence à questão.
- **Numérica:** usa `BigDecimal`; `2`, `2.0` e `2,00` são equivalentes; formatos com ponto e vírgula simultaneamente são rejeitados.
- **Texto curto:** normalização Unicode NFC, `trim`, colapso de espaços e comparação sem distinção de maiúsculas/minúsculas; acentos são preservados.

Cada questão vale uma unidade. O percentual final é `acertos / total de questões * 100`; erros intermediários ficam no histórico de submissões e não contam como erro final.

## Stack

- Java 21 + Spring Boot 3.5.6
- Spring Security + OAuth2 Resource Server/JWT
- Spring Data JPA
- PostgreSQL 17 + Flyway
- JUnit 5 + Mockito + Testcontainers
- React 19 + TypeScript + Vite
- Vitest + Testing Library
- Docker + Docker Compose + Nginx

## Migrações

- `V1__create_core_tables.sql`: professores, turmas, alunos e perfil pedagógico.
- `V2__create_activities.sql`: atividades, questões e alternativas.
- `V3__create_assignments.sql`: atribuições.
- `V4__create_attempts_and_answers.sql`: tentativas e respostas consolidadas.
- `V5__seed_initial_activities.sql`: atividade inicial.
- `V6__create_auth_tables.sql`: sessões de refresh e recuperação de senha.
- `V7__extend_learning_flow.sql`: limites de tentativas, status da tentativa, histórico `resposta_tentativas`, constraints e índices da v0.4.

## Rotas principais do frontend

- Professor — gestão escolar: `/app/gestao`
- Professor — atividades: `/app/atividades`
- Aluno — minhas atividades: `/app/atividades-aluno`
- Aluno — resolução: `/app/atividades-aluno/:atribuicaoId`
- Aluno — resultado: `/app/tentativas/:tentativaId/resultado`

## Executar com Docker

Crie `.env` a partir de `.env.example` e preencha `DB_PASSWORD` e `JWT_SECRET` com valores locais seguros. O `.env` não deve ser commitado.

```bash
docker compose --env-file .env config -q
docker compose --env-file .env up --build -d
docker compose ps
```

Padrão do projeto: frontend em `http://localhost:3000`, backend em `http://localhost:8080` e PostgreSQL publicado conforme `DB_PORT` do `.env`.

## Testes e verificação

Backend:

```bash
cd backend
mvn test
```

Os testes de migration usam PostgreSQL 17 via Testcontainers e exigem Docker disponível.

Frontend:

```bash
cd frontend
npm install
npm run test:run
npm run build
```

Verificadores estruturais:

```bash
python scripts/verify_project.py
python scripts/verify_v03.py
python scripts/verify_v04.py
```

## Critério E2E da v0.4

1. Professor autentica, cria uma atividade, adiciona questões, configura tentativas/dicas, publica e atribui.
2. Aluno autentica, visualiza a atribuição, inicia/retoma, erra e recebe dica, tenta novamente, acerta ou esgota o limite e conclui.
3. Professor abre os resultados e visualiza participação, desempenho, submissões, dicas, tempo e erros por questão.
4. Aluno não acessa telas/APIs administrativas e um professor não acessa recursos pertencentes a outro professor.

## Roadmap

- **v0.3** — gestão escolar, alunos e perfil pedagógico. **Concluída.**
- **v0.4** — atividades, questões, atribuições, experiência do aluno, correção, tentativas/dicas e resultados. **Implementada na branch de release até validação E2E.**
- **v0.5** — acessibilidade ampliada e banco pedagógico reutilizável.
- **v0.6** — relatórios pedagógicos longitudinais e evolução individual.
- **v0.7** — observabilidade, documentação de API e hardening de produção.

## Documentação técnica

- Design: `docs/superpowers/specs/2026-10-15-mathinclui-v0.4-atividades-design.md`
- Plano: `docs/superpowers/plans/2026-10-15-mathinclui-v0.4-atividades-progresso.md`

O MathInclui mantém como princípio central a inclusão pedagógica sem rotular estudantes nem registrar dados clínicos.
