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
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
    implementation(platform("aws.sdk.kotlin:bom:1.3.112"))
    implementation("software.amazon.awssdk:ssm:2.25.3")
    implementation("aws.sdk.kotlin:ec2")
    implementation("aws.sdk.kotlin:ssm")
    implementation("aws.sdk.kotlin:secretsmanager")
    implementation("aws.smithy.kotlin:http-client-engine-okhttp")
    implementation("aws.smithy.kotlin:http-client-engine-crt")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
