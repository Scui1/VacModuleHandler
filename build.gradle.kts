import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val ktorVersion = "2.3.7"
plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "de.scui"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        group
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/Scui1/KotlinPEFile")
        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.4.9")
    implementation("de.scui:kotlin-pefile:1.0")
    implementation("io.ktor:ktor-client-java:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("io.ktor.server.cio.EngineMain")
}

tasks.withType<ShadowJar> {
    archiveFileName.set("vacmodulehandler.jar")
}