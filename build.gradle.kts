import no.iktdev.ts.TsGenerator
import java.net.URLClassLoader
import kotlin.jvm.java

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.5"
}

group = "no.iktdev"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot (WebFlux gir SSE)
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // JSON
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")

    // Prosesskjøring (jotta-cli)
    implementation("com.github.pgreze:kotlin-process:1.5.1")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.assertj:assertj-core:3.25.3")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}


tasks.register("generateTs") {
    dependsOn("build")
    doLast {
        val classesDir = file("$projectDir/build/classes/kotlin/main")
        val cl = URLClassLoader(arrayOf(classesDir.toURI().toURL()), TsGenerator::class.java.classLoader)

        TsGenerator.generate(
            packageName = "no.iktdev.japp.models",
            output = file("web/src/types/types.ts"),
            classLoader = cl
        )
    }
}


tasks.named("build") {
    finalizedBy("generateTs")
}


tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    enabled = false
}

tasks.bootJar {
    archiveFileName.set("app.jar")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

