import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.WriteProperties

plugins {
    id("java-library")
    alias(libs.plugins.shadow)
}

group = rootProject.group
version = rootProject.version

val targetJavaVersion = rootProject.extra["targetJavaVersion"] as Int
val paperApiVersion = rootProject.extra["paperApiVersion"] as String
val apiJarTaskForTarget = project(":api").tasks.named<Jar>("jarJava$targetJavaVersion")
val apiJarForTarget = files(apiJarTaskForTarget.flatMap { it.archiveFile }).builtBy(apiJarTaskForTarget)
val hooksJarTaskForTarget = project(":hooks").tasks.named<Jar>("jarJava$targetJavaVersion")
val hooksJarForTarget = files(hooksJarTaskForTarget.flatMap { it.archiveFile }).builtBy(hooksJarTaskForTarget)

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    compileOnly(apiJarForTarget)
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

tasks.named<JavaCompile>("compileJava") {
    classpath = sourceSets.main.get().compileClasspath + hooksJarForTarget
    dependsOn(hooksJarTaskForTarget)
}

tasks.processResources {
    from(generateLoaderLibrariesProperties)
}

val additionalPaperClassifiersByJavaVersion: LinkedHashMap<Int, String> =
        linkedMapOf(
                17 to "jvm17",
                21 to "jvm21",
                25 to "jvm25")

additionalPaperClassifiersByJavaVersion.forEach { (javaVersion, classifier) ->
    val hooksJarTaskForJavaVersion = project(":hooks").tasks.named<Jar>("jarJava$javaVersion")
    val hooksJarForJavaVersion = files(hooksJarTaskForJavaVersion.flatMap { it.archiveFile }).builtBy(hooksJarTaskForJavaVersion)

    val compileTask = tasks.register<JavaCompile>("compileJava$javaVersion") {
        description = "Compiles platform-paper classes targeting Java $javaVersion."
        source = sourceSets.main.get().allJava
        classpath = sourceSets.main.get().compileClasspath + hooksJarForJavaVersion
        destinationDirectory.set(layout.buildDirectory.dir("classes/java/java$javaVersion"))
        options.encoding = "UTF-8"
        options.annotationProcessorPath = sourceSets.main.get().annotationProcessorPath
        if (javaVersion == 17) {
            // Keep compatibility with classpaths that expose newer bytecode dependencies.
            options.release.set(null as Int?)
            sourceCompatibility = "17"
            targetCompatibility = "17"
        } else {
            options.release.set(javaVersion)
        }
        if (javaVersion > targetJavaVersion) {
            javaCompiler.set(
                    javaToolchains.compilerFor {
                        languageVersion.set(JavaLanguageVersion.of(javaVersion))
                    })
        }
        dependsOn(hooksJarTaskForJavaVersion)
    }

    tasks.register<ShadowJar>("shadowJarJava$javaVersion") {
        description = "Builds platform-paper shaded jar targeting Java $javaVersion."
        archiveClassifier.set("$classifier-all")
        from(compileTask.flatMap { it.destinationDirectory })
        from({ zipTree(hooksJarTaskForJavaVersion.get().archiveFile.get().asFile) })
        from(tasks.named("processResources"))
        configurations = listOf(project.configurations.runtimeClasspath.get())
        dependsOn(compileTask, hooksJarTaskForJavaVersion, tasks.named("processResources"))
    }
}
tasks.withType<ShadowJar> {
    exclude("META-INF/**")
    minimize {
        exclude(dependency("${rootProject.group}:hooks:.*"))
    }
}

tasks.named<ShadowJar>("shadowJar") {
    from({ zipTree(hooksJarTaskForTarget.get().archiveFile.get().asFile) })
    dependsOn(hooksJarTaskForTarget)
}

tasks.named("assemble") {
    dependsOn(additionalPaperClassifiersByJavaVersion.keys.map { "shadowJarJava$it" })
}
