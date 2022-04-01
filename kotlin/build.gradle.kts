plugins {
    kotlin("jvm") version me.lusory.toothpick.gradle.DependencyVersions.KOTLIN
}

dependencies {
    api(project(":core"))
}