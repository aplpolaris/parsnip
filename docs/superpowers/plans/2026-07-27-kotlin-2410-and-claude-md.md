# CLAUDE.md Dependency Maintenance Playbook + Kotlin 2.4.10 Upgrade — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a root `CLAUDE.md` documenting the repo and a generic Dependency
Maintenance Playbook, then execute that playbook once for real: bump Kotlin
from 2.1.21 to 2.4.10 in both modules, update the README's Kotlin reference,
and carry the work through a GitHub issue, a merged PR, and a prepared (not
executed) release checklist.

**Architecture:** No application code changes — this is a docs file
(`CLAUDE.md`) plus two `pom.xml` property edits, a README edit, and process
artifacts (GitHub issue + PR) created with the `gh` CLI. Verification is
`mvn install`/`mvn test` (existing suites), not new unit tests, since nothing
here has behavior to unit-test.

**Tech Stack:** Maven, Kotlin (`kotlin.version` property), `gh` CLI, GitHub Issues/PRs.

## Global Constraints

- Kotlin target version: **2.4.10** (confirmed real release, July 14, 2026, bug-fix release on the 2.4.0 line) — from spec Q&A.
- `CLAUDE.md` lives at repo root (not `docs/`).
- Dependency survey uses **both** `mvn versions:display-dependency-updates`/`versions:display-plugin-updates` AND a check of open Dependabot PRs/alerts via `gh`.
- Issue tracking: create a real GitHub issue via `gh issue create` in `aplpolaris/parsnip`.
- One combined PR/branch touches both `parsnip-types/pom.xml` and `parsnip/pom.xml` (version bump only, not cross-module-versioned).
- Test order mirrors CI: `parsnip-types` (`mvn install`) then `parsnip` (`mvn test`).
- Manual/example testing only triggered for major-version or behavior-relevant bumps — a Kotlin bugfix-line bump (2.1.21 → 2.4.10 is actually a minor+ language bump; treat as needing at least a run of `examples/alien-encounter-pipeline.yaml` since it crosses several minor versions) — agent states reasoning either way.
- Release (`mvn release:prepare`/`release:perform`) is **prepared, not executed**, by the agent — a checklist handoff to the user, since it publishes irreversibly to Maven Central.
- Never run `git push`, `gh pr create`, or `gh issue create` without this plan having reached that step — no skipping ahead.

---

### Task 1: Write CLAUDE.md

**Files:**
- Create: `CLAUDE.md`

**Interfaces:**
- Produces: the root `CLAUDE.md` file other tasks (and future agent sessions) reference. No code interface — this is documentation only.

- [ ] **Step 1: Write CLAUDE.md**

```markdown
# CLAUDE.md

Guidance for Claude Code (and other coding agents) working in this repository.

## Repo Overview

Parsnip is a JSON data-manipulation/ETL library, split into two independently
published Maven modules:

- **parsnip-types** (`parsnip-types/`) — general utilities, mostly for type
  deserialization. No dependency on `parsnip`.
- **parsnip** (`parsnip/`) — the primary data-manipulation/ETL library.
  Depends on a **fixed released version** of `parsnip-types` declared in
  `parsnip/pom.xml` (e.g. `2.1.0`) — resolved from Maven Central/local repo,
  **not** built from a reactor/multi-module parent. There is no root
  aggregator `pom.xml`.

Both modules are published to Maven Central under `com.googlecode.blaisemath`.

## Build & Test

```bash
# parsnip-types must build/install first — parsnip resolves it from the
# local/remote repo, not a reactor build
cd parsnip-types && mvn install
cd ../parsnip && mvn test
```

CI (`.github/workflows/run-tests.yml`) runs this same order on every push/PR.

Requires JDK 17 and Maven >= 3.6.3 (enforced by `maven-enforcer-plugin` in
both POMs).

## Release Process

Each module is released independently via `maven-release-plugin` +
`central-publishing-maven-plugin` (Sonatype Central), e.g.:

```bash
cd parsnip-types   # or parsnip
mvn release:prepare
mvn release:perform
```

This pushes commits/tags and publishes irreversibly to Maven Central — an
agent should never run these commands without explicit user confirmation
immediately beforehand, and by default should only *prepare* a release
checklist and let the user run the commands.

If `parsnip`'s dependency on `parsnip-types` needs to move to a newly
released `parsnip-types` version, that's a separate, follow-up POM edit
(update the pinned `<version>` under the `parsnip-types` dependency in
`parsnip/pom.xml`) — not implied automatically by releasing `parsnip-types`.

## Dependency Maintenance Playbook

Use this generic, repeatable process for any dependency or tooling version
bump (Kotlin, Jackson, Guava, plugin versions, etc.) — not a one-off recipe
for any single library.

1. **Open a tracking issue** — `gh issue create` in `aplpolaris/parsnip`,
   e.g. title "Dependency maintenance: <date/scope>", with a checklist body
   that gets filled in as the process progresses.
2. **Survey dependencies** — in both `parsnip-types/` and `parsnip/`, run:
   ```bash
   mvn versions:display-dependency-updates
   mvn versions:display-plugin-updates
   ```
   Also check `gh` for open Dependabot PRs/alerts on the repo. Compile one
   combined list of available updates.
3. **Classify updates** — patch/minor (batchable, low risk) vs. major
   (review changelogs/breaking changes individually). Record the
   classification in the tracking issue.
4. **Decide scope for this cycle** — edit the issue with the exact target
   versions being tackled now; explicitly defer the rest to a future issue.
5. **Branch and update** — one combined branch/PR touching both modules'
   `pom.xml` (and README where a version is user-visible), since version
   properties here (e.g. `kotlin.version`) aren't cross-module-dependent.
6. **Run automated tests** in CI order:
   ```bash
   cd parsnip-types && mvn install
   cd ../parsnip && mvn test
   ```
7. **Manual tests, conditionally** — only for major-version or otherwise
   behavior-relevant bumps, exercise `examples/` as a smoke test (e.g. the
   ETL pipeline in `examples/alien-encounter-pipeline.yaml`, run via the
   integration test harness in `parsnip`). State the reasoning for running
   or skipping this step.
8. **Fix and iterate** until all triggered tests are green.
9. **Push the branch and open a PR** via `gh pr create`, referencing the
   tracking issue, summarizing the change and test evidence gathered.
10. **Wait for CI/review**, address feedback, push fixes.
11. **On merge** — close the tracking issue (`gh issue close`), pull `main`
    locally.
12. **Release — prepared by the agent, executed by the user.** Prepare a
    release checklist/notes (which module(s), in what order) but do **not**
    run `mvn release:prepare`/`mvn release:perform` — that's a manual step
    the user runs, since it publishes irreversibly to Maven Central. Release
    `parsnip-types` first only if *its own* artifact version is changing
    (since `parsnip` pins a fixed `parsnip-types` version); otherwise
    release order between the two modules is unconstrained.
```

- [ ] **Step 2: Verify the file reads correctly**

Run: `cat CLAUDE.md | head -5` (or open it) — confirm it starts with `# CLAUDE.md` and renders as valid Markdown.

- [ ] **Step 3: Commit**

```bash
git add CLAUDE.md
git commit -m "docs: add CLAUDE.md with repo overview and dependency maintenance playbook"
```

---

### Task 2: Open the tracking issue and survey dependencies (Playbook steps 1-4)

**Files:**
- None modified — this task produces a GitHub issue, not a file change.

**Interfaces:**
- Consumes: nothing from Task 1.
- Produces: an issue number (e.g. `#43`) referenced by Task 3's PR and commit messages.

- [ ] **Step 1: Run the dependency survey in both modules**

```bash
cd D:/code/aplpolaris/parsnip/parsnip-types
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
cd D:/code/aplpolaris/parsnip/parsnip
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
```

Expected: Maven prints tables of dependencies/plugins with newer versions
available (or "No dependencies/plugins updates available" per section). Note
Kotlin's row: current `2.1.21`, and confirm `2.4.10` is listed as the latest
minor/major available (matches the confirmed release date of 2026-07-14).

- [ ] **Step 2: Check Dependabot state via gh**

```bash
gh api repos/aplpolaris/parsnip/dependabot/alerts --jq '.[] | {package: .dependency.package.name, severity: .security_advisory.severity, state: .state}'
gh pr list --repo aplpolaris/parsnip --search "dependabot" --state open
```

Expected: either an empty list, or a set of alerts/PRs to fold into the
combined update list from Step 1.

- [ ] **Step 3: Open the tracking issue**

```bash
gh issue create --repo aplpolaris/parsnip \
  --title "Dependency maintenance: Kotlin 2.1.21 -> 2.4.10 (2026-07-27)" \
  --body "$(cat <<'EOF'
## Survey

- Kotlin: 2.1.21 -> 2.4.10 (latest stable bug-fix release on the 2.4.x line,
  released 2026-07-14) — targeted this cycle.
- (record any other updates found in Step 1/2 here, classified minor/major,
  and whether they're in or out of scope for this cycle)

## Scope for this cycle

- [ ] Bump `kotlin.version` to 2.4.10 in `parsnip-types/pom.xml` and `parsnip/pom.xml`
- [ ] Update README Kotlin version reference
- [ ] Automated tests pass (parsnip-types install, parsnip test)
- [ ] Manual smoke test of examples/ (Kotlin bump spans multiple minor versions)
- [ ] PR opened and merged
- [ ] Release checklist prepared for user
EOF
)"
```

Expected: command prints the created issue URL, e.g.
`https://github.com/aplpolaris/parsnip/issues/43`. Record the issue number
for use in the PR title/body in Task 3.

---

### Task 3: Bump Kotlin to 2.4.10, update README, test (Playbook steps 5-8)

**Files:**
- Modify: `parsnip-types/pom.xml:54` (`<kotlin.version>2.1.21</kotlin.version>` → `2.4.10`)
- Modify: `parsnip/pom.xml:52` (`<kotlin.version>2.1.21</kotlin.version>` → `2.4.10`)
- Modify: `README.md` (add/update a Kotlin version reference)

**Interfaces:**
- Consumes: issue number from Task 2 Step 3, for the branch/commit naming.
- Produces: a feature branch with the version bump, ready for Task 4's PR.

- [ ] **Step 1: Create a feature branch**

```bash
cd D:/code/aplpolaris/parsnip
git checkout -b chore/kotlin-2.4.10-upgrade
```

- [ ] **Step 2: Bump kotlin.version in parsnip-types/pom.xml**

Change line 54 from:
```xml
        <kotlin.version>2.1.21</kotlin.version>
```
to:
```xml
        <kotlin.version>2.4.10</kotlin.version>
```

- [ ] **Step 3: Bump kotlin.version in parsnip/pom.xml**

Change line 52 from:
```xml
        <kotlin.version>2.1.21</kotlin.version>
```
to:
```xml
        <kotlin.version>2.4.10</kotlin.version>
```

- [ ] **Step 4: Update README.md with the Kotlin version**

Read the current `README.md` "Modules" section first (it currently has no
explicit Kotlin version line — confirm before editing). Add a line under the
Overview or Modules section noting the Kotlin version, e.g. immediately
after the bullet list in the Overview section:

```markdown
Parsnip is built with **Kotlin 2.4.10**.
```

Adjust exact placement/wording to fit the surrounding prose once you're
looking at the live file — the goal is one clear, accurate version
reference, not a rigid insertion point.

- [ ] **Step 5: Build and test parsnip-types first**

```bash
cd D:/code/aplpolaris/parsnip/parsnip-types
mvn install
```

Expected: `BUILD SUCCESS`, all tests pass. If Kotlin 2.4.10 introduces
compile errors or deprecation-as-error failures, fix them here before
proceeding (this is the "fix and iterate" step — capture whatever the actual
compiler output says; don't invent errors that didn't occur).

- [ ] **Step 6: Build and test parsnip**

```bash
cd D:/code/aplpolaris/parsnip/parsnip
mvn test
```

Expected: `BUILD SUCCESS`, all tests pass. `parsnip` resolves `parsnip-types`
from the local `.m2` repo (installed in Step 5) at its pinned version
(`2.1.0` in the dependency section, unrelated to the Kotlin bump) — Step 5
must run first so that JAR is available.

- [ ] **Step 7: Manual smoke test via examples/**

Reasoning: the bump spans 2.1.21 → 2.4.10, several minor Kotlin releases —
treat as behavior-relevant per the playbook's trigger rule, so run the
example ETL pipeline:

```bash
cd D:/code/aplpolaris/parsnip/parsnip
mvn test -Dtest=AlienEncounterPipelineTest
```

(This is the existing integration test that exercises
`examples/alien-encounter-pipeline.yaml` — confirm the actual test class
name in `parsnip/src/test/kotlin` before running; adjust `-Dtest=` to match
if it differs.)

Expected: test passes, confirming the YAML-configured ETL pipeline still
runs correctly under Kotlin 2.4.10.

- [ ] **Step 8: Commit the version bump**

```bash
git add parsnip-types/pom.xml parsnip/pom.xml README.md
git commit -m "chore: upgrade Kotlin from 2.1.21 to 2.4.10

Refs #<issue-number-from-task-2>"
```

---

### Task 4: Open the PR, merge, prepare release checklist (Playbook steps 9-12)

**Files:**
- None modified — this task produces a PR, then (after merge) an issue
  close and a release checklist message to the user.

**Interfaces:**
- Consumes: branch `chore/kotlin-2.4.10-upgrade` from Task 3, issue number
  from Task 2.
- Produces: nothing consumed by a later task — this is the terminal task.

- [ ] **Step 1: Push the branch**

```bash
git push -u origin chore/kotlin-2.4.10-upgrade
```

- [ ] **Step 2: Open the PR**

```bash
gh pr create --repo aplpolaris/parsnip \
  --title "chore: upgrade Kotlin from 2.1.21 to 2.4.10" \
  --body "$(cat <<'EOF'
## Summary
- Bumps `kotlin.version` in both `parsnip-types/pom.xml` and `parsnip/pom.xml` from 2.1.21 to 2.4.10
- Updates README with the current Kotlin version

## Test plan
- [x] `mvn install` in parsnip-types — pass
- [x] `mvn test` in parsnip — pass
- [x] Manual smoke test: examples/alien-encounter-pipeline.yaml integration test — pass

Closes #<issue-number-from-task-2>
EOF
)"
```

Expected: prints the PR URL.

- [ ] **Step 3: Wait for CI and review; address feedback**

No fixed command — monitor `gh pr checks` and respond to review comments,
pushing fixup commits to the same branch as needed:

```bash
gh pr checks --repo aplpolaris/parsnip <pr-number> --watch
```

- [ ] **Step 4: After merge, close the issue and sync main**

```bash
gh issue close <issue-number> --repo aplpolaris/parsnip --comment "Merged in PR #<pr-number>."
git checkout main
git pull
```

- [ ] **Step 5: Prepare (not execute) the release checklist**

Present this to the user rather than running it:

```
Release checklist (run manually — publishes to Maven Central irreversibly):

1. cd parsnip-types && mvn release:prepare && mvn release:perform
2. cd parsnip && mvn release:prepare && mvn release:perform

Order is unconstrained here since only kotlin.version changed (no
parsnip-types artifact version bump), but releasing parsnip-types first is
the repo's existing convention.
```

---

## Plan Self-Review Notes

- **Spec coverage:** all 12 playbook steps from the design doc map to a task
  (Task 1 = CLAUDE.md itself; Task 2 = steps 1-4; Task 3 = steps 5-8; Task 4
  = steps 9-12). Repo-basics section, build/test commands, and release
  mechanics are all in CLAUDE.md per the design's "Where it lives" section.
- **Placeholders:** `<issue-number-from-task-2>` and `<pr-number>` are
  intentional runtime substitutions (the actual numbers don't exist until
  Task 2/4 run) — not spec placeholders, since the surrounding commands are
  fully specified.
- **Type/name consistency:** `kotlin.version` property name, file paths, and
  line numbers were confirmed directly from the current `pom.xml` contents
  (parsnip-types/pom.xml:54, parsnip/pom.xml:52) before writing this plan.
