// fabric
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

configurations {
    create("common") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }

    named("compileClasspath") { extendsFrom(getByName("common")) }
    named("runtimeClasspath") { extendsFrom(getByName("common")) }
    maybeCreate("developmentFabric").apply { extendsFrom(getByName("common")) }

    create("shadowBundle") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

repositories {
}

dependencies {
    // Required
    modImplementation("net.fabricmc:fabric-loader:${rootProject.findProperty("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.findProperty("fabric_api_version")}")
    modImplementation("dev.architectury:architectury-fabric:${rootProject.findProperty("architectury_api_version")}")
    modImplementation("maven.modrinth:midnightlib:${rootProject.findProperty("midnightlib_version")}-fabric")

    // Optional
    modImplementation("com.terraformersmc:modmenu:${rootProject.findProperty("modmenu_version")}")

    // Compat
    modCompileOnly("maven.modrinth:blockus:${rootProject.findProperty("blockus_version")}")
    modCompileOnly("maven.modrinth:mo-glass:${rootProject.findProperty("mo_glass_version")}")

    // Misc
    add("common", project(path = ":common", configuration = "namedElements"))
        .also { (it as ProjectDependency).isTransitive = false }
    add("shadowBundle", project(path = ":common", configuration = "transformProductionFabric"))
}

tasks.named<Copy>("processResources") {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
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