import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("java-library")
}

group = rootProject.group
version = rootProject.version

val paperApiVersion = rootProject.extra["paperApiVersion"] as String
val targetJavaVersion = rootProject.extra["targetJavaVersion"] as Int

dependencies {
    compileOnly(project(":api"))
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.okhttp)
    compileOnly(libs.socketclient)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(targetJavaVersion)
}

val additionalCommonClassifiersByJavaVersion: LinkedHashMap<Int, String> =
        linkedMapOf(
                8 to "jvm8",
                11 to "jvm11",
                17 to "jvm17",
                21 to "jvm21",
                25 to "jvm25")

additionalCommonClassifiersByJavaVersion.forEach { (javaVersion, classifier) ->
    val compileTask = tasks.register<JavaCompile>("compileJava$javaVersion") {
        description = "Compiles platform-common classes targeting Java $javaVersion."
        source = sourceSets.main.get().allJava
        classpath = sourceSets.main.get().compileClasspath
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
    }

    tasks.register<Jar>("jarJava$javaVersion") {
        description = "Builds platform-common jar targeting Java $javaVersion."
        archiveClassifier.set(classifier)
        from(compileTask.flatMap { it.destinationDirectory })
        from(tasks.named("processResources"))
        dependsOn(compileTask, tasks.named("processResources"))
    }
}

tasks.named("assemble") {
    dependsOn(additionalCommonClassifiersByJavaVersion.keys.map { "jarJava$it" })
}
