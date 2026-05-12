plugins {
    id("java-library")
    alias(libs.plugins.resource.factory.velocity)
    alias(libs.plugins.shadow)
}

group = rootProject.group
version = rootProject.version

velocityPluginJson {
    main = "dk.mineclub.minecore.internal.InternalPlugin"
    authors = listOf("MineClub")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

dependencies {
    compileOnly(libs.velocity)
    compileOnly(libs.jedis)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.velocity)
}
