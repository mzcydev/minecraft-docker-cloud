plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("com.google.protobuf") version "0.9.4" apply false
}

group = "dev.cloud"
version = "1.0.0-SNAPSHOT"

subprojects {
    apply(plugin = "java-library")

    group = rootProject.group
    version = rootProject.version

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://nexus.velocitypowered.com/repository/maven-public/")
    }

    dependencies {
        implementation("org.slf4j:slf4j-api:2.0.16")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
    }

    // Lombok global deaktivieren — wird nur manuell pro Modul aktiviert
    configurations.all {
        exclude(group = "org.projectlombok")
    }

    tasks.test { useJUnitPlatform() }
}