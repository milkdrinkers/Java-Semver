<h1 style="text-align:center;">Java-Semver</h1>

<p style="text-align:center;">
    <a href="https://github.com/milkdrinkers/Java-Semver/blob/main/LICENSE">
        <img alt="GitHub License" src="https://img.shields.io/github/license/milkdrinkers/Java-Semver?style=for-the-badge&color=blue&labelColor=141417">
    </a>
    <a href="https://central.sonatype.com/artifact/io.github.milkdrinkers/javasemver">
        <img alt="Maven Central Version" src="https://img.shields.io/maven-central/v/io.github.milkdrinkers/javasemver?style=for-the-badge&labelColor=141417">
    </a>
    <a href="https://milkdrinkers.athyrium.eu/javasemver">
        <img alt="Documentation" src="https://img.shields.io/badge/DOCUMENTATION-900C3F?style=for-the-badge&labelColor=141417">
    </a>
    <a href="https://javadoc.io/doc/io.github.milkdrinkers/javasemver">
        <img alt="Javadoc" src="https://img.shields.io/badge/JAVADOC-8A2BE2?style=for-the-badge&labelColor=141417">
    </a>
    <img alt="GitHub Actions Workflow Status" src="https://img.shields.io/github/actions/workflow/status/milkdrinkers/Java-Semver/ci.yml?style=for-the-badge&labelColor=141417">
    <a href="https://github.com/milkdrinkers/Java-Semver/issues">
        <img alt="GitHub Issues" src="https://img.shields.io/github/issues/milkdrinkers/Java-Semver?style=for-the-badge&labelColor=141417">
    </a>
    <img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/milkdrinkers/Java-Semver?style=for-the-badge&labelColor=141417">
    <a href="https://discord.gg/cG5uWvUcM6">
        <img alt="Discord Server" src="https://img.shields.io/discord/1008300159333040158?style=for-the-badge&logo=discord&logoColor=ffffff&label=discord&labelColor=141417&color=%235865F2">
    </a>
</p>

**Java-Semver** is a lightweight, zero-dependency Java library that provides full support for **Semantic Versioning 2.0.0**. Designed for simplicity and reliability, it enables parsing and comparing semantic versions (`major.minor.patch-preRelease+build`) effortlessly. Perfect for applications requiring precise version management.

---

## 🌟 Features

- **Full SemVer 2.0.0 Compliance**: Strict adherence to the official specification.
- **Zero Dependencies**: Lightweight and self-contained.
- **Java 8+ compatible**: Compatible with legacy and modern Java projects.
- **Simple API**: Intuitive methods for parsing and comparing versions.
- **Error Handling**: Gracefully handles invalid versions through exceptions.
- **Well-tested**: Robust JUnit test coverage ensures reliability.
- **Pre-release & Build Metadata**: Supports `1.0.0-alpha+001` and other complex formats.

## 📦 Installation

Add Java-Semver to your project with **Maven** or **Gradle**:  

<details>
<summary>Gradle Kotlin DSL</summary>

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.milkdrinkers:javasemver:LATEST_VERSION")
}
```
</details>

<details>
<summary>Maven</summary>

```xml
<project>
    <dependencies>
        <dependency>  
            <groupId>io.github.milkdrinkers</groupId>  
            <artifactId>javasemver</artifactId>  
            <version>LATEST_VERSION</version>  
        </dependency>  
    </dependencies>
</project>
```
</details>

## Usage Example 🚀
```java
import io.github.milkdrinkers.javasemver.Version;

final Version currentVersion = Version.of("1.0.0-RC.1+5");
final Version latestVersion = Version.of("2.0.0-beta+exp.sha.5114f85");

Version.isNewer(currentVersion, latestVersion); // false
Version.isNewerOrEqual(currentVersion, latestVersion); // false
Version.isEqual(currentVersion, latestVersion); // false
Version.isOlderOrEqual(currentVersion, latestVersion); // true
Version.isOlder(currentVersion, latestVersion); // true
```

## 📚 Documentation 

- [Full Javadoc Documentation](https://javadoc.io/doc/io.github.milkdrinkers/javasemver)
- [Documentation](https://milkdrinkers.athyrium.eu/javasemver)
- [Maven Central](https://central.sonatype.com/artifact/io.github.milkdrinkers/javasemver)

---

## 🔨 Building from Source 

```bash
git clone https://github.com/milkdrinkers/Java-Semver.git
cd javasemver
./gradlew publishToMavenLocal
```

---

## 🔧 Contributing

Contributions are always welcome! Please make sure to read our [Contributor's Guide](CONTRIBUTING.md) for standards and our [Contributor License Agreement (CLA)](CONTRIBUTOR_LICENSE_AGREEMENT.md) before submitting any pull requests.

We also ask that you adhere to our [Contributor Code of Conduct](CODE_OF_CONDUCT.md) to ensure this community remains a place where all feel welcome to participate.

---

## 📝 Licensing

You can find the license the source code and all assets are under [here](../LICENSE). Additionally, contributors agree to the Contributor License Agreement \(*CLA*\) found [here](CONTRIBUTOR_LICENSE_AGREEMENT.md).
