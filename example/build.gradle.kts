import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("java-library")
    alias(libs.plugins.shadow)
}

group = rootProject.group
version = rootProject.version

val targetJavaVersion = rootProject.extra["targetJavaVersion"] as Int
val paperApiVersion = rootProject.extra["paperApiVersion"] as String

dependencies {
    implementation(project(":api"))
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(targetJavaVersion)
}

tasks.named<ProcessResources>("processResources") {
    val pluginResourceTokens = mapOf("version" to project.version.toString())
    inputs.properties(pluginResourceTokens)
    filesMatching("plugin.yml") {
        expand(pluginResourceTokens)
    }
}

tasks.withType<ShadowJar> {
    exclude("META-INF/**")
    minimize()
}
