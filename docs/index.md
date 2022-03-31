# Home
toothpick is a simple, lightweight dependency injection framework ([JSR-330](https://jcp.org/en/jsr/detail?id=330)) for JVM based languages.

## Features
 - Constructor injection

## Usage
### Maven
```xml
<repositories>
    <repository>
        <id>lusory-repo-releases</id>
        <url>https://repo.lusory.dev/releases</url>
    </repository>
    <repository>
        <id>lusory-repo-snapshots</id>
        <url>https://repo.lusory.dev/snapshots</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>me.lusory.toothpick</groupId>
        <artifactId>core</artifactId>
        <version>LATEST_VERSION_HERE</version>
    </dependency>
</dependencies>
```

### Gradle
#### Groovy DSL
```groovy
repositories {
    maven {
        url 'https://repo.lusory.dev/releases'
    }
    maven {
        url 'https://repo.lusory.dev/snapshots'
    }
}
dependencies {
    implementation 'me.lusory.toothpick:core:LATEST_VERSION_HERE'
}
```

#### Kotlin DSL
```kotlin
repositories {
    maven("https://repo.lusory.dev/releases")
    maven("https://repo.lusory.dev/snapshots")
}
dependencies {
    implementation("me.lusory.toothpick:core:LATEST_VERSION_HERE")
}
```