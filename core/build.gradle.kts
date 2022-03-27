import me.lusory.toothpick.gradle.DependencyVersions

plugins {
    id("io.freefair.lombok") version "6.4.1"
}

dependencies {
    implementation(group = "org.jetbrains", name = "annotations", version = DependencyVersions.JB_ANNOTATIONS)

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = DependencyVersions.JUNIT)
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = DependencyVersions.JUNIT)
}

tasks.withType<Test> {
    useJUnitPlatform()
}