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
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
