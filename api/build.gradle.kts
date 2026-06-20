import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("java-library")
    alias(libs.plugins.shadow)
    alias(libs.plugins.maven.publish)
    signing
}

group = rootProject.group
version = rootProject.version

val additionalApiClassifiersByJavaVersion =
        linkedMapOf(
                8 to "jvm8",
                11 to "jvm11",
                17 to "jvm17",
                25 to "jvm25")

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
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


tasks {
    named<JavaCompile>("compileJava") {
        options.release.set(21)
    }
    withType<ShadowJar> {
        exclude("META-INF/**")
        minimize()
    }
}

val additionalApiJarTasks =
        additionalApiClassifiersByJavaVersion.map { (javaVersion, classifier) ->
            val compileTask = tasks.register<JavaCompile>("compileJava$javaVersion") {
                description = "Compiles API classes targeting Java $javaVersion."
                source = sourceSets.main.get().allJava
                classpath = sourceSets.main.get().compileClasspath
                destinationDirectory.set(layout.buildDirectory.dir("classes/java/java$javaVersion"))
                options.encoding = "UTF-8"
                options.release.set(javaVersion)
                options.annotationProcessorPath = sourceSets.main.get().annotationProcessorPath
                if (javaVersion > 21) {
                    javaCompiler.set(
                            javaToolchains.compilerFor {
                                languageVersion.set(JavaLanguageVersion.of(javaVersion))
                            })
                }
            }

            tasks.register<Jar>("jarJava$javaVersion") {
                description = "Builds an API jar targeting Java $javaVersion."
                archiveClassifier.set(classifier)
                from(compileTask)
                from(tasks.named("processResources"))
                dependsOn(compileTask, tasks.named("processResources"))
            }
        }

signing {
    val signingKeyId =
            providers.gradleProperty("signingInMemoryKeyId")
                    .orElse(providers.environmentVariable("SIGNING_KEY_ID"))
    val signingKey =
            providers.gradleProperty("signingInMemoryKey")
                    .orElse(providers.environmentVariable("SIGNING_KEY"))
    val signingPassword =
            providers.gradleProperty("signingInMemoryKeyPassword")
                    .orElse(providers.environmentVariable("SIGNING_PASSWORD"))

    if (signingKey.isPresent && signingPassword.isPresent) {
        useInMemoryPgpKeys(signingKeyId.orNull, signingKey.orNull, signingPassword.orNull)
    } else {
        useGpgCmd()
    }
}

mavenPublishing {
    publishToMavenCentral()
    val hasSigningKey =
            providers.gradleProperty("signingInMemoryKey").orNull != null
                    || providers.environmentVariable("SIGNING_KEY").orNull != null
    if (hasSigningKey) {
        signAllPublications()
    }
    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = SourcesJar.Sources(),
    ))
    pom {
        name = rootProject.name
        description = "Payment system for servers on mineclub.dk"
        inceptionYear = "2024"
        url = "https://mineclub.dk/"
        licenses {
            license {
                name = "GNU GENERAL PUBLIC LICENSE"
                url = "https://www.gnu.org/licenses/gpl-3.0.html"
            }
        }
        developers {
            developer {
                id = "mineclub"
                name = "MineClub"
                url = "https://github.com/mineklub/"
            }
        }
        scm {
            url = "https://github.com/mineklub/MineCore/"
            connection = "scm:git:git://github.com/mineklub/MineCore.git"
            developerConnection = "scm:git:ssh://git@github.com/mineklub/MineCore.git"
        }
    }
}

extensions.configure<PublishingExtension> {
    publications.withType(MavenPublication::class.java).configureEach {
        additionalApiJarTasks.forEach { artifact(it) }
    }
}
