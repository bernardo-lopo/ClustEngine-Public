plugins {
    kotlin("jvm")
}

group = "app"
version = ""

repositories {
    mavenCentral()
}
val commonRuntime: Configuration? by configurations.creating
val awsRuntime: Configuration? by configurations.creating { extendsFrom(commonRuntime) }
val openstackRuntime: Configuration? by configurations.creating { extendsFrom(commonRuntime) }

configurations.implementation.get().extendsFrom(awsRuntime, openstackRuntime)

dependencies {
    commonRuntime?.invoke(project(":core"))
    commonRuntime?.invoke(project(":tui"))
    commonRuntime?.invoke(project(":tester"))
    commonRuntime?.invoke("org.slf4j:slf4j-simple:2.0.16")
    commonRuntime?.invoke("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    awsRuntime?.invoke(project(":providers:aws"))
    awsRuntime?.invoke(platform("aws.sdk.kotlin:bom:1.3.112"))
    awsRuntime?.invoke("aws.sdk.kotlin:ec2")

    openstackRuntime?.invoke(project(":providers:openstack"))

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

val outputNameOpenStackConsoleAPP = "OpenStackConsoleAPP"
val outputNameAWSConsoleAPP = "AWSConsoleAPP"
val outputNameOpenStackTester = "OpenStackTester"
val outputNameAWSTester = "AWSTester"

tasks.register<Jar>("OpenStackConsoleAppJar") {
    dependsOn("classes")

    manifest {
        attributes["Main-Class"] = "apps.OpenStackConsoleAPPKt"
        attributes["Implementation-Title"] = "OpenStack Console APP Jar"
        attributes["Implementation-Version"] = archiveVersion.get()
        attributes["Multi-Release"] = "true"
    }

    archiveBaseName.set(outputNameOpenStackConsoleAPP)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output) {
        exclude("apps/AWS*.class")
    }

    from({
        openstackRuntime?.map { file ->
            if (file.isDirectory) file else zipTree(file)
        }
    })

    destinationDirectory.set(layout.projectDirectory.dir(".."))
}

tasks.register<Jar>("AWSConsoleAPPJar") {
    dependsOn("classes")

    manifest {
        attributes["Main-Class"] = "apps.AWSConsoleAPPKt"
        attributes["Implementation-Title"] = "AWS Console APP Jar"
        attributes["Implementation-Version"] = archiveVersion.get()
        attributes["Multi-Release"] = "true"
    }

    archiveBaseName.set(outputNameAWSConsoleAPP)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output) {
        exclude("apps/OpenStack*.class")
    }

    from({
        awsRuntime?.map { file ->
            if (file.isDirectory) file else zipTree(file)
        }
    })

    destinationDirectory.set(layout.projectDirectory.dir(".."))
}

tasks.register<Jar>("OpenStackTesterJar") {
    dependsOn("classes")

    manifest {
        attributes["Main-Class"] = "apps.OpenStackTesterAPPKt"
        attributes["Implementation-Title"] = "OpenStack Tester APP Jar"
        attributes["Implementation-Version"] = archiveVersion.get()
        attributes["Multi-Release"] = "true"
    }

    archiveBaseName.set(outputNameOpenStackTester)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output) {
        exclude("apps/AWS*.class")
    }

    from({
        openstackRuntime?.map { file ->
            if (file.isDirectory) file else zipTree(file)
        }
    })

    destinationDirectory.set(layout.projectDirectory.dir(".."))
}

tasks.register<Jar>("AWSTesterJar") {
    dependsOn("classes")

    manifest {
        attributes["Main-Class"] = "apps.AWSTesterAPPKt"
        attributes["Implementation-Title"] = "AWS Tester APP Jar"
        attributes["Implementation-Version"] = archiveVersion.get()
        attributes["Multi-Release"] = "true"
    }

    archiveBaseName.set(outputNameAWSTester)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output) {
        exclude("apps/OpenStack*.class")
    }

    from({
        awsRuntime?.map { file ->
            if (file.isDirectory) file else zipTree(file)
        }
    })

    destinationDirectory.set(layout.projectDirectory.dir(".."))
}

tasks.build {
    dependsOn("AWSTesterJar", "OpenStackTesterJar", "AWSConsoleAPPJar", "OpenStackConsoleAppJar")
}

tasks.clean {
    doLast {
        file("../$outputNameOpenStackConsoleAPP.jar").delete()
        file("../$outputNameAWSConsoleAPP.jar").delete()
        file("../$outputNameOpenStackTester.jar").delete()
        file("../$outputNameAWSTester.jar").delete()

        file("../ConsoleAPP.jar").delete()
        file("../Tester.jar").delete()
    }
}
