import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.plugins.signing.Sign

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
                21 to "jvm21",
                25 to "jvm25")

data class ApiVariantArtifact(
        val javaVersion: Int,
        val classifier: String,
        val jarTask: org.gradle.api.tasks.TaskProvider<Jar>)

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
    withType<ShadowJar> {
        exclude("META-INF/**")
        minimize()
    }
}

val additionalApiArtifacts =
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

            val jarTask = tasks.register<Jar>("jarJava$javaVersion") {
                description = "Builds an API jar targeting Java $javaVersion."
                archiveClassifier.set(classifier)
                from(compileTask.map { it.destinationDirectory }) {
                    exclude("previous-compilation-data.bin")
                }
                from(tasks.named("processResources"))
                dependsOn(compileTask, tasks.named("processResources"))
            }

            ApiVariantArtifact(javaVersion = javaVersion, classifier = classifier, jarTask = jarTask)
        }

fun MavenPom.applyApiPomMetadata() {
    name.set(rootProject.name)
    description.set("Payment system for servers on mineclub.dk")
    inceptionYear.set("2024")
    url.set("https://mineclub.dk/")
    licenses {
        license {
            name.set("GNU GENERAL PUBLIC LICENSE")
            url.set("https://www.gnu.org/licenses/gpl-3.0.html")
        }
    }
    developers {
        developer {
            id.set("mineclub")
            name.set("MineClub")
            url.set("https://github.com/mineklub/")
        }
    }
    scm {
        url.set("https://github.com/mineklub/MineCore/")
        connection.set("scm:git:git://github.com/mineklub/MineCore.git")
        developerConnection.set("scm:git:ssh://git@github.com/mineklub/MineCore.git")
    }
}

signing {
    val signingKey =
            providers.gradleProperty("signingInMemoryKey")
                    .orElse(providers.environmentVariable("SIGNING_KEY"))
    val signingPassword =
            providers.gradleProperty("signingInMemoryKeyPassword")
                    .orElse(providers.environmentVariable("SIGNING_PASSWORD"))

    if (signingKey.isPresent && signingPassword.isPresent) {
        useInMemoryPgpKeys(signingKey.orNull, signingPassword.orNull)
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
        applyApiPomMetadata()
    }
}

extensions.configure<PublishingExtension> {
    val variantSourcesJar = tasks.register<Jar>("variantSourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }
    val variantJavadocJar = tasks.register<Jar>("variantJavadocJar") {
        archiveClassifier.set("javadoc")
        from(tasks.named("javadoc"))
        dependsOn(tasks.named("javadoc"))
    }

    publications.withType(MavenPublication::class.java).configureEach {
        if (name == "maven") {
            additionalApiArtifacts.forEach { artifact(it.jarTask) }
        }
    }

    additionalApiArtifacts.forEach { variant ->
        publications.register("mavenJvm${variant.javaVersion}", MavenPublication::class.java) {
            groupId = project.group.toString()
            artifactId = "api-${variant.classifier}"
            version = project.version.toString()
            artifact(variant.jarTask) {
                classifier = null
            }
            artifact(variantSourcesJar) {
                classifier = "sources"
            }
            artifact(variantJavadocJar) {
                classifier = "javadoc"
            }
            pom {
                applyApiPomMetadata()
            }
        }
    }
}

// Add explicit dependencies from publish tasks to signMavenPublication task
tasks.withType<PublishToMavenRepository> {
    val signingTasks = tasks.withType<Sign>()
    dependsOn(signingTasks)
}
