plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.21"
}

group = "app"
version = ""

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("kotlinx.coroutines.io.parallelism", "512")
}

tasks.test {
    useJUnitPlatform()
}
