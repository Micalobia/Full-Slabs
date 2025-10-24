import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

configurations {
    // common
    create("common") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }

    // extend standard classpaths
    named("compileClasspath") {
        extendsFrom(getByName("common"))
    }
    named("runtimeClasspath") {
        extendsFrom(getByName("common"))
    }
    // loom's dev config (exists when loom is applied)
    maybeCreate("developmentFabric").apply {
        extendsFrom(getByName("common"))
    }

    // shadow bundle
    create("shadowBundle") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

dependencies {
    add("modImplementation", "net.fabricmc:fabric-loader:${rootProject.findProperty("fabric_loader_version")}")
    add("modImplementation", "net.fabricmc.fabric-api:fabric-api:${rootProject.findProperty("fabric_api_version")}")
    add(
        "modImplementation",
        "dev.architectury:architectury-fabric:${rootProject.findProperty("architectury_api_version")}"
    )
    add("modImplementation", "com.terraformersmc:modmenu:${rootProject.findProperty("modmenu_version")}")
    add("modImplementation", "maven.modrinth:midnightlib:${rootProject.findProperty("midnightlib_version")}-fabric")

    // Compat
    add("modCompileOnly", "maven.modrinth:blockus:${rootProject.findProperty("blockus_version")}")

    // common(project(path: ':common', configuration: 'namedElements')) { transitive = false }
    (add("common", project(mapOf("path" to ":common", "configuration" to "namedElements"))) as ProjectDependency)
        .isTransitive = false

    // shadowBundle project(path: ':common', configuration: 'transformProductionFabric')
    add("shadowBundle", project(mapOf("path" to ":common", "configuration" to "transformProductionFabric")))
}

tasks.named<Copy>("processResources") {
    // keep this so Gradle knows to re-run when version changes
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to inputs.properties["version"]))
    }
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(project.configurations.getByName("shadowBundle"))
    archiveClassifier.set("dev-shadow")
}

// Loom remap of the shaded jar
tasks.named("remapJar") {
    // use the archive produced by shadowJar
    val shadow = tasks.named<ShadowJar>("shadowJar")
    inputs.files(shadow.map { it.archiveFile })
    doFirst {
        // for Loom's RemapJarTask, set inputFile property when task executes
        @Suppress("UNCHECKED_CAST")
        (this as org.gradle.api.Task).extensions.extraProperties["inputFile"] = shadow.get().archiveFile.get().asFile
    }
}
