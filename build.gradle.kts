plugins {
    id("java-library")
}

group = "no.hammers"

// Extract version from GitHub Actions Git Tag (e.g. "v1.0.0" -> "1.0.0")
// Falls back to "1.0.0" for local dev builds
val gitVersion: String? = System.getenv("GITHUB_REF_NAME")?.removePrefix("v")
version = gitVersion ?: "1.0.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    // Added CodeMC repository for PacketEvents
    maven {
        name = "codemc-releases"
        url = uri("https://repo.codemc.io/repository/maven-releases/")
    }
}

dependencies {
    compileOnly("dev.folia:folia-api:1.20.4-R0.1-SNAPSHOT")

    // PacketEvents API for Paper/Folia (Spigot/Paper platform)
    compileOnly("com.github.retrooper:packetevents-spigot:2.13.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filesMatching(listOf("paper-plugin.yml", "plugin.yml")) {
            expand(props)
        }
    }
}
