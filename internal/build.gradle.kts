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
    dependency("packetevents")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

dependencies {
    implementation(libs.gson)
    compileOnly(libs.packetevents.api)
    compileOnly(libs.velocity)
    compileOnly(libs.jedis)
    compileOnly(libs.lombok)
    implementation(libs.okhttp)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.velocity)
}
