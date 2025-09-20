import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

configurations {
    create("common") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    named("compileClasspath") { extendsFrom(getByName("common")) }
    named("runtimeClasspath") { extendsFrom(getByName("common")) }
    maybeCreate("developmentNeoForge").apply { extendsFrom(getByName("common")) }

    create("shadowBundle") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

repositories {
    maven("https://maven.neoforged.net/releases") { name = "NeoForged" }
    maven("https://maven.architectury.dev/") { name = "Architectury" }
    mavenCentral()
}

dependencies {
    add("neoForge", "net.neoforged:neoforge:${rootProject.findProperty("neoforge_version")}")

    add("modImplementation", "dev.architectury:architectury-neoforge:${rootProject.findProperty("architectury_api_version")}")

    add("modImplementation", "maven.modrinth:midnightlib:${rootProject.findProperty("midnightlib_version")}-neoforge")

    (add("common", project(mapOf("path" to ":common", "configuration" to "namedElements"))) as ProjectDependency)
        .isTransitive = false

    add("shadowBundle", project(mapOf("path" to ":common", "configuration" to "transformProductionNeoForge")))
}

tasks.named<Copy>("processResources") {
    inputs.property("version", project.version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(mapOf("version" to inputs.properties["version"]))
    }
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(project.configurations.getByName("shadowBundle"))
    archiveClassifier.set("dev-shadow")
}

// Remap the shaded jar via Loom
tasks.named<RemapJarTask>("remapJar") {
    inputFile.set(tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
}
