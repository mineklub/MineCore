pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "papermc"
        }
        maven("https://repo.skriptlang.org/releases") {
            name = "skriptlang"
        }
        maven("https://repo.codemc.io/repository/maven-releases/") {
            name = "codemc"
        }
        maven("https://jitpack.io") {
            name = "jitpack"
        }
    }
}

rootProject.name = "MineCore"

include("api")
project(":api").projectDir = file("api")
include("internal")
project(":internal").projectDir = file("internal")
include("example")
project(":example").projectDir = file("example")
include("hooks")
project(":hooks").projectDir = file("hooks")

include("hooks:skript")
project(":hooks:skript").projectDir = file("hooks/skript")

include("hooks:skript213")
project(":hooks:skript213").projectDir = file("hooks/skript213")

include("hooks:skript295")
project(":hooks:skript295").projectDir = file("hooks/skript295")

include("hooks:skript22dev36")
project(":hooks:skript22dev36").projectDir = file("hooks/skript22dev36")

sequenceOf("common", "paper", "bukkit").forEach {
    val name = "platform-$it"
    if (file("platform/$it").exists()) {
        include(name)
        project(":$name").projectDir = file("platform/$it")
    }
}