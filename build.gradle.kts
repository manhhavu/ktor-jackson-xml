val jackson_version: String by project
val ktor_version: String by project

plugins {
    kotlin("jvm") version "1.3.72"
}

group = "com.manhhavu"
version = "0.1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jackson_version")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.xmlunit:xmlunit-matchers:2.2.1")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}