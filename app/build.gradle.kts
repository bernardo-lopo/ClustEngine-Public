plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "apps"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val commonRuntime by configurations.creating
val awsRuntime by configurations.creating { extendsFrom(commonRuntime) }
val openstackRuntime by configurations.creating { extendsFrom(commonRuntime) }

configurations.implementation.get().extendsFrom(awsRuntime, openstackRuntime)

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":core"))
    implementation(project(":gui"))
    implementation(project(":providers:aws"))
    implementation(project(":providers:openstack"))

    add("commonRuntime", project(":core"))
    add("commonRuntime", project(":tui"))
    add("commonRuntime", project(":tester"))
    add("commonRuntime", "org.slf4j:slf4j-simple:2.0.16")
    add("commonRuntime", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    add("awsRuntime", project(":providers:aws"))
    add("awsRuntime", platform("aws.sdk.kotlin:bom:1.3.112"))
    add("awsRuntime", "aws.sdk.kotlin:ec2")

    add("openstackRuntime", project(":providers:openstack"))

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "apps.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
            )
            packageName = "ClustEngine"
            macOS {
                bundleID = "com.clustengine.app"
                packageVersion = "1.0.0"
                packageBuildVersion = "1.0.0"
            }
        }
    }
}

val outputNameOpenStackTester = "OpenStackTester"
val outputNameAWSTester = "AWSTester"
val outputNameGeneralEnv = "GeneralConsoleAPPEnv"
val outputNameGuiApp = "GuiAPP"

fun registerFatJar(
    taskName: String,
    baseName: String,
    mainClassName: String,
    excludePattern: String?,
    config: Configuration,
) {
    tasks.register<Jar>(taskName) {
        dependsOn("classes")

        manifest {
            attributes["Main-Class"] = mainClassName
            attributes["Implementation-Title"] = baseName
            attributes["Implementation-Version"] = archiveVersion.get()
            attributes["Multi-Release"] = "true"
        }

        archiveBaseName.set(baseName)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(sourceSets.main.get().output) {
            if (excludePattern != null) {
                exclude(excludePattern)
            }
        }

        from({
            config.map { file ->
                if (file.isDirectory) file else zipTree(file)
            }
        })

        destinationDirectory.set(layout.projectDirectory.dir(".."))
    }
}

registerFatJar("OpenStackTesterJar", outputNameOpenStackTester, "apps.OpenStackTesterAPPKt", "apps/AWS*.class", openstackRuntime)
registerFatJar("AWSTesterJar", outputNameAWSTester, "apps.AWSTesterAPPKt", "apps/OpenStack*.class", awsRuntime)
registerFatJar(
    "GeneralConsoleAPPEnvJar",
    outputNameGeneralEnv,
    "apps.GeneralConsoleAPPEnvKt",
    "apps/GeneralConsoleAPPEnv*.class",
    awsRuntime,
)
registerFatJar("GuiAppJar", outputNameGuiApp, "apps.GUIAppKt", null, configurations.runtimeClasspath.get())

tasks.build {
    dependsOn(
        "AWSTesterJar",
        "OpenStackTesterJar",
        "GeneralConsoleAPPEnvJar",
        "GuiAppJar",
    )
}

tasks.clean {
    doLast {
        file("../$outputNameOpenStackTester.jar").delete()
        file("../$outputNameAWSTester.jar").delete()
        file("../$outputNameGeneralEnv.jar").delete()
        file("../$outputNameGuiApp.jar").delete()
        file("../Tester.jar").delete()
    }
}

afterEvaluate {
    val runtimeAttributes = configurations.runtimeClasspath.get().attributes
    listOf(commonRuntime, awsRuntime, openstackRuntime).forEach { config ->
        config.attributes {
            runtimeAttributes.keySet().forEach { key ->
                @Suppress("UNCHECKED_CAST")
                attribute(key as Attribute<Any>, runtimeAttributes.getAttribute(key)!!)
            }
        }
    }
}
