import me.lusory.toothpick.gradle.DependencyVersions

plugins {
    id("io.freefair.lombok") version "6.4.1"
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
    api(group = "javax.inject", name = "javax.inject", version = DependencyVersions.JAVAX_INJECT)
    compileOnly(group = "org.jetbrains", name = "annotations", version = DependencyVersions.JB_ANNOTATIONS)

    // testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = DependencyVersions.JUNIT)
    // testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = DependencyVersions.JUNIT)

    jmh(group = "org.openjdk.jmh", name = "jmh-core", version = DependencyVersions.JMH)
    jmh(group = "org.openjdk.jmh", name = "jmh-generator-annprocess", version = DependencyVersions.JMH)

    jmh(group = "org.codejargon.feather", name = "feather", version = DependencyVersions.FEATHER)
    jmh(group = "com.google.inject", name = "guice", version = DependencyVersions.GUICE)
}

/*
tasks.withType<Test> {
    useJUnitPlatform()
}
*/

/*
jmh {
    includes.add("injectToothpick")
}
*/

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

configurations.all {
    resolutionStrategy.cacheDynamicVersionsFor(0, "seconds")
}