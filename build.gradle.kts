import java.nio.file.Files
import java.nio.file.StandardCopyOption

group = "dk.minecore"
version = "1.0.0"

val targetJavaVersion =
        providers.gradleProperty("minepayJavaVersion")
                .map(String::toInt)
                .orElse(25)
                .get()

val paperApiVersion = if (targetJavaVersion == 21) "1.21.11-R0.1-SNAPSHOT" else "26.1.2.build.+"

require(targetJavaVersion == 21 || targetJavaVersion == 25) {
    "minepayJavaVersion must be either 21 or 25."
}

extra["targetJavaVersion"] = targetJavaVersion
extra["paperApiVersion"] = paperApiVersion

plugins {
    base
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.spotless) apply false
}

subprojects {
    plugins.apply("java-library")
    plugins.apply("com.diffplug.spotless")

    group = rootProject.group
    version = rootProject.version

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
        }
    }

    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat("1.35.0").aosp()
            targetExclude("build/generated/**/*")
        }
        kotlinGradle {
            endWithNewline()
            leadingTabsToSpaces(4)
            trimTrailingWhitespace()
        }
        yaml {
            prettier().config(mapOf("tabWidth" to 4))
            target("src/**/*.yml")
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
    }

    tasks.named("check") {
        dependsOn("spotlessCheck")
    }
}

tasks.register("buildPlatformPaperJava21And25") {
    group = "build"
    description = "Builds :platform-paper shadow jar for Java 21 and 25 and stores both outputs."
    notCompatibleWithConfigurationCache("Runs nested Gradle builds with different minepayJavaVersion values.")

    doLast {
        val gradlew = if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) {
            "gradlew.bat"
        } else {
            "./gradlew"
        }

        fun runGradle(vararg args: String) {
            val command = mutableListOf(gradlew)
            command.addAll(args)
            val exitCode = ProcessBuilder(command).directory(rootDir).inheritIO().start().waitFor()
            if (exitCode != 0) {
                throw GradleException("Command failed (${command.joinToString(" ")}) with exit code $exitCode")
            }
        }

        val libsDir = rootDir.resolve("platform/paper/build/libs")
        val defaultShadowJar = libsDir.resolve("platform-paper-${project.version}-all.jar")
        val java21ShadowJar = libsDir.resolve("platform-paper-${project.version}-jvm21-all.jar")
        val java25ShadowJar = libsDir.resolve("platform-paper-${project.version}-jvm25-all.jar")

        runGradle(":platform-paper:shadowJar", "-PminepayJavaVersion=21", "-x", "test")
        Files.copy(defaultShadowJar.toPath(), java21ShadowJar.toPath(), StandardCopyOption.REPLACE_EXISTING)

        runGradle(":platform-paper:shadowJar", "-PminepayJavaVersion=25", "-x", "test")
        Files.copy(defaultShadowJar.toPath(), java25ShadowJar.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}





