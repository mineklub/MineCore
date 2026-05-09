group = "dk.minecore"
version = "1.0.0"

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
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat("1.24.0").aosp()
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
    }

    tasks.named("check") {
        dependsOn("spotlessCheck")
    }
}
