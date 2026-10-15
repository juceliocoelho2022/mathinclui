# SDD ledger — plan: docs/superpowers/plans/2026-10-15-mathinclui-v0.4-atividades-progresso.md

Ruling: Local git worktree could not be created because the execution sandbox cannot resolve github.com. Implementation will use GitHub commits on the dedicated feature branch and GitHub Actions as the executable RED→GREEN test environment. Cost if wrong: slower feedback and branch-visible test commits, but main remains untouched until explicit merge approval.

Pre-flight shared interfaces:
- Task 1 → Tasks 2/5: V7 schema names/types match plan/spec (`limite_tentativas`, `tentativas.status`, `resposta_tentativas`). Clean.
- Task 2 → Tasks 3/5: entity names and limit interfaces match the planned services. Clean.
- Task 3 → Task 4: `AtividadeService` must expose ownership/system-publication gates used by assignments. Clean.
- Task 4 → Task 5: assignment eligibility depends on `ATIVA`, dates and target turma/aluno. Clean.
- Task 5 → Task 6: attempts/responses/history provide all dashboard metrics. Clean.
- Tasks 3/4/5/6 → Task 7: REST paths are fixed by spec and API clients consume them. Clean.
- Task 7 → Tasks 8/9: typed clients are the only frontend transport boundary. Clean.
- Tasks 8/9 → Task 10: release verifier must check both professor and student routes without duplicating business logic. Clean.
