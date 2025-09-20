import org.gradle.api.JavaVersion
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.api.mappings.layered.spec.LayeredMappingSpecBuilder // <-- correct type

plugins {
    id("dev.architectury.loom") version "1.11-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("com.gradleup.shadow") version "8.3.6" apply false
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
        maven(url = "https://api.modrinth.com/maven")
    }

    val loom = extensions.getByName("loom") as LoomGradleExtensionAPI

    dependencies {
        add("minecraft", "net.minecraft:minecraft:${rootProject.findProperty("minecraft_version")}")

        add("mappings", loom.layered {
            mappings("net.fabricmc:yarn:${rootProject.findProperty("yarn_mappings")}:v2")
            mappings("dev.architectury:yarn-mappings-patch-neoforge:${rootProject.findProperty("yarn_mappings_patch_neoforge_version")}")
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
