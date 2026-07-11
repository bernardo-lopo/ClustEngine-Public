plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    id("org.jetbrains.compose") version "1.7.3"
}

repositories {
    mavenCentral()
}

tasks.named<Delete>("clean") {
    delete(rootProject.file("tmp"))
    delete(rootProject.file("active_instances.json"))
    delete(rootProject.file(".clustenginesettings"))

    delete(rootProject.file("app/saved_clusters"))
    delete(rootProject.file("app/tmp"))
    delete(rootProject.file("app/active_instances.json"))

    delete(
        fileTree(rootProject.projectDir) {
            include("*.jar")
        },
    )
}

subprojects {
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
    }
}
