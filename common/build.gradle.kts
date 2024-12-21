import java.time.Instant

plugins {
//    `maven-publish`
//    signing
//    alias(libs.plugins.publish.on.central)
//    alias(libs.plugins.maven.deployer)
//    alias(libs.plugins.jreleaser)
    id("io.deepmedia.tools.deployer") version "0.16.0"
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

deployer {
    release {
        version.set("${rootProject.version}")
        description.set(rootProject.description.orEmpty())
    }

    projectInfo {
        groupId = "io.github.milkdrinkers"
        artifactId = "javasemver"
        version = "${rootProject.version}"

        name = rootProject.name
        description = rootProject.description.orEmpty()
        url = "https://github.com/milkdrinkers/java-semver"

        scm {
            connection = "scm:git:git://github.com/milkdrinkers/java-semver.git"
            developerConnection = "scm:git:ssh://github.com:milkdrinkers/java-semver.git"
            url = "https://github.com/milkdrinkers/java-semver"
        }

        license({
            name = "GNU General Public License Version 3"
            url = "https://www.gnu.org/licenses/gpl-3.0.en.html#license-text"
        })

        developer({
            name.set("darksaid98")
            email.set("darksaid9889@gmail.com")
            url.set("https://github.com/darksaid98")
            organization.set("Milkdrinkers")
        })
    }

    content {
        component {
            fromJava()
        }
    }

    centralPortalSpec {
        allowMavenCentralSync = false
        auth.user.set(secret("MAVEN_USERNAME"))
        auth.password.set(secret("MAVEN_PASSWORD"))
    }

    signing {
        key.set(secret("GPG_KEY"))
        password.set(secret("GPG_PASSWORD"))
    }
}

//publishing {
//    publications {
//        create<MavenPublication>("maven") {
//            groupId = "io.github.milkdrinkers"
//            artifactId = "javasemver"
//            version = "${rootProject.version}"
//
//            from(components["java"])
//
//            pom {
//                name.set(rootProject.name)
//                description.set(rootProject.description.orEmpty())
//                url.set("https://github.com/milkdrinkers/java-semver")
//                inceptionYear.set("2024")
//
//                licenses {
//                    license {
//                        name.set("GNU General Public License Version 3")
//                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html#license-text")
//                    }
//                }
//
//                developers {
//                    developer {
//                        id.set("darksaid98")
//                        name.set("darksaid98")
//                        email.set("darksaid9889@gmail.com")
//                        url.set("https://github.com/darksaid98")
//                        organization.set("Milkdrinkers")
//                        organizationUrl.set("https://github.com/milkdrinkers")
//                    }
//                }
//
//                scm {
//                    connection.set("scm:git:git://github.com/milkdrinkers/java-semver.git")
//                    developerConnection.set("scm:git:ssh://github.com:milkdrinkers/java-semver.git")
//                    url.set("https://github.com/milkdrinkers/java-semver")
//                }
//            }
//        }
//    }
//
//    repositories {
//        maven {
//            url = uri(layout.buildDirectory.dir("staging-deploy"))
//        }
//    }
//}



//jreleaser {
//    signing {
//        active.set(Active.ALWAYS)
//        armored.set(true)
//        secretKey.set(rootProject.findProperty("signing.key")?.toString() ?: System.getenv("GPG_KEY"))
//        passphrase.set(rootProject.findProperty("signing.password")?.toString() ?: System.getenv("GPG_PASSWORD"))
//    }
//    deploy {
//        maven {
//            mavenCentral {
//                create("sonatype") {
//                    active.set(Active.ALWAYS)
//                    username.set(rootProject.findProperty("maven.username")?.toString() ?: System.getenv("MAVEN_USERNAME"))
//                    password.set(rootProject.findProperty("maven.password")?.toString() ?: System.getenv("MAVEN_PASSWORD"))
//                    url.set("https://central.sonatype.com/api/v1/publisher")
//                    stagingRepository("target/staging-deploy")
//                    applyMavenCentralRules.set(true)
//                }
//            }
//        }
//    }
//}

//signing {
//    val signingKey = project.findProperty("signing.key")?.toString() ?: System.getenv("GPG_KEY")
//    val signingPassword = project.findProperty("signing.password")?.toString() ?: System.getenv("GPG_PASSWORD")
//
//    useInMemoryPgpKeys(signingKey, signingPassword)
//}

fun applyCustomVersion() {
    // Apply custom version arg or append snapshot version
    val ver = properties["altVer"]?.toString() ?: "${rootProject.version}-SNAPSHOT-${Instant.now().epochSecond}"

    // Strip prefixed "v" from version tag
    rootProject.version = (if (ver.first().equals('v', true)) ver.substring(1) else ver.uppercase()).uppercase()
}