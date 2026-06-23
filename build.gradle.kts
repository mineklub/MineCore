group = "dk.mineclub.minecore"
version = providers.gradleProperty("minecoreVersion").orElse("0.1.0").get()

val targetJavaVersion =
        providers.gradleProperty("minepayJavaVersion")
                .map(String::toInt)
                .orElse(25)
                .get()

val paperApiVersion =
        when (targetJavaVersion) {
            17 -> "1.20.4-R0.1-SNAPSHOT"
            21 -> "1.21.11-R0.1-SNAPSHOT"
            else -> "26.2.build.+"
        }

require(targetJavaVersion == 17 || targetJavaVersion == 21 || targetJavaVersion == 25) {
    "minepayJavaVersion must be either 17, 21 or 25."
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