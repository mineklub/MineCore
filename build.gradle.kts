group = "dk.minecore"
version = "0.0.4"

repositories {
    mavenCentral()
}

plugins {
    id("java")
    alias(libs.plugins.shadow)
    alias(libs.plugins.spotless)
}

subprojects {
    plugins.apply("java")
    plugins.apply("com.diffplug.spotless")

    spotless {
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

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.build {
        dependsOn(tasks.spotlessCheck)
    }
}
