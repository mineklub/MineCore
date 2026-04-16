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
    }
}

rootProject.name = "MineCore"

include("common")
project(":common").projectDir = file("common")
include("internal")
project(":internal").projectDir = file("internal")

sequenceOf("paper").forEach {
    val name = "platform-$it"
    if (file("platform/$it").exists()) {
        include(name)
        project(":$name").projectDir = file("platform/$it")
    }
}
