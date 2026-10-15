# MathInclui v0.3.0

**Matemática por diferentes caminhos.**

O MathInclui é uma plataforma inclusiva de aprendizagem de Matemática, inicialmente voltada ao 6º, 7º e 8º anos. A v0.3 adiciona gestão de turmas, alunos e perfil pedagógico individual sobre a autenticação segura entregue na v0.2.

## Release v0.3.0

- Gestão de turmas do 6º ao 8º ano.
- Cadastro e manutenção de alunos por turma.
- PIN de aluno armazenado com BCrypt.
- Perfil pedagógico individual sem dados clínicos.
- Área React `/app/gestao` para o professor.
- Isolamento de dados por professor autenticado.


## Stack

- Java 21 + Spring Boot 3.5.6
- Spring Security + OAuth2 Resource Server/Jose
- React 19 + TypeScript + Vite
- PostgreSQL 17 + Flyway
- Docker + Docker Compose
- Nginx no frontend de produção

## Base de autenticação da v0.2

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

## Fluxos de autenticação

### Cadastrar professor

```bash
curl -X POST http://localhost:8080/api/v1/auth/professor/register \
  -H "Content-Type: application/json" \
  -d '{"nome":"Professor Demo","email":"prof@escola.com","password":"SenhaForte123"}'
```

### Login do professor

```bash
curl -X POST http://localhost:8080/api/v1/auth/professor/login \
  -H "Content-Type: application/json" \
  -d '{"email":"prof@escola.com","password":"SenhaForte123"}'
```

### Login do aluno

```bash
curl -X POST http://localhost:8080/api/v1/auth/aluno/login \
  -H "Content-Type: application/json" \
  -d '{"username":"joao.silva","pin":"4821"}'
```

### Renovar sessão

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"COLE_O_REFRESH_TOKEN"}'
```

### Sessão atual

```bash
curl http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer COLE_O_ACCESS_TOKEN"
```

### Recuperação de senha

```bash
curl -X POST http://localhost:8080/api/v1/auth/professor/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"prof@escola.com"}'
```

No ambiente local, com `PASSWORD_RESET_LOG_TOKEN=true`, o token bruto aparece **somente no log do backend**. O banco guarda apenas o SHA-256 do token. Em produção, desative essa opção e conecte um provedor real de e-mail.

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

## Migrações

- `V1__create_core_tables.sql`: professores, turmas, alunos e perfil pedagógico.
- `V2__create_activities.sql`: atividades, questões e alternativas.
- `V3__create_assignments.sql`: atribuições.
- `V4__create_attempts_and_answers.sql`: tentativas e respostas.
- `V5__seed_initial_activities.sql`: atividade inicial de porcentagem.
- `V6__create_auth_tables.sql`: refresh sessions e tokens de recuperação de senha.

## Segurança

- BCrypt para senha/PIN.
- JWT HS256 com segredo externo ao código.
- Access token padrão: 15 minutos.
- Refresh token padrão: 7 dias.
- Refresh rotation a cada renovação.
- Revogação de todos os refresh tokens do professor após redefinição de senha.
- Rate limit padrão: 5 falhas; bloqueio local por 15 minutos.
- Mensagem genérica para credenciais inválidas.
- Frontend guarda tokens em `sessionStorage`; para produção pública, a evolução recomendada é refresh token em cookie HttpOnly/SameSite.

## Roadmap

- **v0.3** — turmas, alunos e perfil pedagógico. **Concluída.**
- **v0.4** — atividades, questões, alternativas e banco de exercícios.
- **v0.5** — experiência do aluno, leitura em voz alta, dicas e correção.
- **v0.6** — dashboard de progresso.
- **v0.7** — Testcontainers, observabilidade, documentação de API e hardening.

## Verificação estrutural

```bash
python3 scripts/verify_project.py
python3 scripts/verify_v03.py
```

## Privacidade pedagógica

O MathInclui não armazena laudos ou diagnósticos clínicos. O perfil pedagógico registra somente necessidades de apoio configuradas pelo professor.

## Documentação

- `docs/superpowers/specs/2026-10-15-mathinclui-v0.1-design.md`
- `docs/superpowers/plans/2026-10-15-mathinclui-v0.2-auth.md`
- `docs/superpowers/plans/2026-10-15-mathinclui-v0.3-gestao-escolar.md`

## IntelliJ IDEA — Java 21

O backend exige **JDK 21**. Se o IntelliJ exibir `JVM target 5` ou usar JDK 17 como fallback:

1. Abra `File > Project Structure > Project` e selecione **SDK 21** e Language level **21 / SDK default**.
2. Em `Modules > mathinclui-backend > Dependencies`, selecione **Module SDK: Project SDK (21)**.
3. Em `Settings > Build, Execution, Deployment > Compiler > Java Compiler`, defina o target do módulo como **21**.
4. Em `Settings > Build Tools > Maven`, use **JDK 21** no Importer/Runner.
5. Abra `backend/pom.xml` como projeto Maven e clique em **Reload All Maven Projects**.

Confirme no terminal do IntelliJ:

```powershell
java -version
javac -version
```

Ambos devem indicar Java 21.


## Execução pelo IntelliJ / terminal local

O Spring Boot importa opcionalmente o arquivo `.env` tanto da raiz (`./.env`) quanto da pasta pai (`../.env`). Isso permite executar o backend com o working directory na raiz do projeto ou em `backend/`.

Antes de iniciar localmente:

```powershell
Copy-Item .env.example .env
```

Confirme que o `.env` contém um `JWT_SECRET` com pelo menos 24 caracteres. O arquivo `.env` não deve ser commitado.


## v0.3 — Gestão escolar

A versão 0.3 adiciona o fluxo do professor para:

- criar, listar, editar e desativar turmas do 6º ao 8º ano;
- cadastrar e listar alunos por turma;
- gerar e redefinir PIN de acesso com BCrypt;
- criar automaticamente um perfil pedagógico padrão por aluno;
- configurar tamanho de fonte, leitura em voz alta, apoio visual, dicas passo a passo, repetição guiada, questões por tela, tempo ampliado e nível de apoio;
- garantir isolamento de dados por professor autenticado;
- acessar a nova área React em `/app/gestao`.

O sistema não registra laudo, diagnóstico ou informação clínica no perfil pedagógico.
