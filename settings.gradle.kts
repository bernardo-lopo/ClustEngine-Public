pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
        kotlin("jvm") version "2.1.20"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "ClustEngine"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("awssdk") {
            from("aws.sdk.kotlin:version-catalog:1.4.48")
        }
    }
}

include("core")
include("core")
include("providers")
include("providers:aws")
include("providers:openstack")
include("tester")
include("tui")
include("app")
