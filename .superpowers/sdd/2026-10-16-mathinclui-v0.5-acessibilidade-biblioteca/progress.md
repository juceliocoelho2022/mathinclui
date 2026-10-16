# SDD ledger — plan: docs/superpowers/plans/2026-10-16-mathinclui-v0.5-acessibilidade-biblioteca.md

Executor: Native / inline
Started: 2026-10-16
Base before implementation: baf8b25aa0e343ca8955ea5bc48b3505c18b03b1

Pre-flight interfaces:
- Tasks 1→2: V9 accessibility tables are consumed by JPA repositories/services; schema names/enums must match exactly.
- Tasks 2→3: AcessibilidadeEfetivaService and preference contracts are consumed by professor overrides and student detail integration.
- Tasks 4→5→6: V10 library schema is consumed by activity library, classification/tags, question model copy/import.
- Tasks 5/6→7: stable library schema is required before BNCC and official library seeds.
- Tasks 2/3/5/6→8–11: frontend depends on final HTTP DTO contracts; backend must be green first.
- Tasks 1–11→12: release verification/CI consumes all migrations/routes/features.

Ruling: This environment does not have a local git worktree/executor. The approved feature branch itself is the isolated workspace; `main` remains untouched. GitHub Actions will be used as the remote RED→GREEN executor, matching the v0.4 execution method. Cost if wrong: transient feature-branch commits may include failing tests, but no shared/main branch is changed.
Ruling: A temporary branch-only workflow `v05-dev-ci.yml` is execution tooling needed to observe RED/GREEN before Task 12; it will be replaced/removed by the final v0.5 workflow. Cost if wrong: extra CI runs on this feature branch only.
