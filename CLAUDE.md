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
