plugins {
    kotlin("jvm") version "1.9.0"
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
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}