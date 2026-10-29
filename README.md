# domains.critical.command.example (CriticalExamples)

A deliberately minimal Bukkit/Spigot/Paper plugin demonstrating
[Aikar Command Framework (ACF)](https://github.com/aikar/commands)
command/subcommand tab-completion. That is its entire purpose — it is an
**example**, not a feature plugin.

- `/test` — the ACF command (class `CommandExampleCommand`).
  - `/test command` — the `@Default` subcommand; replies identifying itself.
  - `/test example` — second subcommand; same idea.

There is no storage, no config, no listeners. ACF is shaded and relocated
into the jar.

## Provenance

An original 2019 plugin by Human (`TheCritic`), snapshotted into this repo
— not a fork, no upstream. This modernization flattened the original
nested Eclipse layout to the repo root, removed IDE artifacts from version
control, and rebuilt it on modern Gradle so the example actually builds
and runs on current Paper. (Unlike its sibling `domains.critical.groups`,
which *looks* similar but was meant to become a real plugin, this one was
always meant to be exactly this small.)

## Supported Paper versions

One codebase, no version branches. Default build targets the newest stable
Paper API (currently **26.2**); the same source compiles cleanly against
older API lines:

```sh
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
./gradlew build                                             # 26.2 (default)
./gradlew build -PpaperApiVersion=1.21.11-R0.1-SNAPSHOT
./gradlew build -PpaperApiVersion=1.20.1-R0.1-SNAPSHOT
./gradlew build -PpaperApiVersion=1.19.4-R0.1-SNAPSHOT
./gradlew build -PpaperApiVersion=1.18.2-R0.1-SNAPSHOT
```

All five targets are verified builds. The jar lands in `build/libs/`.

## Testing

1. **Unit tests** — `./gradlew check` (JUnit 5 over the command handlers
   and plugin main).
2. **Headless Paper boot smoke test** (opt-in — needs a real Paper server
   jar):

   ```sh
   ./gradlew paperBootTest -PpaperServerJar=/path/to/paper-26.2-111.jar
   ```

   Boots a real headless Paper server with the packaged jar in `plugins/`
   and asserts: the jar loads, `onEnable` doesn't throw, `/test` is
   registered in the live command map and doesn't throw when invoked, the
   plugin shows in `plugins`, and `onDisable` exits cleanly. Without a
   server jar the task reports `SKIPPED (this is a skip, not a pass)`.
   Transcript: `build/paper-boot/paper-boot-test.log`.

Full modernization history: `PLAN.md`.
