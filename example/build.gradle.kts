import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    alias(libs.plugins.shadow)
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":api"))
    compileOnly(libs.paper)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withJavadocJar()
    withSourcesJar()
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release.set(25)
    }
    withType<ShadowJar> {
        exclude("META-INF/**")
        minimize()
    }
}
