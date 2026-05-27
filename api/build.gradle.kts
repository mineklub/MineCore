import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    id("java-library")
    alias(libs.plugins.shadow)
    alias(libs.plugins.maven.publish)
    signing
}

group = rootProject.group
version = rootProject.version

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
    implementation(libs.okhttp) {
        exclude(group = "com.squareup.okio", module = "okio")
    }
    implementation(libs.okio)
    implementation(libs.json)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withJavadocJar()
    withSourcesJar()
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release.set(25)
    }
    withType<ShadowJar> {
        exclude("META-INF/**")
        minimize()
    }
    signing {
        useGpgCmd()
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
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
