# 🪟 Ktor Plugin for Sentry
> *Pluggable Ktor plugin to implement Sentry for error handling and request contexts.*

## What is this library?
This basically implements error tracking and appending the request to your Sentry project. It's just simple as installing the plugin,
and it will track errors and such.

## Example Usage
```kotlin
fun Application.module() {
    install(Sentry) {
        dsn = "..."
        scope { s ->
            s.addTag("blep", "the fluff")
        }
    }
}
```

## Installation
> :eyes: **0.0.1** | :scroll: [Documentation](https://auguwu.github.io/sentry-ktor)

## Gradle
### Kotlin DSL
```kotlin
repositories {
    maven {
        url = uri("https://maven.floofy.dev/repo/releases")
    }
}

dependencies {
    implementation("dev.floofy.ktor:ktor-sentry:<VERSION>")
}
```

### Groovy DSL
```groovy
repositories {
    maven {
        url "https://maven.floofy.dev/repo/releases"
    }
}

dependencies {
    implementation "dev.floofy.ktor:ktor-sentry:<VERSION>"
}
```

## Maven
```xml
<repositories>
    <repository>
        <id>noel-maven</id>
        <url>https://maven.floofy.dev/repo/releases</url>
    </repository>
</repositories>
```

```xml
<dependencies>
    <dependency>
        <groupId>dev.floofy.ktor</groupId>
        <artifactId>ktor-sentry</artifactId>
        <version>{{VERSION}}</version>
        <type>pom</type>
    </dependency>
</dependencies>
```

## License
**ktor-sentry** is released under the **MIT** License by Noel. Read [here](/LICENSE) for more information.
