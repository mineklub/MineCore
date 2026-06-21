plugins {
    id("java-library")
}

group = rootProject.group
version = rootProject.version

val targetJavaVersion = rootProject.extra["targetJavaVersion"] as Int
val paperApiVersion = rootProject.extra["paperApiVersion"] as String
val hooksJarTaskForTarget = project(":hooks").tasks.named<Jar>("jarJava$targetJavaVersion")
val hooksJarForTarget = files(hooksJarTaskForTarget.flatMap { it.archiveFile }).builtBy(hooksJarTaskForTarget)

dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":platform-paper"))
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    compileOnly(libs.skript213)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.named<JavaCompile>("compileJava") {
    classpath = sourceSets.main.get().compileClasspath + hooksJarForTarget
    dependsOn(hooksJarTaskForTarget)
}

val additionalHookClassifiersByJavaVersion: LinkedHashMap<Int, String> =
    linkedMapOf(
        17 to "jvm17",
        21 to "jvm21",
        25 to "jvm25")

additionalHookClassifiersByJavaVersion.forEach { (javaVersion, classifier) ->
    val hooksJarTaskForJavaVersion = project(":hooks").tasks.named<Jar>("jarJava$javaVersion")
    val hooksJarForJavaVersion = files(hooksJarTaskForJavaVersion.flatMap { it.archiveFile }).builtBy(hooksJarTaskForJavaVersion)

    val compileTask = tasks.register<JavaCompile>("compileJava$javaVersion") {
        description = "Compiles hooks classes targeting Java $javaVersion."
        source = sourceSets.main.get().allJava
        classpath = sourceSets.main.get().compileClasspath + hooksJarForJavaVersion
        destinationDirectory.set(layout.buildDirectory.dir("classes/java/java$javaVersion"))
        options.encoding = "UTF-8"
        if (javaVersion == 17) {
            // Avoid --release 17 here because upstream Skript signatures reference Java 21 types.
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

    tasks.register<Jar>("jarJava$javaVersion") {
        description = "Builds hooks jar targeting Java $javaVersion."
        archiveClassifier.set(classifier)
        from(compileTask.flatMap { it.destinationDirectory })
        from(tasks.named("processResources"))
        dependsOn(compileTask, tasks.named("processResources"))
    }
}

tasks.named("assemble") {
    dependsOn(additionalHookClassifiersByJavaVersion.keys.map { "jarJava$it" })
}
