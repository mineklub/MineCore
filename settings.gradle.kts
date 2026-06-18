pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(org.gradle.api.initialization.resolve.RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "papermc"
        }
        maven("https://repo.skriptlang.org/releases") {
            name = "skriptlang"
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

sequenceOf("paper").forEach {
    val name = "platform-$it"
    if (file("platform/$it").exists()) {
        include(name)
        project(":$name").projectDir = file("platform/$it")
    }
}