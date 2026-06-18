plugins {
    id("java-library")
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":hooks"))
    compileOnly(project(":api"))
    compileOnly(project(":platform-paper"))
    compileOnly(libs.paper)
    compileOnly(libs.skriptnew)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}
