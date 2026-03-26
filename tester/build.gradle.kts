plugins {
    kotlin("jvm")
}

group = "app"
version = ""

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
