plugins {
    id("java-library")
}

group = rootProject.group
version = rootProject.version

val targetJavaVersion = rootProject.extra["targetJavaVersion"] as Int
val paperApiVersion = rootProject.extra["paperApiVersion"] as String

dependencies {
    implementation(project(":hooks"))
    compileOnly(project(":api"))
    compileOnly(project(":platform-paper"))
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    compileOnly(libs.skriptnew)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(targetJavaVersion)
}
