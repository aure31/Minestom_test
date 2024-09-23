plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "org.example"
version = "0.0.1"
var fileName = "server"

repositories {
    mavenCentral()
    maven("https://reposilite.worldseed.online/public")
    maven("https://maven.serble.net/snapshots/")
}

dependencies {
    // Change this to the latest version
    implementation("net.minestom:minestom-snapshots:9fbff439e7")
    implementation("org.slf4j:slf4j-simple:2.0.14")
    implementation("net.worldseed.multipart:WorldSeedEntityEngine:11.0.1")
    implementation ("ru.brikster:glyphs-api:1.1.0")
    implementation("ru.brikster:glyphs-resources:1.1.0")
    implementation("net.worldseed.particleemitter:ParticleEmitter:1.4.0")
    implementation("net.mangolise:mango-anti-cheat:latest")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // Minestom has a minimum Java version of 21
    }
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "org.example.Main" // Change this to your main class
        }
    }

    build {
        dependsOn(shadowJar)
        doLast{
            copy {
                from("build/libs/${project.name}-${project.version}.jar")
                into("${project.projectDir}/server/")
                rename("${project.name}-${project.version}.jar", "${fileName}-${project.version}.jar")
            }
        }

    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("") // Prevent the -all suffix on the shadowjar file.
    }
}