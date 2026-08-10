# PLAN.md — domains.critical.command.example (CriticalExamples) modernization

## Goal

Get this plugin building and running on the latest Paper API, then walk
backward through older Minecraft versions as far as practical, and give an
honest answer on cross-platform (mod-loader) support. See `CLAUDE.md` for
architecture and the ACF dependency writeup.

This repo is part of a larger program (`~/code/minecraft-mods`) porting all
of Bert's Minecraft projects to build against modern Minecraft
versions/platforms. `EpicFurnaces`, `EpicHoppers`, `Spigot-InvUnload`, and
`domains.critical.groups` are finished siblings using the same
Gradle-9/Paper-API/foojay pattern; this repo copies that pattern rather
than reinventing it (`domains.critical.groups` in particular is
structurally near-identical — same era, same author, same nested layout,
same ACF dependency, same Gradle 5/jcenter Groovy build — and was used as
the direct template for this modernization).

**Scope note:** unlike `domains.critical.groups` (a same-era sibling that
*looks* like a demo but was actually meant to be a real feature plugin
left unfinished), this repo genuinely is just an example/test plugin for
ACF command completion — see `CLAUDE.md`'s honest-scope section. There is
no "missing feature" here to flag as an open problem.

## Milestones

### 1. Docs + repo hygiene — DONE

- [x] Investigated provenance: original snapshot (two "Initial commit"
      commits, no fork relationship, no upstream remote).
- [x] `CLAUDE.md` written (architecture, scope note distinguishing this
      from the `domains.critical.groups` scaffold case, ACF dependency
      writeup, platform summary).
- [x] `PLAN.md` written (this file).
- [x] Flatten the nested `domains.critical.command.example/domains.
      critical.command.example/` layout to the repo root (see `CLAUDE.md`
      "Repo layout decision"). Removed from version control:
      `.classpath`, `.project`, `.settings/`, the committed `bin/`
      Eclipse/Buildship output directory, `.DS_Store`.
- [x] Renamed local branch `master` → `main`, pushed. The branch-rename
      API 422'd because `main` already existed remotely (created by the
      `git push -u origin main`) — fell back to `gh repo edit
      --default-branch main`, per the documented fallback. `master` left
      intact remotely, not deleted.

### 2. Modern build (latest Paper API) — DONE

- [x] Converted `build.gradle`/`settings.gradle` (Groovy DSL, Gradle 5.0,
      `jcenter()`, Shadow 4.0.2, `compile` configuration, Java 8) to
      Kotlin DSL matching the `domains.critical.groups` pattern: Gradle
      9.x wrapper, `com.gradleup.shadow` via the `plugins {}` block,
      `org.gradle.toolchains.foojay-resolver-convention` 1.0.0 in
      `settings.gradle.kts`, Java 25 toolchain.
- [x] Repositories: `mavenCentral()`, `https://repo.papermc.io/repository/
      maven-public/` (the old `https://papermc.io/repo/repository/
      maven-public/` URL used by the original build now 403s — confirmed
      dead), and `https://repo.aikar.co/content/groups/aikar/` for
      ACF/TaskChain (confirmed live).
- [x] `paper-api` at the latest resolvable version, queried live (not from
      training memory) — confirmed via `fill.papermc.io/v3/projects/
      paper` and `repo.papermc.io`'s `paper-api` `maven-metadata.xml`:
      **26.2.build.111-stable**, same coordinate the finished siblings
      used (re-verified current at modernization time, 2026-08-10).
- [x] ACF bumped from the original `0.5.0-SNAPSHOT` to the current latest,
      `0.5.1-SNAPSHOT`.
- [x] Fixed the relocation target typo: original relocated ACF to
      `domains.critical.commandcompletionexample.acf`, which doesn't
      match this plugin's actual package
      (`domains.critical.command.example`). Corrected to
      `domains.critical.command.example.acf`.
- [x] `plugin.yml`: `version: '${version}'` templated via
      `processResources`; `api-version` bumped from `1.13` to `"26.2"`.
- [x] Green build (`./gradlew build`), jar contents verified with
      `unzip -l` (not just BUILD SUCCESSFUL — the sibling program hit an
      empty/wrong jar twice). 296 files total: `domains/critical/
      command/example/{CommandExampleMain,CommandExampleCommand}.class`
      present, ACF relocated to `domains/critical/command/example/acf/*`
      (confirmed no leftover unrelocated `co/aikar/commands/*` classes),
      `co/aikar/locales`/`co/aikar/taskchain` correctly left unrelocated
      (matches the corrected relocate rule, which only ever targets
      `co.aikar.commands`). `plugin.yml` in the jar shows `version: '1.0'`
      / `api-version: "26.2"` as expected.
- [x] Commit + push.

### 3. Cross-platform assessment — DONE

**Conclusion: Fabric/NeoForge/Forge are not applicable to this plugin.**

Same architectural reasoning as every Bukkit-API sibling in this program:
Bukkit/Spigot/Paper plugins run against a stable, server-implementation-
maintained API (`org.bukkit.*`/`io.papermc.paper.*`) that stays
source-compatible across MC versions — that's the entire premise of
`plugin.yml`'s single `api-version` gate and why one jar can span the
whole version matrix below. Fabric, Forge, and NeoForge mods compile
directly against Minecraft's own classes (Mojang-mapped or obfuscated)
and use Mixins to inject into game internals, rebuilt per Minecraft
version — a genuinely different programming model, not an alternate
build target for the same source. Porting this plugin onto a mod loader
would mean rewriting its Bukkit-API-calling code from scratch against
Fabric/Forge internals; that's a rewrite, not a port, and out of scope
here.

Per-subsystem reasoning: this plugin has exactly one subsystem — one ACF
command handler (`CommandExampleCommand`, two trivial subcommands that
each send a chat message), no listeners, no schedulers, no persistence,
no config I/O. There is nothing else to evaluate per-subsystem; this is
the shortest such writeup in the program because the plugin itself is
the shortest and simplest — its entire purpose is demonstrating ACF
subcommand completion, not doing anything to a player, world, or config.

Folia-specific note: no `BukkitScheduler` calls, no shared mutable world
state, no per-region assumptions anywhere in the 2-class source. Expected
Folia-compatible "for free" given the total absence of anything Folia's
region-threaded model would break — at phase 1 time, flagged as **not
smoke-tested** rather than claimed as verified. **Phase 2 update:**
re-audited and confirmed; `plugin.yml` now declares `folia-supported: true`
(see milestone 7 below). The Bukkit-API platform matrix row below is
updated accordingly.

**Bukkit-API platform matrix** (what "cross-platform" honestly means for
a plugin like this):

| Server implementation | Status | Notes |
|---|---|---|
| Paper | Supported (primary target) | Built and tested against this |
| Purpur | Expected-compatible | Purpur is a Paper fork with a superset API; nothing here uses anything Purpur would break |
| Folia | **Supported** (`folia-supported: true` set, phase 2) | No scheduler/shared-state usage at all (see above) — the strongest "should just work" case of any plugin in this program; re-audited in phase 2, `folia-supported: true` added to `plugin.yml` and verified present in the packaged jar |
| Spigot | Expected-compatible | Only stable Bukkit/Spigot API surface is used (`JavaPlugin`, `CommandSender`), no Paper-only calls |
| CraftBukkit | Expected-compatible (same reasoning as Spigot) | Not a realistic deployment target in practice |
| Fabric / NeoForge / Forge | **Not applicable** | Different programming model (Mixins + raw Minecraft internals), not an alternate build target for the same Bukkit-API source — see reasoning above |

### 4. Backward version walk — DONE

The plugin has no per-version code branching (a single source set targets
whichever `paper-api` is on the compile classpath), so each older target
was tested by overriding the `paperApiVersion` Gradle property on the
command line — e.g. `./gradlew clean shadowJar
-PpaperApiVersion=1.18.2-R0.1-SNAPSHOT` — rather than maintaining separate
modules/source sets. All four builds succeeded with the same Java 25
toolchain and every resulting jar was verified with `unzip -l`: 296 files
each time (matching the 26.2 default build's file count exactly — 201
`.class` files plus ACF's bundled localization/resource files), plugin
classes present, ACF correctly relocated, no stray unrelocated
`co/aikar/commands/*` entries. This is a genuinely-tested result,
confirmed live against `repo.papermc.io`'s `paper-api`
`maven-metadata.xml` (all four `-R0.1-SNAPSHOT` coordinates resolved), not
an assumption from "it's just Bukkit API so it must work."

| Target | api-version needed | Status |
|---|---|---|
| 26.2 (latest) | `26.2` | **Built.** Default target. Jar: 296 files, verified with `unzip -l`. |
| 1.21.11 (1.21.x) | `1.21` | **Built.** `-PpaperApiVersion=1.21.11-R0.1-SNAPSHOT`. Jar: 296 files, verified. |
| 1.20.1 | `1.20` | **Built.** `-PpaperApiVersion=1.20.1-R0.1-SNAPSHOT`. Jar: 296 files, verified. |
| 1.19.4 | `1.19` | **Built.** `-PpaperApiVersion=1.19.4-R0.1-SNAPSHOT`. Jar: 296 files, verified. |
| 1.18.2 | `1.18` | **Built.** `-PpaperApiVersion=1.18.2-R0.1-SNAPSHOT`. Jar: 296 files, verified. |

Note: `api-version` in the shipped `plugin.yml` is fixed at `"26.2"` (the
actual shipped target's compatibility gate); it is not swapped per build
in this exercise since the older-version builds were a compile/package
verification pass, not four separate release artifacts — same documented
precedent as EpicFurnaces/EpicHoppers/`domains.critical.groups`.

### 5. Verification — DONE

- [x] `unzip -l build/libs/*.jar` per target, confirmed real class files
      present (not empty/truncated) — 296 files each time across all five
      targets (26.2 default + four backward-walk builds).
- [x] PLAN.md checklists updated to reflect actual build results.
- [x] Final commit + push.

### 6. Phase 2: Test coverage — DONE, 100%, zero exclusions

- [x] Added `jacoco` plugin (toolVersion `0.8.15`, matching the
      `domains.critical.groups`/`domains.critical.regions` siblings — the
      Gradle-bundled default is older and doesn't fully understand Java 25
      class files), JUnit 5 (`junit-bom:6.1.3`), MockBukkit
      (`org.mockbukkit.mockbukkit:mockbukkit-v26.1.2:4.115.0`) and Mockito
      (`5.18.0`, added for parity — ended up unused, same as the `groups`
      sibling, since both classes are fully stateless with no fields to
      spy).
- [x] **Test classpath pins `paper-api:26.1.2.build.74-stable` explicitly**,
      independent of the main source set's `-PpaperApiVersion` (default
      26.2.build.111-stable) — MockBukkit's bundled registry-data snapshot
      is tied to that exact build; a newer `paper-api` on the test
      classpath compiles fine but throws `InternalDataLoadException` at
      test runtime. Same known-good pin as both siblings, reused verbatim,
      not re-derived independently.
- [x] `CommandExampleMainTest` (3 tests): `onEnable` registers `/test` in
      the server's `CommandMap` (ACF's `PaperCommandManager` writes
      directly into the command map — `server.getPluginCommand("test")`
      never sees it, since `plugin.yml` declares no `commands:` section;
      confirmed the hard way, matches both siblings' documented gotcha),
      `onDisable`/`onLoad` don't throw.
- [x] `CommandExampleCommandTest` (6 tests): both ACF subcommand handlers
      (`onTest1`/`@Default`, `onTest2`/`@Subcommand("example")`) exercised
      directly against a `PlayerMock` and a `ConsoleCommandSenderMock` —
      neither handler restricts sender type, so both paths are real, not
      redundant — plus two end-to-end tests dispatching through ACF's
      actual command routing (`player.performCommand("test")` and
      `player.performCommand("test example")`) to prove the registration
      wiring in `CommandExampleMain` and the annotation-driven routing in
      `CommandExampleCommand` both work together, not just the handler
      bodies in isolation.
- [x] **Jar-content regression test for the phase-1 relocation-typo fix**:
      a `verifyAcfRelocation` Gradle task (wired into `check`) unzips the
      actual packaged `shadowJar` output and asserts relocated ACF classes
      exist under `domains/critical/command/example/acf/` and that no
      unrelocated `co/aikar/commands/*` entries leaked through. This is a
      task-level check, not a JUnit test, because JUnit exercises compiled
      classes, not the shaded jar — a unit test cannot see a relocation
      regression. Verified this task actually catches a regression (not a
      false positive): reintroduced the exact original typo'd target
      (`domains.critical.commandcompletionexample.acf`) temporarily, reran
      `verifyAcfRelocation`, confirmed it failed with "found none", then
      restored the fix and reran — passed clean.
- [x] Coverage: **100% line, 100% method** across both classes (2 classes
      analyzed — `CommandExampleCommand`, `CommandExampleMain` — matching
      the include list exactly, confirmed via the XML report's counters,
      not just a green `BUILD SUCCESSFUL`). No `BRANCH` counter appears in
      the report at all: neither class contains a single conditional branch
      (no `if`/`else`, no loops) — genuinely branch-free source, not an
      exclusion. `jacocoTestCoverageVerification` enforces LINE and BRANCH
      minimums of `1.00`; `tasks.check` depends on it, so the bar is
      build-enforced, not just reported.
- [x] **Zero exclusions.** Every line of both classes in this plugin is
      genuinely testable via MockBukkit; there is no untestable surface
      (no static Bukkit singletons touched outside what MockBukkit
      provides, no client rendering, no JDBC).
- [x] No bugs found while writing tests — the pre-existing double-semicolon
      import quirk in `CommandExampleMain.java` (see `CLAUDE.md`) is
      cosmetic and does not affect behavior; left as-is per the same
      "don't fix pre-existing cosmetic quirks" precedent as the `groups`
      sibling's console-sender bug (which *was* a real bug and *was*
      fixed there — this repo has no equivalent real bug).
- [x] Commit + push.

### 7. Phase 2: Folia compatibility — DONE, positive verdict

- [x] Static audit: zero scheduler calls (`BukkitScheduler`,
      `getServer().getScheduler()`), zero event listeners, zero mutable
      shared state of any kind (no static fields at all in either class —
      `CommandExampleMain` and `CommandExampleCommand` are both fully
      stateless), zero cross-region teleport/chunk access, zero per-world
      assumptions. Confirmed via direct source grep, not assumption.
- [x] **Verdict: `folia-supported: true` added to `plugin.yml`.** This is
      the strongest possible Folia case in the entire program: a
      genuinely stateless 2-class scaffold with one ACF command handler
      (two trivial subcommands, each just a `sendMessage` call) and no
      other runtime surface whatsoever — even more trivial than the
      `domains.critical.groups` sibling's already-strongest case, since
      that one at least has a player-vs-console branch. Re-verified the
      shaded jar contains the flag: `unzip -p
      build/libs/domains.critical.command.example-1.0.jar plugin.yml`
      shows `folia-supported: true` in the packaged resource, not just the
      source file. Jar file count unchanged at 296 files (the flag adds a
      line to `plugin.yml`, not a new file).

## Open problems / honest blockers

- ACF (`co.aikar:acf-*`) has been resolved only from `co.aikar`'s own
  SNAPSHOT-only repo for its entire existence; no stable release,
  upstream development effectively dormant. Treated as an accepted risk
  (see `CLAUDE.md`), not fixed in this pass. `co.aikar:taskchain-bukkit`
  is an unused dependency, kept for fidelity rather than removed.
- `api-version` in the shipped `plugin.yml` is fixed at the latest target
  ("26.2"); per-`paperApiVersion` templating of `api-version` for the
  backward-walk builds is out of scope, matching documented precedent in
  EpicFurnaces/EpicHoppers/`domains.critical.groups`.

## Repository / git notes

- Local branch was `master` (2 commits: "Initial commit", "Initial
  Commit"). Per this program's git rules: `git branch -m master main &&
  git push -u origin main`, then set GitHub's default branch to `main`
  (via the branch-rename API, falling back to `gh repo edit
  --default-branch main` if that 422s because `main` already exists
  remotely). Neither `master` nor any other branch is deleted.
- Do not commit anything under `.github/workflows/` — the active `gh`
  token for the `bshuler` account lacks the `workflow` scope.
- Commits authored as `Bert Shuler <BertShuler@proton.me>`, signed via the
  1Password SSH agent. If signing fails with no human at the keyboard, the
  prepared commit message goes to the session scratchpad's
  `domains.critical.command.example-commit-msg.txt` instead of being
  force-committed unsigned, and all remaining non-commit work continues.
