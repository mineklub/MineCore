plugins {
    id("java-library")
    id("xyz.jpenilla.resource-factory-velocity-convention") version "1.3.1"
    alias(libs.plugins.shadow)
}

group = rootProject.group
version = rootProject.version

velocityPluginJson {
    main = "dk.mineclub.minecore.internal.InternalPlugin"
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)
}
