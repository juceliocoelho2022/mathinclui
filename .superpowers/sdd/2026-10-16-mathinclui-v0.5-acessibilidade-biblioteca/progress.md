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
Task 4: RED observed in run 38969813488 — Flyway stopped at V9; library tables/provenance columns did not exist. V10 implementation passed its own migration contract in run 38970152865, but exposed the older V9 test's exact-version assertion.
Task 4: Ruling: migration feature tests must assert their migration is present (`current >= N`), while only the release-head migration may assert an exact final version — otherwise every later additive migration breaks earlier feature tests without a product regression. Cost if wrong: a missing V9 could theoretically be masked by a later version number, but all V9 tables/constraints remain separately asserted in the same test class.
Task 4: GREEN observed in run 38970287393 — backend full suite PASS, frontend PASS/build PASS, structural PASS after making V9 version assertion forward-compatible.
Task 4: complete (commits eb2de04..880546f, tests: full GitHub Actions backend suite → PASS)
Task 5: RED observed in run 38970621493 — library service/filter/classification/tag contracts absent at testCompile. Production implementation compiled and behavioral tests passed in run 38970923292; only two strict-Mockito unused stubs remained in the system-source deep-copy test. Removed those test-only stubs.
Task 5: GREEN observed in run 38971110015 — backend full suite PASS, frontend tests/build PASS, structural PASS.
Task 5: complete (commits b4c80b7..318c271, tests: full GitHub Actions backend suite → PASS)
Task 6: RED observed in run 38971358693 — testCompile failed because QuestaoModeloService, SalvarQuestaoNoBancoRequest and ImportarQuestoesRequest did not exist. Production implementation landed in commit 53e4ce1; the first GREEN pass exposed that salvarNoBanco returned a response before reloading saved alternatives, so the response contract was incomplete. Commit 8bec661 fixed the response mapping after persisted alternatives/classification.
Task 6: GREEN observed in run 38971878168 — full v0.5 Dev CI PASS (backend, frontend tests/build and structural).
Task 6: complete (commits 59141d7..8bec661, tests: full GitHub Actions suite → PASS)
Task 7: BNCC source/errata recorded in commit b073689. RED observed in run 38977816926 — Flyway stopped at V10 and all five official-library contracts failed with zero seeded rows, while pre-existing tests remained green. V11/V12 then applied cleanly and V12OfficialLibraryIT itself passed; full suite exposed V10LibraryMigrationIT still asserting exact version 10, contrary to the Task 4 ruling. Updated only that version assertion to `current >= 10`, preserving all V10 structure/constraint assertions.
Task 7: GREEN observed in run 38978125603 — backend full suite PASS, frontend tests/build PASS, structural PASS; Flyway reaches V12 and official activities/question models/BNCC contracts pass.
Task 7: complete (commits b073689..5213892, tests: full GitHub Actions suite → PASS)
Task 8: RED observed in run 38978312878 — BibliotecaPage module did not exist and `/app/atividades?editar=77` stayed on the list instead of opening the copied activity. Production added library API/client, filters, activity card, three views, copy guard, pagination, route/session entry point, v0.5 styles and query-param handoff to AtividadeEditor.
Task 8: GREEN observed in run 38978674489 — frontend full test suite PASS, TypeScript/Vite build PASS, backend full suite PASS and structural PASS.
Task 8: complete (commits 2b06b22..10eb321, tests: full GitHub Actions suite → PASS)
Task 9: RED observed in run 38978888410 — QuestaoLibraryPicker was absent, AtividadeEditor had no BNCC/tag classification controls and no “Salvar no banco” action; 17 existing frontend tests still passed. Production added separate curriculum API calls, ClassificacaoCurricularFields, multi-select QuestaoLibraryPicker, question import/reload, and save-to-bank using the activity's year/content/difficulty plus current skill/tag selections.
Task 9: GREEN observed in run 38979125753 — backend full suite PASS, frontend full tests PASS, TypeScript/Vite build PASS and structural PASS.
Task 9: complete (commits 94def21..b26ec79, tests: full GitHub Actions suite → PASS)
Task 10: RED observed in run 38979340980 — assignment UI had no accessibility panel/general save/individual customization/partial-failure retry. During the first implementation pass, Task 9 also emitted a React parent-update warning; regression test commit a4ca376 reproduced it RED in run 38979469090 before production fix 4b4d918 moved the parent callback out of the child state updater. First Task10 GREEN attempt then exposed only an incomplete Vitest mock hiding real `adaptacaoVazia/temAdaptacao`; fixed the test harness with partial import in 5752384.
Task 10: GREEN observed in run 38979792034 — frontend full tests PASS, TypeScript/Vite build PASS, backend full suite PASS and structural PASS. Assignment is persisted first, then general/individual accessibility; partial adaptation failure can retry without duplicating the assignment.
Task 10: complete (commits 3f31653..5752384, tests: full GitHub Actions suite → PASS)
