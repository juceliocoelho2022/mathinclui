# SDD ledger — plan: docs/superpowers/plans/2026-10-16-mathinclui-v0.5-acessibilidade-biblioteca.md

Setup: isolated remote workspace branch `worktree/v0.5-native`, based on `baf8b25aa0e343ca8955ea5bc48b3505c18b03b1`.
Setup: Ruling: the sandbox cannot resolve github.com for clone/worktree execution, so GitHub Actions will be the remote test executor and this git-ignored ledger is temporarily committed on the isolated workspace branch, then removed before handoff — cost if wrong: slower feedback and no local task-start/task-done helper scripts, but branch/test evidence remains auditable.
Pre-flight: Ruling: the written spec uses conceptual literals NORMAL and BAIXO/MEDIO/ALTO, while the persisted v0.4 enums are PADRAO/GRANDE/EXTRA_GRANDE and PADRAO/MODERADO/AMPLIADO; preserve existing persisted literals and explicit ordering from the implementation plan to maintain DB compatibility — cost if wrong: API names differ from conceptual spec wording, but no migration breakage.
Pre-flight: Ruling: spec route `salvar-modelo` conflicts with plan/UI route `salvar-no-banco`; implement both as aliases over the same service — cost if wrong: slightly larger API surface.
Pre-flight: Ruling: spec route `/biblioteca/bncc/habilidades` conflicts with plan route `/biblioteca/bncc`; implement both read aliases — cost if wrong: slightly larger API surface.
Pre-flight: Ruling: spec requires PUT tag rename while plan Task 5 only lists create/delete; implement update as well because the spec is authoritative — cost if wrong: one additional supported command.
Pre-flight Task 1→2: V9 accessibility tables are consumed by accessibility entities/services; no conflict after enum ruling.
Pre-flight Task 2→3: effective accessibility service and preference contracts are consumed by professor override APIs and student detail; no conflict.
Pre-flight Task 3→10: professor adaptation HTTP contracts are consumed by assignment UI; no conflict.
Pre-flight Task 2/3→11: effective accessibility DTO/preferences are consumed by student UI; no conflict.
Pre-flight Task 4→5: V10 activity library/BNCC/tag schema is consumed by activity library service; no conflict.
Pre-flight Task 4→6: V10 question model schema is consumed by question-bank service; no conflict.
Pre-flight Task 4→7: V10 catalog relations are consumed by V11/V12 seed; no conflict.
Pre-flight Task 5/6→8: library activity/question APIs are consumed by professor library UI; route aliases noted above.
Pre-flight Task 5/6→9: classification/import/save-model APIs are consumed by activity editor; route aliases noted above.
Pre-flight Task 1–11→12: release verification consumes all prior schema/API/UI outputs; no conflict.
