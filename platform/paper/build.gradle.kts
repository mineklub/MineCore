import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.WriteProperties

plugins {
    id("java-library")
    alias(libs.plugins.shadow)
}

group = rootProject.group
version = rootProject.version

val targetJavaVersion = rootProject.extra["targetJavaVersion"] as Int
val paperApiVersion = rootProject.extra["paperApiVersion"] as String

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    compileOnly(project(":api"))
    implementation(project(":hooks"))
}

val loaderLibrariesCsv =
        listOf(
                        "io.socket:socket.io-client:${libs.versions.socketclient.get()}",
                        "com.google.code.gson:gson:${libs.versions.gson.get()}",
                        "com.google.guava:guava:${libs.versions.guava.get()}",
                        "com.squareup.okhttp3:okhttp-jvm:${libs.versions.okhttp.get()}",
                        "org.json:json:${libs.versions.json.get()}")
                .joinToString(",")

val minecoreJitpackVersion =
        providers.gradleProperty("minecoreJitpackVersion").orElse("firsttry-SNAPSHOT").get()

val generateLoaderLibrariesProperties =
        tasks.register<WriteProperties>("generateLoaderLibrariesProperties") {
            destinationFile =
                    layout.buildDirectory
                            .file("generated/resources/minecore-loader-libraries-generated.properties")
                            .get()
                            .asFile
            encoding = "UTF-8"
            property("libraries", loaderLibrariesCsv)
            property(
                    "minecoreDependency",
                    "com.github.mineklub.MineCore:api:$minecoreJitpackVersion")
        }

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

extensions.configure<SpotlessExtension> {
    yaml {
        targetExclude("src/main/resources/plugin.yml", "src/main/resources/config.yml")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(targetJavaVersion)
}

tasks.processResources {
    from(generateLoaderLibrariesProperties)
}


tasks.withType<ShadowJar> {
    exclude("META-INF/**")
    minimize {
        exclude(dependency("${rootProject.group}:hooks:.*"))
    }
}
