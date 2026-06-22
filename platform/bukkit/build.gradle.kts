import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("java-library")
    alias(libs.plugins.shadow)
}

extra["platformModuleLabel"] = "platform-bukkit"
extra["includeApiInShadow"] = true
apply(from = rootProject.file("platform/shared-platform.gradle"))

dependencies {
    // Runtime deps shaded directly into the jar (no Paper PluginLoader)
    implementation(libs.socketclient) {
        exclude(group = "org.json", module = "json")
        exclude(group = "com.squareup.okio", module = "okio")
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }
    implementation(libs.gson)
    implementation(libs.guava)
    implementation(libs.okhttp)
    implementation(libs.json)
}

tasks.withType<ShadowJar> {
    exclude("META-INF/**")
    minimize {
        exclude(dependency("${rootProject.group}:hooks:.*"))
    }
    minimize {
        exclude(dependency("io.socket:socket.io-client:.*"))
        exclude(dependency("com.google.code.gson:gson:.*"))
        exclude(dependency("com.google.guava:guava:.*"))
        exclude(dependency("com.squareup.okhttp3:okhttp-jvm:.*"))
        exclude(dependency("com.squareup.okio:okio:.*"))
        exclude(dependency("org.json:json:.*"))
    }
}

@Suppress("UNCHECKED_CAST")
val additionalClassifiersByJavaVersion =
        extra["additionalPlatformClassifiersByJavaVersion"] as Map<Int, String>
val targetJavaVersion = rootProject.extra["targetJavaVersion"] as Int
@Suppress("UNCHECKED_CAST")
val hooksJarTaskForTarget = extra["hooksJarTaskForTarget"] as org.gradle.api.tasks.TaskProvider<Jar>
@Suppress("UNCHECKED_CAST")
val apiJarTaskForTarget = extra["apiJarTaskForTarget"] as org.gradle.api.tasks.TaskProvider<Jar>
@Suppress("UNCHECKED_CAST")
val commonJarTaskForTarget = extra["commonJarTaskForTarget"] as org.gradle.api.tasks.TaskProvider<Jar>

additionalClassifiersByJavaVersion.forEach { (javaVersion, classifier) ->
    val hooksJarTaskForJavaVersion = project(":hooks").tasks.named<Jar>("jarJava$javaVersion")
    val apiJarTaskForJavaVersion = project(":api").tasks.named<Jar>("jarJava$javaVersion")
    val commonJarTaskForJavaVersion = project(":platform-common").tasks.named<Jar>("jarJava$javaVersion")
    val compileTask = tasks.named<JavaCompile>("compileJava$javaVersion")

    tasks.register<ShadowJar>("shadowJarJava$javaVersion") {
        description = "Builds platform-bukkit shaded jar targeting Java $javaVersion."
        archiveClassifier.set("$classifier-all")
        from(compileTask.flatMap { it.destinationDirectory })
        from({ zipTree(hooksJarTaskForJavaVersion.get().archiveFile.get().asFile) })
        from({ zipTree(apiJarTaskForJavaVersion.get().archiveFile.get().asFile) })
        from({ zipTree(commonJarTaskForJavaVersion.get().archiveFile.get().asFile) })
        from(tasks.named("processResources"))
        configurations = listOf(project.configurations.runtimeClasspath.get())
        dependsOn(
                compileTask,
                hooksJarTaskForJavaVersion,
                apiJarTaskForJavaVersion,
                commonJarTaskForJavaVersion,
                tasks.named("processResources"))
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("jvm${targetJavaVersion}-all")
    from({ zipTree(hooksJarTaskForTarget.get().archiveFile.get().asFile) })
    from({ zipTree(apiJarTaskForTarget.get().archiveFile.get().asFile) })
    from({ zipTree(commonJarTaskForTarget.get().archiveFile.get().asFile) })
    dependsOn(hooksJarTaskForTarget, apiJarTaskForTarget, commonJarTaskForTarget)
}

tasks.named("assemble") {
    dependsOn(additionalClassifiersByJavaVersion.keys.map { "shadowJarJava$it" })
}
