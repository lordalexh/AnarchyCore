plugins {
    id("java-library")
}

group = "no.hammers"

// Dynamically fetch version in a Configuration-Cache compliant way
val ciTag = System.getenv("GITHUB_REF_NAME")

val gitVersion: String = if (!ciTag.isNullOrBlank()) {
    ciTag.removePrefix("v")
} else {
    val execOutput = providers.exec {
        commandLine("git", "describe", "--tags", "--abbrev=0")
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim()

    if (execOutput.isNotBlank()) {
        execOutput.removePrefix("v")
    } else {
        "1.1.0" // Default fallback version
    }
}

version = gitVersion

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "codemc-releases"
        url = uri("https://repo.codemc.io/repository/maven-releases/")
    }
}

dependencies {
    compileOnly("dev.folia:folia-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.13.0")
    implementation("org.xerial:sqlite-jdbc:3.45.2.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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