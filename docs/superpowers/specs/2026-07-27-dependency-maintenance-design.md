# Design: Dependency Maintenance Playbook (CLAUDE.md)

## Purpose

Give Claude Code (and other coding agents) a repeatable, generic process for
routine dependency/version maintenance in this repo — not specific to any one
library — so a maintenance cycle can be kicked off with a single request and
monitored end-to-end.

## Repo shape this process must account for

- Two independent Maven modules published separately to Maven Central:
  `parsnip-types` and `parsnip`, each released via `maven-release-plugin`.
- `parsnip/pom.xml` depends on a **fixed released version** of
  `parsnip-types` (currently `2.1.0`) — resolved from Central/local repo, not
  a reactor build. Bumping a property like `kotlin.version` in both modules
  does not, by itself, create a release-order dependency between them.
- CI (`.github/workflows/run-tests.yml`) builds/tests `parsnip-types` then
  `parsnip`, in that order, on every push/PR.

## Where it lives

Root `CLAUDE.md`, containing:
1. A short repo-basics section (module layout, build/test commands, release
   mechanics) so the playbook has the context it needs without re-deriving it
   each time.
2. A numbered **Dependency Maintenance Playbook** section — the checklist
   below.

## Playbook steps

1. **Open a tracking issue** via `gh issue create` (e.g. "Dependency
   maintenance: <date/scope>"), with a checklist body that gets filled in as
   the process progresses.
2. **Survey dependencies** — run `mvn versions:display-dependency-updates`
   and `versions:display-plugin-updates` in both `parsnip-types` and
   `parsnip`; cross-check `gh` for open Dependabot PRs/alerts. Compile one
   combined list.
3. **Classify updates** — patch/minor (batchable, low risk) vs. major
   (review changelogs/breaking changes individually). Record classification
   in the issue.
4. **Decide scope for this cycle** — edit the issue with the exact target
   versions being tackled now; defer the rest to a future cycle.
5. **Branch and update** — a single combined branch/PR touching both
   modules' `pom.xml` (and README where a version is user-visible), since
   properties like `kotlin.version` aren't cross-module-dependent.
6. **Run automated tests** — `mvn install` in `parsnip-types`, then
   `mvn test` in `parsnip` (mirrors CI order; `parsnip` resolves
   `parsnip-types` from the local/remote repo, not the reactor).
7. **Manual tests, conditionally** — only for major-version or otherwise
   behavior-relevant bumps, exercise `examples/` as a smoke test. The agent
   decides whether this applies and states its reasoning; routine
   patch/minor bumps skip this step.
8. **Fix and iterate** until the automated (and, if triggered, manual) tests
   are green.
9. **Push the branch and open a PR** referencing the tracking issue,
   summarizing what changed and the test evidence gathered.
10. **Wait for CI/review**, address feedback, push fixes.
11. **On merge** — close the tracking issue, pull `main` locally.
12. **Release — prepared by the agent, executed by the user.** The agent
    prepares a release checklist/notes (which module(s), in what order) but
    does **not** run `mvn release:prepare` / `mvn release:perform` itself —
    those publish irreversibly to Maven Central, so the user runs them by
    hand. Order note: release `parsnip-types` first only if *its own*
    artifact version is changing (since `parsnip` pins a fixed
    `parsnip-types` version); otherwise release order is unconstrained.

## Out of scope

- Automating the Central publish step itself.
- Cross-module reactor build changes (would be a separate, larger change to
  the repo's build structure).

## First application of this playbook

Immediately after writing `CLAUDE.md`, run the playbook once for real:
upgrade Kotlin from `2.1.21` to the latest available version in both
modules, update the README's Kotlin version reference, and carry the process
through to a merged PR and a prepared (not executed) release checklist.
