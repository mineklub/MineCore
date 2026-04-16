plugins {
    id("java-library")
    alias(libs.plugins.resource.factory.velocity)
    alias(libs.plugins.shadow)
}

group = rootProject.group
version = rootProject.version

velocityPluginJson {
    main = "dk.mineclub.minecore.internal.InternalPlugin"
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
    annotationProcessor(libs.velocity)
    testImplementation(libs.junit.jupiter)
}
