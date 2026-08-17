plugins {
    id("java-library")
}

group = "no.hammers"

// Dynamically fetch version from GitHub Actions OR local Git tags
fun getGitVersion(): String {
    // 1. Check if running inside GitHub Actions (e.g. tag 'v1.1' -> '1.1')
    val ciTag = System.getenv("GITHUB_REF_NAME")
    if (!ciTag.isNullOrBlank()) {
        return ciTag.removePrefix("v")
    }

    // 2. Otherwise, check local Git tags via command line
    return try {
        val process = ProcessBuilder("git", "describe", "--tags", "--abbrev=0")
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
        val tag = process.inputStream.bufferedReader().readText().trim()
        if (tag.startsWith("v")) tag.removePrefix("v") else if (tag.isNotEmpty()) tag else "1.1.0"
    } catch (e: Exception) {
        "1.1.0" // Default fallback version
    }
}

version = getGitVersion()

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
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
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