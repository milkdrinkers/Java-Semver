import java.time.Instant

plugins {
//    `maven-publish`
//    signing
    alias(libs.plugins.publish.on.central)
}

applyCustomVersion()

dependencies {
    compileOnly(libs.annotations)
    annotationProcessor(libs.annotations)

    testImplementation(libs.annotations)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit)
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.addAll(arrayListOf("-Xlint:all", "-Xlint:-processing", "-Xdiags:verbose"))
    }

    javadoc {
        isFailOnError = false
        val options = options as StandardJavadocDocletOptions
        options.encoding = Charsets.UTF_8.name()
        options.overview = "src/main/javadoc/overview.html"
        options.windowTitle = "${rootProject.name} Javadoc"
        options.tags("apiNote:a:API Note:", "implNote:a:Implementation Note:", "implSpec:a:Implementation Requirements:")
        options.addStringOption("Xdoclint:none", "-quiet")
        options.use()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }

    test {
        useJUnitPlatform()
        failFast = false
    }
}

publishOnCentral {
    projectLongName.set(rootProject.name)
    projectDescription.set(rootProject.description.orEmpty())
    projectUrl.set("https://github.com/milkdrinkers/java-semver")

    licenseName.set("GNU General Public License Version 3")
    licenseUrl.set("https://www.gnu.org/licenses/gpl-3.0.en.html#license-text")

    scmConnection.set("git:git://github.com/milkdrinkers/java-semver")

    configureMavenCentral.set(true)
    mavenCentral.user.set(System.getenv("MAVEN_USERNAME") ?: project.findProperty("maven.username")?.toString())
    mavenCentral.password.set(System.getenv("MAVEN_PASSWORD") ?: project.findProperty("maven.password")?.toString())

    if (rootProject.version.toString().endsWith("-SNAPSHOT")) { // Avoid stable versions being pushed there...
        mavenCentralSnapshotsRepository() // Imports user and password from the configuration for Maven Central
        // mavenCentralSnapshotsRepository() {
        //     ...but they can be customized as per any other repository
        // }
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            groupId = "io.github.milkdrinkers"
            artifactId = "javasemver"
            version = "${rootProject.version}"

            pom {
//                name.set(rootProject.name)
//                description.set(rootProject.description.orEmpty())
//                url.set("https://github.com/milkdrinkers/java-semver")
//                licenses {
//                    license {
//                        name.set("GNU General Public License Version 3")
//                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html#license-text")
//                    }
//                }
                developers {
                    developer {
                        id.set("darksaid98")
                        name.set("darksaid98")
                        email.set("darksaid9889@gmail.com")
                        url.set("https://github.com/darksaid98")
                        organization.set("Milkdrinkers")
                        organizationUrl.set("https://github.com/milkdrinkers")
                    }
                }
//                scm {
//                    connection.set("scm:git:git://github.com/milkdrinkers/java-semver.git")
//                    developerConnection.set("scm:git:ssh://github.com:milkdrinkers/java-semver.git")
//                    url.set("https://github.com/milkdrinkers/java-semver")
//                }
            }
        }
    }
}

signing {
    val signingKey = project.findProperty("signing.key")?.toString() ?: System.getenv("GPG_KEY")
    val signingPassword = project.findProperty("signing.password")?.toString() ?: System.getenv("GPG_PASSWORD")

    useInMemoryPgpKeys(signingKey, signingPassword)
}

fun applyCustomVersion() {
    // Apply custom version arg or append snapshot version
    val ver = properties["altVer"]?.toString() ?: "${rootProject.version}-SNAPSHOT-${Instant.now().epochSecond}"

    // Strip prefixed "v" from version tag
    rootProject.version = (if (ver.first().equals('v', true)) ver.substring(1) else ver.uppercase()).uppercase()
}