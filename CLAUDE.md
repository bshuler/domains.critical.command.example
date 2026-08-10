# CLAUDE.md — domains.critical.command.example (CriticalExamples)

## What this is

CriticalExamples is a Bukkit/Spigot/Paper **server plugin** (not a client
mod, not a Fabric/Forge/NeoForge mod). Its `plugin.yml` name is
`CriticalExamples`; the Gradle project/package name is
`domains.critical.command.example`.

This is a 2019-era original plugin (package `domains.critical.command.
example`, `api-version: 1.13`), snapshotted into this repo as an
**original copy** — `git log` shows only two commits ("Initial commit" /
"Initial Commit") and there is no upstream/fork relationship. See
`PLAN.md` for the full modernization plan, milestone status, and the
honest platform/version support matrix.

**Honest scope note:** this plugin is exactly what its name and README say
it is — a deliberate, minimal **example** built to exercise aikar's ACF
(Aikar's Command Framework) command/subcommand completion, not a feature
plugin. `CommandExampleMain.onEnable()` registers one ACF command
(`CommandExampleCommand`, aliased `/test`) with two subcommands
(`command`, the `@Default` one, and `example`), each of which just replies
with a string identifying which subcommand ran. There is nothing else to
find here — no storage, no config, no listeners. This modernization makes
the example **build and run cleanly on modern Paper**; there is no
"missing feature" to flag because the example was never meant to have
one — see `PLAN.md` for how this differs from the scaffold-honesty note in
other same-era siblings (`domains.critical.groups`) that look similar but
were meant to be more than a demo.

## Repo layout decision

The original repo had a **nested layout**: repo root held only
`README.md`, with the real Gradle project one level down in
`domains.critical.command.example/domains.critical.command.example/`.
Flattened to the repo root during this modernization — nothing depended
on the nesting (no CI, no multi-project settings, no scripts referencing
the nested path), and every finished sibling in `~/code/minecraft-mods`
(EpicFurnaces, EpicHoppers, Spigot-InvUnload, `domains.critical.groups`)
uses a flat root layout. `.classpath`, `.project`, `.settings/`, and the
committed `bin/` Eclipse/Buildship build-output directory were removed
from version control (their content was stale compiled `.class` files and
Eclipse metadata, none of it needed with Gradle-only tooling). `.DS_Store`
was also removed.

## Architecture

Two classes, both trivial:

- `CommandExampleMain` (`domains.critical.command.example.
  CommandExampleMain`) — plugin entry point (`JavaPlugin`). `onEnable()`
  constructs a `co.aikar.commands.PaperCommandManager` (ACF) and registers
  `CommandExampleCommand`. No listeners.
- `CommandExampleCommand` (`domains.critical.command.example.
  CommandExampleCommand`) — an ACF `BaseCommand` aliased to `/test`. Two
  subcommands: `command` (the `@Default` handler — runs when no
  subcommand is typed) and `example`, each sending a plain chat message
  identifying itself. This is the whole point of the plugin: exercising
  ACF's tab-completion of subcommand names, nothing more.

No config files, no persistence, no other packages.

Pre-existing source oddity, left as-is (harmless, not part of this pass's
scope): `CommandExampleMain.java`'s import line ends in a double
semicolon (`import domains.critical.command.example.
CommandExampleCommand;;`) — a stray empty statement that compiles cleanly
under `javac` and produces no bytecode difference. Not touched, per the
same "don't fix pre-existing cosmetic quirks in a porting pass" precedent
as `domains.critical.groups`' console-sender bug.

## Third-party dependency: ACF (Aikar's Command Framework)

This plugin depends on `co.aikar:acf-core`/`acf-paper`/`acf-bukkit` and
`co.aikar:taskchain-bukkit`, resolved from Aikar's own Maven repo
(`https://repo.aikar.co/content/groups/aikar/`), **not** Maven Central or
papermc's repo. This repo is still live and still serving these artifacts
(verified at modernization time); the plugin's original coordinates
(`0.5.0-SNAPSHOT`) were bumped to the current latest, `0.5.1-SNAPSHOT`
(also still SNAPSHOT-only — ACF has never cut a stable release under this
groupId). ACF's own upstream project (`aikar/commands` on GitHub) has been
effectively dormant for years; this is a known, accepted risk of depending
on it, not a mistake introduced here. Given that this whole plugin exists
*to demonstrate ACF*, dropping ACF for Paper's native Brigadier-based
command API would defeat the plugin's purpose, so — unlike a feature
plugin that merely uses ACF as a convenience — migrating away from ACF is
not a fallback worth planning for here even in the abstract.

`co.aikar:taskchain-bukkit` is pulled in by the original `build.gradle`
but **unused** — no source file imports `co.aikar.taskchain`. Kept (not
removed) for fidelity to the original author's dependency list; harmless
to a shaded plugin jar's correctness.

ACF classes are shaded and relocated into `domains.critical.command.
example.acf` (via the Shadow plugin's `relocate`) so this plugin doesn't
collide with any other plugin on the same server that also bundles a
different ACF version. The *original* build relocated to a typo'd package
(`domains.critical.commandcompletionexample.acf` — doesn't match this
plugin's actual package, `domains.critical.command.example`); corrected
during this modernization to `domains.critical.command.example.acf` so the
relocated path actually nests under the plugin's own namespace, matching
every other sibling's relocation convention.

## Platforms

This is Bukkit-API software. "Platform" here means Bukkit-API server
implementations, not mod loaders — see `PLAN.md` milestone 3 for the full
per-subsystem reasoning:

- **Paper** (primary target), and by API compatibility, **Purpur**.
- **Folia** — **supported** (`folia-supported: true` in `plugin.yml`, phase
  2). This plugin has zero schedulers, zero listeners, and zero shared
  mutable state of any kind (no static fields in either class), so there
  was nothing to fix to make it region-thread-safe — the flag is a
  straightforward positive, not an unverified guess. See `PLAN.md`
  milestone 7.
- **Spigot** — only stable Bukkit/Spigot API is used, no Paper-only calls.
- **Fabric / NeoForge / Forge are not applicable** — same architectural
  reasoning as every other Bukkit-API sibling in this program: these are a
  different programming model (direct Minecraft-internals + Mixins,
  rebuilt per version), not an alternate build target for the same
  source.

## Build

```bash
./gradlew build          # compiles, shades, produces build/libs/domains.critical.command.example-<version>.jar
./gradlew shadowJar      # same, explicit task name
```

- Gradle 9.x, Java 25 toolchain (auto-provisioned via the foojay resolver —
  do not install a system JDK for this; the only installed system JDK,
  Temurin 21, is untouched). Java 25 is required because the latest
  `paper-api`'s Gradle module metadata declares it as a minimum.
- Paper API target is overridable via the `paperApiVersion` Gradle
  property, defaulted in `gradle.properties` to the latest resolvable
  coordinate from `repo.papermc.io/repository/maven-public/`. Do not
  trust a cached memory of "the latest MC version" — query
  `https://fill.papermc.io/v3/projects/paper` or `paper-api`'s
  `maven-metadata.xml` at build time; Minecraft is calendar-versioned now
  (26.x).
- `plugin.yml`'s `${version}` is templated via `processResources` from the
  Gradle project version (`pluginVersion` property). `api-version` is
  fixed at the latest target ("26.2") in the shipped resource; per-
  `paperApiVersion` templating of `api-version` is deliberately out of
  scope for the backward version walk (same precedent as
  EpicFurnaces/EpicHoppers/`domains.critical.groups`) — see PLAN.md Open
  Problems.

## Tests

```bash
./gradlew test jacocoTestReport                                          # run tests + HTML/XML coverage report
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification check # full verification, matches CI expectations
```

- Report: `build/reports/jacoco/test/html/index.html` (human-readable),
  `build/reports/jacoco/test/jacocoTestReport.xml` (machine-readable).
- **Current status: 100% line coverage, 100% method coverage, zero
  exclusions**, enforced by `jacocoTestCoverageVerification` (LINE and
  BRANCH minimum `1.00` — see `build.gradle.kts`). 2 classes analyzed:
  `CommandExampleMain`, `CommandExampleCommand`. No `BRANCH` counter
  appears in the report at all — neither class contains a single
  conditional branch, so branch coverage is vacuously satisfied rather
  than a meaningful metric here.
- **MockBukkit is pinned to a fixed Paper build on the test classpath**,
  independent of whatever `-PpaperApiVersion` the main source set targets:
  `testImplementation("io.papermc.paper:paper-api:26.1.2.build.74-stable")`
  and `testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.1.2:4.115.0")`
  are declared explicitly rather than inherited from `compileOnly` — a newer
  `paper-api` on the test classpath compiles fine but throws
  `InternalDataLoadException` at test *runtime*, because MockBukkit ships a
  bundled registry-data snapshot captured from its own exact Paper build
  (same versions as the `domains.critical.groups`/`domains.critical.regions`
  siblings; not re-verified independently here since it's the identical
  known-good pin).
- **ACF-registered commands (via `PaperCommandManager`) are invisible to
  `server.getPluginCommand("test")` in MockBukkit tests**, same as the
  `domains.critical.groups`/`domains.critical.regions` siblings. Use
  `server.getCommandMap().getCommand("test")` instead.
- Both classes are fully stateless (no static fields), so no
  Mockito/inline-mock-maker gotchas or shared-state reset-between-tests
  concerns applied — plain MockBukkit (`PlayerMock`,
  `ConsoleCommandSenderMock`) was sufficient for every branch. The
  `mockito-core` test dependency was added for parity with the sibling
  pattern but ended up unused.
- Tests are real: `CommandExampleCommandTest` asserts actual message
  content from both ACF subcommand handlers against a player sender and a
  console sender, plus two end-to-end tests that dispatch through ACF's
  real command routing (`performCommand("test")` /
  `performCommand("test example")`) rather than only calling the handler
  methods directly.
- **Jar-content regression test for the phase-1 relocation-typo fix**: the
  `verifyAcfRelocation` Gradle task (wired into `tasks.check`, runs after
  `shadowJar`) unzips the actual packaged jar and asserts relocated ACF
  classes exist under `domains/critical/command/example/acf/` with no
  unrelocated `co/aikar/commands/*` entries. This is a build-level check,
  not a JUnit test, because JUnit exercises compiled classes, not the
  shaded jar. Verified during phase 2 that it actually fails when the
  original typo'd relocation target is reintroduced (not a false-positive
  check that would pass regardless).

## Porting notes for whoever touches this next

- No Material/Sound/Particle enum usage anywhere in this plugin's source —
  the usual p/q enum-rename hazard that hit other siblings in this program
  does not apply here. Nothing to guard with a runtime `valueOf` fallback.
- `co.aikar.commands.PaperCommandManager` and the `BaseCommand`/
  `@CommandAlias`/`@Default`/`@Subcommand`/`@Description` annotations used
  here are all from ACF's oldest, most stable API surface and compiled
  unchanged against the latest Paper API during this modernization — no
  ACF-side breakage found.
- If this plugin is ever cloned as a starting point for a real feature
  plugin, remember it is deliberately a bare command-completion demo — add
  real subcommands/logic there, not here.
