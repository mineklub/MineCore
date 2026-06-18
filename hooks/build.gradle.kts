plugins {
    id("java-library")
}

group = rootProject.group
version = rootProject.version

dependencies {
    compileOnly(libs.paper)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}
