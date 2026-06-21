import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("java-library")
}

group = rootProject.group
version = rootProject.version

val hookPaperApiVersion = "1.20.4-R0.1-SNAPSHOT"

dependencies {
    compileOnly("io.papermc.paper:paper-api:$hookPaperApiVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

val additionalHookClassifiersByJavaVersion: LinkedHashMap<Int, String> =
        linkedMapOf(
                17 to "jvm17",
                21 to "jvm21",
                25 to "jvm25")

additionalHookClassifiersByJavaVersion.forEach { (javaVersion, classifier) ->
    val compileTask = tasks.register<JavaCompile>("compileJava$javaVersion") {
        description = "Compiles hook API classes targeting Java $javaVersion."
        source = sourceSets.main.get().allJava
        classpath = sourceSets.main.get().compileClasspath
        destinationDirectory.set(layout.buildDirectory.dir("classes/java/java$javaVersion"))
        options.encoding = "UTF-8"
        options.release.set(javaVersion)
        if (javaVersion > 17) {
            javaCompiler.set(
                    javaToolchains.compilerFor {
                        languageVersion.set(JavaLanguageVersion.of(javaVersion))
                    })
        }
    }

    tasks.register<Jar>("jarJava$javaVersion") {
        description = "Builds hook API jar targeting Java $javaVersion."
        archiveClassifier.set(classifier)
        from(compileTask.flatMap { it.destinationDirectory })
        from(tasks.named("processResources"))
        dependsOn(compileTask, tasks.named("processResources"))
    }
}

tasks.named("assemble") {
    dependsOn(additionalHookClassifiersByJavaVersion.keys.map { "jarJava$it" })
}
