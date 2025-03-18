import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.publisher)
}

mavenPublishing {
    coordinates("io.github.milkdrinkers", "javasemver", "${rootProject.version}")

    pom {
        name.set(rootProject.name)
        description.set(rootProject.description.orEmpty())
        url.set("https://github.com/milkdrinkers/Java-Semver")
        inceptionYear.set("2025")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("darksaid98")
                name.set("darksaid98")
                url.set("https://github.com/darksaid98")
                organization.set("Milkdrinkers")
            }
        }

        scm {
            url.set("https://github.com/milkdrinkers/Java-Semver")
            connection.set("scm:git:git://github.com/milkdrinkers/Java-Semver.git")
            developerConnection.set("scm:git:ssh://github.com:milkdrinkers/Java-Semver.git")
        }
    }

    configure(JavaLibrary(
        javadocJar = JavadocJar.None(), // The mavenPublishing plugin shouldn't generate another javadoc jar
        sourcesJar = true
    ))

    // Publish to Maven Central
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    // Sign all publications
    signAllPublications()

    // Skip signing for local tasks
    tasks.withType<Sign>().configureEach { onlyIf { !gradle.taskGraph.allTasks.any { it is PublishToMavenLocal } } }
}