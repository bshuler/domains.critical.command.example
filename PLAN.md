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
- [ ] Flatten the nested `domains.critical.command.example/domains.
      critical.command.example/` layout to the repo root (see `CLAUDE.md`
      "Repo layout decision"). Remove from version control:
      `.classpath`, `.project`, `.settings/`, the committed `bin/`
      Eclipse/Buildship output directory, `.DS_Store`.
- [ ] Rename local branch `master` → `main`, push, set as GitHub default
      branch.

### 2. Modern build (latest Paper API) — TODO

- [ ] Convert `build.gradle`/`settings.gradle` (Groovy DSL, Gradle 5.0,
      `jcenter()`, Shadow 4.0.2, `compile` configuration, Java 8) to
      Kotlin DSL matching the `domains.critical.groups` pattern: Gradle
      9.x wrapper, `com.gradleup.shadow` via the `plugins {}` block,
      `org.gradle.toolchains.foojay-resolver-convention` 1.0.0 in
      `settings.gradle.kts`, Java 25 toolchain.
- [ ] Repositories: `mavenCentral()`, `https://repo.papermc.io/repository/
      maven-public/` (the old `https://papermc.io/repo/repository/
      maven-public/` URL used by the original build now 403s — dead), and
      `https://repo.aikar.co/content/groups/aikar/` for ACF/TaskChain.
- [ ] `paper-api` at the latest resolvable version, queried live (not from
      training memory) — confirmed via `fill.papermc.io/v3/projects/
      paper` and `repo.papermc.io`'s `paper-api` `maven-metadata.xml`:
      **26.2.build.111-stable**, same coordinate the finished siblings
      used (re-verified current at modernization time, 2026-08-10).
- [ ] ACF bumped from the original `0.5.0-SNAPSHOT` to the current latest,
      `0.5.1-SNAPSHOT`.
- [ ] Fix the relocation target typo: original relocated ACF to
      `domains.critical.commandcompletionexample.acf`, which doesn't
      match this plugin's actual package
      (`domains.critical.command.example`). Corrected to
      `domains.critical.command.example.acf`.
- [ ] `plugin.yml`: `version: ${version}` templated via
      `processResources`; `api-version` bumped from `1.13` to `"26.2"`.
- [ ] Green build (`./gradlew build`), jar contents verified with
      `unzip -l` (not just BUILD SUCCESSFUL): plugin classes present, ACF
      relocated under `domains/critical/command/example/acf/*`, no stray
      unrelocated `co/aikar/commands/*` entries, `plugin.yml` in the jar
      shows expected `version`/`api-version`.
- [ ] Commit + push.

### 3. Cross-platform assessment — TODO

Expected conclusion (to confirm during the pass): Fabric/NeoForge/Forge
not applicable, same architectural reasoning as every Bukkit-API sibling
in this program (stable Bukkit/Paper API vs. Mixin-based mod loaders
rebuilt per version — a different programming model, not an alternate
build target for the same source). Spigot/Purpur expected-compatible;
Folia status to be assessed and stated honestly (likely fine given no
scheduler/shared-state usage, but not smoke-tested unless stated
otherwise below).

### 4. Backward version walk — TODO

Planned targets, via `-PpaperApiVersion=`:

| Target | api-version needed | Status |
|---|---|---|
| 26.2 (latest) | `26.2` | Pending |
| 1.21.11 (1.21.x) | `1.21` | Pending |
| 1.20.1 | `1.20` | Pending |
| 1.19.4 | `1.19` | Pending |
| 1.18.2 | `1.18` | Pending |

Each build's jar to be verified with `unzip -l` (plugin classes present,
ACF relocated, no stray `co/aikar/*` entries).

### 5. Verification — TODO

- [ ] `unzip -l build/libs/*.jar` per target, confirmed real class files
      present.
- [ ] PLAN.md checklists updated to reflect actual build results.
- [ ] Final commit + push.

## Open problems / honest blockers

- ACF (`co.aikar:acf-*`) has been resolved only from `co.aikar`'s own
  SNAPSHOT-only repo for its entire existence; no stable release,
  upstream development effectively dormant. Treated as an accepted risk
  (see `CLAUDE.md`), not fixed in this pass. `co.aikar:taskchain-bukkit`
  is an unused dependency, kept for fidelity rather than removed.
- `api-version` in the shipped `plugin.yml` will be fixed at the latest
  target ("26.2"); per-`paperApiVersion` templating of `api-version` for
  the backward-walk builds is out of scope, matching documented precedent
  in EpicFurnaces/EpicHoppers/`domains.critical.groups`.
- Folia compatibility, if stated, will be a reasoned expectation (no
  scheduler/shared-state usage), not a smoke-tested fact, unless a real
  Folia smoke-boot is performed (not currently planned).

## Repository / git notes

- Local branch is `master` (2 commits: "Initial commit", "Initial
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
