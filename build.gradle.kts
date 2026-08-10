import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    java
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
