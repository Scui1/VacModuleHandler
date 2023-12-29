plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
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
    implementation("io.ktor:ktor-client-java:2.3.3")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.3.3")
    implementation("io.ktor:ktor-server-core:2.3.3")
    implementation("io.ktor:ktor-server-jetty:2.3.3")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.3")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("io.ktor.server.jetty.EngineMain")
}