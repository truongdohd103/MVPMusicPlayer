import org.gradle.kotlin.dsl.`kotlin-dsl`
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

// Ép jvmTarget về 17 cho tất cả các task KotlinCompile
tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

// Ép sourceCompatibility và targetCompatibility cho JavaCompile về 17
tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}
