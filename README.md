# MathInclui v0.2.3

**Matemática por diferentes caminhos.**

O MathInclui é uma plataforma inclusiva de aprendizagem de Matemática, inicialmente voltada ao 6º, 7º e 8º anos. A v0.2 adiciona autenticação completa por perfil sobre a fundação Java + React + PostgreSQL da v0.1.

## Patch v0.2.3

- Corrige o build TypeScript no Docker (`TS5096`) adicionando `noEmit: true` ao `frontend/tsconfig.node.json`.
- Mantém `allowImportingTsExtensions: true` compatível com `tsc -b`.

## Stack

- Java 21 + Spring Boot 3.5.6
- Spring Security + OAuth2 Resource Server/Jose
- React 19 + TypeScript + Vite
- PostgreSQL 17 + Flyway
- Docker + Docker Compose
- Nginx no frontend de produção

## O que existe na v0.2

- cadastro de professor com e-mail + senha;
- login de professor com BCrypt;
- login de aluno com `username` + PIN numérico de 4 a 6 dígitos;
- PIN e senha armazenados somente como hash BCrypt;
- JWT de acesso + JWT de refresh;
- refresh token com sessão persistida, rotação e revogação;
- logout idempotente;
- endpoint de sessão atual `GET /api/v1/auth/me`;
- perfis `ROLE_PROFESSOR` e `ROLE_ALUNO`;
- access token exigido nas rotas protegidas; refresh token não pode ser usado como bearer de API;
- rate limiting local de tentativas de login;
- recuperação de senha do professor com token aleatório e hash SHA-256 no banco;
- frontend com entrada separada para professor e aluno;
- migração Flyway `V6__create_auth_tables.sql`;
- Docker Compose e variáveis de ambiente atualizados.

> **Importante:** na v0.2 o aluno já consegue autenticar contra a tabela `alunos`, mas a interface de cadastro de turmas/alunos será implementada na **v0.3**. Até lá, contas de aluno precisam existir no banco para testar o login de estudante.

## Executar com Docker

1. Copie o arquivo de ambiente:

```bash
cp .env.example .env
```

2. Troque pelo menos `DB_PASSWORD` e `JWT_SECRET` no `.env`.

3. Suba os serviços:

```bash
docker compose up --build
```

4. Acesse:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080/api/v1/system/info`
- Health: `http://localhost:8080/actuator/health`

## Desenvolvimento local

### Backend

Requer Maven 3.9+ e Java 21:

```bash
cd backend
mvn test
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run build
npm run dev
```

## Segurança da v0.2

- BCrypt para senha/PIN.
- JWT HS256 com segredo externo ao código.
- Access token padrão: 15 minutos.
- Refresh token padrão: 7 dias.
- Refresh rotation a cada renovação.
- Rate limit padrão: 5 falhas; bloqueio local por 15 minutos.
- Frontend guarda tokens em `sessionStorage`; para produção pública, a evolução recomendada é refresh token em cookie HttpOnly/SameSite.

## Roadmap

- **v0.3** — turmas, alunos e perfil pedagógico.
- **v0.4** — atividades, questões, alternativas e banco de exercícios.
- **v0.5** — experiência do aluno, leitura em voz alta, dicas e correção.
- **v0.6** — dashboard de progresso.
- **v0.7** — Testcontainers, observabilidade, documentação de API e hardening.

## Privacidade pedagógica

O MathInclui não armazena laudos ou diagnósticos clínicos. O perfil pedagógico registra somente necessidades de apoio configuradas pelo professor.
