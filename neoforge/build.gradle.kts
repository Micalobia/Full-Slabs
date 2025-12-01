// neoforge
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("com.gradleup.shadow")
    id("me.modmuss50.mod-publish-plugin")
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
    // Required
    neoForge("net.neoforged:neoforge:${rootProject.findProperty("neoforge_version")}")
    modImplementation("dev.architectury:architectury-neoforge:${rootProject.findProperty("architectury_api_version")}")
    modImplementation("maven.modrinth:midnightlib:${rootProject.findProperty("midnightlib_version")}-neoforge")

    // Misc
    add("common", project(path = ":common", configuration = "namedElements"))
        .also { (it as ProjectDependency).isTransitive = false }
    add("shadowBundle", project(path = ":common", configuration = "transformProductionNeoForge"))
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

tasks.named<RemapJarTask>("remapJar") {
    inputFile.set(tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
}

publishMods {
    file.set(tasks.remapJar.get().archiveFile)
    type.set(STABLE)
    modLoaders.add("neoforge")
    changelog.set("Automatic Github release for version ${project.version}")
    displayName.set("Full Slabs ${project.version} NeoForge")

    modrinth {
        projectId = rootProject.findProperty("modrinth_id") as String
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.add(rootProject.findProperty("minecraft_version") as String)
    }

    curseforge {
        projectId = rootProject.findProperty("curseforge_id") as String
        accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
        minecraftVersions.add(rootProject.findProperty("minecraft_version") as String)
    }
}