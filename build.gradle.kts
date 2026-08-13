import org.gradle.jvm.toolchain.JavaLanguageVersion
import java.util.zip.ZipFile

plugins {
    java
    jacoco
    id("com.gradleup.shadow") version "9.6.1"
}

group = "domains.critical.command.example"
version = providers.gradleProperty("pluginVersion").get()

// Java 25: the latest resolvable paper-api (26.2.build.111-stable, matching
// Minecraft's current calendar-versioned release) publishes Gradle module
// metadata requiring JVM 25 - same constraint every sibling in this program
// hit. The foojay-resolver-convention plugin (declared in
// settings.gradle.kts) auto-provisions this JDK into Gradle's own toolchain
// cache; it does not touch the system/Homebrew JDK 21 install. See
// PLAN.md/CLAUDE.md.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    // papermc's old published URL (https://papermc.io/repo/repository/
    // maven-public/, used by the original 2019 build.gradle) now 403s.
    maven("https://repo.papermc.io/repository/maven-public/")
    // ACF (co.aikar:acf-*) and taskchain-bukkit are only ever published
    // here - confirmed still live at modernization time. No stable release
    // has ever existed under this groupId; both artifacts used below are
    // SNAPSHOT coordinates. See CLAUDE.md for the accepted-risk writeup.
    maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${providers.gradleProperty("paperApiVersion").get()}")

    // ACF - shaded and relocated below (see shadowJar.relocate) so this
    // plugin doesn't collide with another plugin bundling a different ACF
    // version on the same server. Bumped from the original 0.5.0-SNAPSHOT
    // to the current latest, 0.5.1-SNAPSHOT.
    implementation("co.aikar:acf-core:0.5.1-SNAPSHOT")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("co.aikar:acf-bukkit:0.5.1-SNAPSHOT")
    // Pulled in by the original build.gradle but unused by this plugin's
    // source (no co.aikar.taskchain import anywhere) - kept for fidelity
    // to the original dependency list. See CLAUDE.md.
    implementation("co.aikar:taskchain-bukkit:3.7.2")

    // Test infrastructure. JUnit (Jupiter) for everything; MockBukkit for
    // simulating a Bukkit/Paper server so code touching org.bukkit.* can be
    // exercised without a real server. Tests deliberately pin
    // `paper-api:26.1.2.build.74-stable` here rather than inheriting
    // whichever `-PpaperApiVersion` the main source set is compiled against
    // (default 26.2.build.111-stable): MockBukkit ships its own bundled
    // registry-data snapshot captured from that exact Paper build, and
    // letting a newer paper-api leak onto the test classpath produces a
    // real InternalDataLoadException at test RUNTIME (compiles fine) - not
    // hypothetical, hit by sibling plugins in this program independently
    // (domains.critical.groups, domains.critical.regions). So: tests run
    // against exactly ONE fixed target, 26.1.2, regardless of what the
    // packaged jar targets. Deliberately NOT extending testImplementation
    // from compileOnly, for the same reason.
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.papermc.paper:paper-api:26.1.2.build.74-stable")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.1.2:4.115.0")
    testImplementation("org.mockito:mockito-core:5.18.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveBaseName.set("domains.critical.command.example")
    // Original build relocated to a typo'd package
    // (domains.critical.commandcompletionexample.acf) that doesn't match
    // this plugin's actual package (domains.critical.command.example).
    // Corrected here to nest under the plugin's own namespace, matching
    // every other sibling's relocation convention. See CLAUDE.md.
    relocate("co.aikar.commands", "domains.critical.command.example.acf")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

// JaCoCo 0.8.15 is the newest published version (matches the
// domains.critical.groups/domains.critical.regions siblings) - the default
// Gradle-bundled version is older and does not fully understand Java 25
// class files.
jacoco {
    toolVersion = "0.8.15"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "1.00".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                minimum = "1.00".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

// Regression check for the phase-1 relocation-typo fix: the original build
// relocated ACF to a typo'd package (domains.critical.commandcompletion
// example.acf) that didn't match this plugin's actual package. Corrected to
// domains.critical.command.example.acf - this task verifies the packaged
// shadow jar directly (unit tests exercise compiled classes, not the shaded
// jar, so they cannot catch a relocation regression) so the fix can never
// silently regress.
tasks.register("verifyAcfRelocation") {
    dependsOn(tasks.shadowJar)
    doLast {
        val jarFile = tasks.shadowJar.get().archiveFile.get().asFile
        ZipFile(jarFile).use { zip ->
            val entries = zip.entries().asSequence().map { it.name }.toList()
            val relocated = entries.any { it.startsWith("domains/critical/command/example/acf/") }
            val unrelocated = entries.any { it.startsWith("co/aikar/commands/") }
            check(relocated) {
                "Expected relocated ACF classes under domains/critical/command/example/acf/ in ${jarFile.name}, found none"
            }
            check(!unrelocated) {
                "Found unrelocated co/aikar/commands/* classes in ${jarFile.name} - relocation regression"
            }
        }
    }
}

tasks.check {
    dependsOn("verifyAcfRelocation")
}

// Tier 2: boots a real headless Paper server with the packaged jar installed.
// Opt-in (needs -PpaperServerJar=...), deliberately not wired into `check`.
// See gradle/paper-boot-test.gradle.kts and PLAN.md.
apply(from = "gradle/paper-boot-test.gradle.kts")

// These commands are registered PROGRAMMATICALLY at runtime by ACF, so they
// appear nowhere in plugin.yml and nothing but a live server can confirm they
// were actually wired into the command map. Naming them here is what makes the
// boot test's command check non-tautological for this repo.
extra["paperBootExpectedCommands"] = listOf("test")
