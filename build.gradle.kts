/*
 * 🪟 ktor-sentry: Pluggable Ktor plugin to implement Sentry for error handling and request contexts.
 * Copyright (c) 2022 Noel <cutie@floofy.dev>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import gay.floof.gradle.utils.*
import java.util.Properties

buildscript {
    repositories {
        maven("https://maven.floofy.dev/repo/releases")
        gradlePluginPortal()
        mavenCentral()
    }

    dependencies {
        classpath("com.diffplug.spotless:spotless-plugin-gradle:6.3.0")
        classpath("io.kotest:kotest-gradle-plugin:0.3.9")
        classpath("gay.floof.utils:gradle-utils:1.3.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.6.10")
        classpath(kotlin("gradle-plugin", version = "1.6.10"))
    }
}

plugins {
    id("com.diffplug.spotless") version "6.3.0"
    id("org.jetbrains.dokka") version "1.6.10"
    kotlin("jvm") version "1.6.10"
    id("io.kotest") version "0.3.9"

    `java-library`
    `maven-publish`
}

val VERSION = Version(0, 0, 1, 0)
val JAVA_VERSION = JavaVersion.VERSION_17

group = "dev.floofy"
version = "$VERSION"

repositories {
    mavenCentral()
    mavenLocal()
    noel()
}

dependencies {
    // Kotlin stdlib
    implementation(kotlin("stdlib", version = "1.6.10"))

    // Noel Utilities
    implementation("gay.floof.commons", "commons-slf4j", "1.3.0")

    // slf4j logging
    api("org.slf4j:slf4j-api:1.7.32")

    // Ktor
    api(platform("io.ktor:ktor-bom:1.6.7"))
    api("io.ktor:ktor-server-core")

    // Sentry!
    api("io.sentry:sentry:5.6.0")

    // Testing utilities
    testImplementation(platform("io.kotest:kotest-bom:5.0.3"))
    testImplementation("io.ktor:ktor-server-tests")
    testImplementation("io.kotest:kotest-runner-junit5")
    testImplementation("io.kotest:kotest-assertions-core")
    testImplementation("io.kotest:kotest-property")
}

// Setup Spotless in all subprojects
spotless {
    kotlin {
        trimTrailingWhitespace()
        licenseHeaderFile("${rootProject.projectDir}/assets/HEADING")
        endWithNewline()

        // We can't use the .editorconfig file, so we'll have to specify it here
        // issue: https://github.com/diffplug/spotless/issues/142
        // ktlint 0.35.0 (default for Spotless) doesn't support trailing commas
        ktlint("0.43.0")
            .userData(
                mapOf(
                    "no-consecutive-blank-lines" to "true",
                    "no-unit-return" to "true",
                    "disabled_rules" to "no-wildcard-imports,colon-spacing",
                    "indent_size" to "4"
                )
            )
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JAVA_VERSION.toString()
            javaParameters = true
            freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
        }
    }

    dokkaHtml {
        outputDirectory.set(file("${rootProject.projectDir}/docs"))

        dokkaSourceSets {
            configureEach {
                platform.set(org.jetbrains.dokka.Platform.jvm)
                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(uri("https://github.com/auguwu/sentry-ktor/tree/master/src/main/kotlin").toURL())
                    remoteLineSuffix.set("#L")
                }

                jdkVersion.set(17)
            }
        }
    }
}

java {
    sourceCompatibility = JAVA_VERSION
    targetCompatibility = JAVA_VERSION
}

val publishingProps = try {
    Properties().apply { load(file("${rootProject.projectDir}/gradle/publishing.properties").reader()) }
} catch(e: Exception) {
    Properties()
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val dokkaJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assemble Kotlin documentation with Dokka"

    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
    dependsOn(tasks.dokkaHtml)
}

publishing {
    publications {
        create<MavenPublication>("SentryKtor") {
            from(components["kotlin"])
            groupId = "dev.floofy.ktor"
            artifactId = "ktor-sentry"
            version = "$VERSION"

            artifact(sourcesJar.get())
            artifact(dokkaJar.get())

            pom {
                description.set("Pluggable Ktor plugin to implement Sentry for error handling and request contexts.")
                name.set("ktor-sentry")
                url.set("https://auguwu.github.io/sentry-ktor")

                organization {
                    name.set("Noel")
                    url.set("https://floofy.dev")
                }

                developers {
                    developer {
                        name.set("Noel")
                    }
                }

                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/auguwu/sentry-ktor/issues")
                }

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                scm {
                    connection.set("scm:git:ssh://github.com/auguwu/sentry-ktor.git")
                    developerConnection.set("scm:git:ssh://git@github.com:auguwu/sentry-ktor.git")
                    url.set("https://github.com/auguwu/sentry-ktor")
                }
            }
        }
    }

    repositories {
        maven(url = "s3://maven.floofy.dev/repo/releases") {
            credentials(AwsCredentials::class.java) {
                accessKey = publishingProps.getProperty("s3.accessKey") ?: ""
                secretKey = publishingProps.getProperty("s3.secretKey") ?: ""
            }
        }
    }
}
