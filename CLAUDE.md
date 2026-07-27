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
4. **Decide scope for this cycle** — the agent proposes which updates to
   tackle now (based on the classification in step 3) and which to defer,
   but the **user approves the final list** before any code changes are
   made. Edit the issue with the exact target versions agreed on;
   explicitly defer the rest to a future issue.
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
10. **Wait for CI, then the user reviews and merges.** The agent does not
    merge dependency-update PRs itself — a human must review and click
    merge, even if CI is green. Address any review feedback by pushing
    fixes to the same branch.
11. **On merge** — close the tracking issue (`gh issue close`), pull `main`
    locally.
12. **Release — prepared by the agent, executed by the user.** Prepare a
    release checklist/notes (which module(s), in what order) but do **not**
    run `mvn release:prepare`/`mvn release:perform` — that's a manual step
    the user runs, since it publishes irreversibly to Maven Central. Release
    `parsnip-types` first only if *its own* artifact version is changing
    (since `parsnip` pins a fixed `parsnip-types` version); otherwise
    release order between the two modules is unconstrained.
13. **If `parsnip-types` was released in this cycle, sync the pin.**
    Immediately after `parsnip-types`'s `mvn release:perform` completes,
    update the pinned `<version>` under the `parsnip-types` dependency in
    `parsnip/pom.xml` to the version just released, in its own small commit
    pushed straight to `main` (verify with `mvn test` in `parsnip` first —
    it should resolve the new `parsnip-types` artifact from Central). Do
    this **before** running `parsnip`'s own `release:prepare`, so `parsnip`
    is never released pinned to a stale `parsnip-types` version. Skipping
    this step is exactly what caused an aborted, wrongly-pinned `parsnip`
    release attempt in the 2026-07-27 cycle.
14. **Publish on Sonatype Central.** After `mvn release:perform` completes
    for a module, its artifacts land in a Central Publishing Portal
    deployment that still needs a manual publish action. Go to
    **https://central.sonatype.com/publishing** and hit the **Publish**
    button for that deployment.
