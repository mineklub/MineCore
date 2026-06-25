import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.WriteProperties

plugins {
    id("java-library")
    alias(libs.plugins.shadow)
}

extra["platformModuleLabel"] = "platform-paper"
extra["includeApiInShadow"] = false
apply(from = rootProject.file("platform/shared-platform.gradle"))

val loaderLibrariesCsv =
        listOf(
                        "io.socket:socket.io-client:${libs.versions.socketclient.get()}",
                        "com.google.code.gson:gson:${libs.versions.gson.get()}",
                        "com.google.guava:guava:${libs.versions.guava.get()}",
                        "com.squareup.okhttp3:okhttp-jvm:${libs.versions.okhttp.get()}",
                        "org.json:json:${libs.versions.json.get()}")
                .joinToString(",")

val generateLoaderLibrariesProperties =
        tasks.register<WriteProperties>("generateLoaderLibrariesProperties") {
            destinationFile =
                    layout.buildDirectory
                            .file("generated/resources/minecore-loader-libraries-generated.properties")
                            .get()
                            .asFile
            encoding = "UTF-8"
            property("libraries", loaderLibrariesCsv)
        }

tasks.processResources {
    from(generateLoaderLibrariesProperties)
}

@Suppress("UNCHECKED_CAST")
val additionalClassifiersByJavaVersion =
        extra["additionalPlatformClassifiersByJavaVersion"] as Map<Int, String>
val targetJavaVersion = rootProject.extra["targetJavaVersion"] as Int
@Suppress("UNCHECKED_CAST")
val hooksJarTaskForTarget = extra["hooksJarTaskForTarget"] as org.gradle.api.tasks.TaskProvider<Jar>
@Suppress("UNCHECKED_CAST")
val commonJarTaskForTarget = extra["commonJarTaskForTarget"] as org.gradle.api.tasks.TaskProvider<Jar>

additionalClassifiersByJavaVersion.forEach { (javaVersion, classifier) ->
    val hooksJarTaskForJavaVersion = project(":hooks").tasks.named<Jar>("jarJava$javaVersion")
    val commonJarTaskForJavaVersion = project(":platform-common").tasks.named<Jar>("jarJava$javaVersion")
    val compileTask = tasks.named<JavaCompile>("compileJava$javaVersion")

    tasks.register<ShadowJar>("shadowJarJava$javaVersion") {
        description = "Builds platform-paper shaded jar targeting Java $javaVersion."
        archiveClassifier.set("$classifier-all")
        archiveVersion.set("")
        from(compileTask.flatMap { it.destinationDirectory })
        from({ zipTree(hooksJarTaskForJavaVersion.get().archiveFile.get().asFile) })
        from({ zipTree(commonJarTaskForJavaVersion.get().archiveFile.get().asFile) })
        from(tasks.named("processResources"))
        configurations = listOf(project.configurations.runtimeClasspath.get())
        dependsOn(
                compileTask,
                hooksJarTaskForJavaVersion,
                commonJarTaskForJavaVersion,
                tasks.named("processResources"))
    }
}

tasks.withType<ShadowJar> {
    exclude("META-INF/**")
    minimize {
        exclude(dependency("${rootProject.group}:hooks:.*"))
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("jvm${targetJavaVersion}-all")
    from({ zipTree(hooksJarTaskForTarget.get().archiveFile.get().asFile) })
    from({ zipTree(commonJarTaskForTarget.get().archiveFile.get().asFile) })
    dependsOn(hooksJarTaskForTarget, commonJarTaskForTarget)
}

tasks.named("assemble") {
    dependsOn(additionalClassifiersByJavaVersion.keys.map { "shadowJarJava$it" })
}
