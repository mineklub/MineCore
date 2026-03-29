import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    id("java-library")
    alias(libs.plugins.shadow)
    id("com.vanniktech.maven.publish") version "0.36.0"
    signing
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.gson)
    implementation(libs.okhttp) {
        exclude(group = "com.squareup.okio", module = "okio")
    }
    implementation(libs.okio)
    implementation(libs.socketclient) {
        exclude(group = "org.json", module = "json")
        exclude(group = "com.squareup.okio", module = "okio")
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }
    implementation(libs.json)
    api(libs.jspecify)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    withType<ShadowJar> {
        exclude("META-INF/**")
        minimize()
    }
    java {
        withJavadocJar()
        withSourcesJar()
    }
    signing {
        useGpgCmd()
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    configure(JavaLibrary(
        javadocJar = JavadocJar.None(),
        sourcesJar = true,
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
