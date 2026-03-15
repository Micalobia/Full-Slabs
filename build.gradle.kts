import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("dev.architectury.loom") version "1.13-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("com.gradleup.shadow") version "8.3.6" apply false
    id("me.modmuss50.mod-publish-plugin") version "1.1.0" apply false
}

architectury {
    minecraft = project.findProperty("minecraft_version") as String
}

allprojects {
    group = rootProject.findProperty("maven_group") as String
    version = rootProject.findProperty("mod_version") as String

    configurations.configureEach {
        resolutionStrategy {
            force("dev.architectury:architectury-transformer:5.2.88")
        }
    }
}

subprojects {
    apply(plugin = "base")
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    extensions.configure<BasePluginExtension> {
        archivesName.set("${rootProject.findProperty("archives_name")}-${project.name}")
    }

    repositories {
        maven(url = "https://maven.terraformersmc.com/releases")
        maven(url = "https://maven.parchmentmc.org")
        maven(url = "https://api.modrinth.com/maven")
    }

    val loom = extensions.getByName("loom") as LoomGradleExtensionAPI

    dependencies {
        add("minecraft", "net.minecraft:minecraft:${rootProject.findProperty("minecraft_version")}")

        @Suppress("UnstableApiUsage")
        add("mappings", loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${rootProject.findProperty("minecraft_version")}:${rootProject.findProperty("parchment_version")}@zip")
        })
    }

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(21)
    }

    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                val baseExt = extensions.getByType(BasePluginExtension::class.java)
                artifactId = baseExt.archivesName.get()
                from(components["java"])
            }
        }
        repositories { }
    }
}