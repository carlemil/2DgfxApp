<!--
SYNC IMPACT REPORT
==================
Version change: N/A → 1.0.0 (initial ratification)
Modified principles: N/A (new constitution — no prior version)
Added sections:
  - Core Principles (I. Performance & Frame Budget, II. Compose-First UI,
    III. Coroutine-Based Concurrency, IV. Test-First Development,
    V. Simplicity & YAGNI)
  - Technology Stack Requirements
  - Quality Gates & Review Process
  - Governance
Removed sections: N/A
Templates reviewed:
  ✅ .specify/templates/plan-template.md   — Constitution Check reads file
                                             dynamically; no updates needed.
  ✅ .specify/templates/spec-template.md   — No principle-specific references;
                                             no updates needed.
  ✅ .specify/templates/tasks-template.md  — Task categories compatible with
                                             all five principles; no updates needed.
  ✅ .claude/commands/speckit.specify.md   — No agent-only overrides; no updates needed.
  ✅ .claude/commands/speckit.plan.md      — Reads constitution dynamically; no updates needed.
  ✅ .claude/commands/speckit.tasks.md     — No constitution-specific references;
                                             no updates needed.
Follow-up TODOs: None — all placeholders resolved.
-->

# 2DgfxApp Constitution

## Core Principles

### I. Performance & Frame Budget

All rendering and demo-loop logic MUST target a sustained 60 fps on the
declared minimum Android API level. Frame-time budgets MUST be defined per
feature in plan.md; any implementation that causes measurable frame drops
MUST be blocked until resolved or explicitly justified in Complexity Tracking.
Profiling with Android Studio's CPU/GPU profiler is REQUIRED before marking
any rendering task complete.

**Rationale**: Frame-rate consistency is a first-class UX requirement for
2d demos. Degraded performance perceived by viewers is a defect, not a trade-off.

### II. Compose-First UI

All UI MUST be built with Jetpack Compose. Use of legacy XML layouts or the
Android View system is PROHIBITED unless a specific Compose interoperability
path is unavailable; any such exception MUST be documented as a Complexity
Tracking entry in plan.md. Composable functions MUST be stateless and delegate
all mutable state to ViewModels or explicit state holders.

**Rationale**: Compose is the canonical Android UI toolkit for this project.
Mixing UI paradigms introduces cognitive overhead and testing complexity that
conflicts with Principle V.

### III. Coroutine-Based Concurrency

All asynchronous operations MUST use Kotlin Coroutines with structured
concurrency (CoroutineScope / SupervisorJob). Raw threads, RxJava, and
`GlobalScope` are PROHIBITED. Long-running demo-loop work MUST run on a
dedicated dispatcher (`Dispatchers.Default` or a named `DemoLoop` dispatcher);
all UI-state updates MUST occur on `Dispatchers.Main`.

**Rationale**: Coroutines are the idiomatic Kotlin async model and integrate
natively with Compose lifecycle scopes (`LaunchedEffect`, `rememberCoroutineScope`).
Unstructured concurrency creates lifecycle leaks and race conditions.

### IV. Test-First Development

All demo-logic, ViewModel, and repository code MUST follow TDD: write a
failing test → obtain approval → implement → confirm test passes.
Red-Green-Refactor is the mandatory development cycle for these layers.
Compose UI tests are REQUIRED for any user-facing interaction introduced by
a feature. Rendering paths may be exempted from automated tests only when no
deterministic assertion is feasible; exemptions MUST be documented in plan.md.

**Rationale**: demo logic is prone to subtle state-machine bugs. Test-first
enforces explicit contracts and prevents regressions across demo states.

### V. Simplicity & YAGNI

Every feature MUST begin at the simplest viable implementation. Abstractions,
design patterns, and third-party libraries MUST be explicitly justified
(documented in Complexity Tracking before adoption). Premature optimization is
PROHIBITED; performance improvements MUST be preceded by a profiler measurement
that identifies a concrete bottleneck.

**Rationale**: 2D demo codebases grow quickly in scope. Keeping complexity low
preserves velocity and limits the bug surface area.

## Technology Stack Requirements

The following stack is canonical and MUST NOT be substituted without a
constitution amendment:

- **Language**: Kotlin (JVM target)
- **UI framework**: Jetpack Compose (latest stable)
- **Async**: Kotlin Coroutines + Flow
- **Platform**: Android (minimum API level defined per feature in plan.md)
- **Build**: Gradle with Kotlin DSL
- **Testing**: JUnit 5, Compose UI Test, Turbine (Flow testing)

New libraries MUST be evaluated against Principle V before adoption. The
evaluation rationale MUST be recorded in `research.md` for the relevant
feature.

## Quality Gates & Review Process

Every feature MUST pass all gates before merging to the main branch:

1. **Constitution Check** — plan.md Constitution Check section passes with
   no unjustified violations.
2. **Frame Budget Check** — no measurable fps regression introduced; a profiler
   screenshot or benchmark result MUST be attached to the PR.
3. **Test Coverage** — all demo-logic, ViewModel, and repository tests pass;
   Compose UI tests for new user-facing Composables pass.
4. **No Raw Threads** — static analysis (`detekt`) reports zero `Thread()` or
   `GlobalScope` usages in changed files.
5. **Spec Alignment** — implementation satisfies all acceptance scenarios
   defined in spec.md.

Code review is REQUIRED for all PRs. The reviewer MUST explicitly confirm
Constitution compliance in their review comment before approving.

## Governance

This constitution supersedes all other development practices and guidelines
for 2DgfxApp. When a conflict arises between any other document and this
constitution, the constitution takes precedence.

**Amendment procedure**: Any amendment MUST:

1. Be proposed as a PR containing the rationale and a migration plan.
2. Receive explicit approval from at least one additional maintainer.
3. Increment the version according to the versioning policy below.
4. Update `LAST_AMENDED_DATE` to the merge date.

**Versioning policy**:

- MAJOR: A principle is removed, renamed to change its meaning, or a Quality
  Gate is removed.
- MINOR: A new principle or section is added, or existing guidance is
  materially expanded.
- PATCH: Wording clarifications, typo fixes, or non-semantic refinements.

**Compliance review**: Each PR reviewer MUST verify adherence to all five
core principles. The Constitution Check section of plan.md documents
pre-implementation verification; it MUST be re-evaluated after Phase 1 design.

**Runtime guidance**: Refer to per-feature `quickstart.md` files for
day-to-day development guidance within each feature branch.

**Version**: 1.0.0 | **Ratified**: 2026-02-22 | **Last Amended**: 2026-02-22
