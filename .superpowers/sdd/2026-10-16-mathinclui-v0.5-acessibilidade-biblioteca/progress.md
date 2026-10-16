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

Preflight: baseline run 38968331710 — backend GREEN, frontend GREEN/build GREEN, structural GREEN.
Task 1: RED observed in run 38968496397 — Flyway v8, 3 accessibility tables absent; 53 tests, expected V9 failures only. GREEN observed in run 38968600360 — backend full suite PASS, frontend PASS/build PASS, structural PASS.
Task 1: complete (commits 0f837b7..7271e83, tests: full GitHub Actions backend suite → PASS)
Task 2: RED observed in run 38968790062 — testCompile failed because accessibility domain/repositories/services did not exist. First GREEN attempt run 38969028838 exposed only Mockito UnnecessaryStubbing in the 404 test; root cause was three global stubs not reached on the early-return path, not a production failure. Removed those stubs. GREEN observed in run 38969232817 — backend full suite PASS, frontend PASS/build PASS, structural PASS.
Task 2: complete (commits 17e10e5..d175281, tests: full GitHub Actions backend suite → PASS)
Task 3: RED observed in run 38969457277 — missing AdaptacaoAtribuicaoService/AdaptacaoRequest and AlunoAtividadeService still consumed PerfilPedagogicoRepository. GREEN observed in run 38969659698 — backend full suite PASS, frontend PASS/build PASS, structural PASS. Student detail now exposes effective accessibility DTO and professor overrides enforce assignment/student ownership.
Task 3: complete (commits bfdc916..a23dcdf, tests: full GitHub Actions backend suite → PASS)
