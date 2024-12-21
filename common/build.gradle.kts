plugins {
    `maven-publish`
    signing
}

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

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.milkdrinkers"
            artifactId = "javasemver"
            version = "${rootProject.version}"

            pom {
                name.set(rootProject.name)
                description.set(rootProject.description.orEmpty())
                url.set("https://github.com/milkdrinkers/java-semver")
                licenses {
                    license {
                        name.set("GNU General Public License Version 3")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html#license-text")
                    }
                }
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
                scm {
                    connection.set("scm:git:git://github.com/milkdrinkers/java-semver.git")
                    developerConnection.set("scm:git:ssh://github.com:milkdrinkers/java-semver.git")
                    url.set("https://github.com/milkdrinkers/java-semver")
                }
            }

            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "central"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("maven.username") as String? ?: System.getenv("MAVEN_USERNAME")
                password = project.findProperty("maven.password") as String? ?: System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey = project.findProperty("signing.key") as String? ?: System.getenv("GPG_KEY")
    val signingPassword = project.findProperty("signing.password") as String? ?: System.getenv("GPG_PASSWORD")

    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}
