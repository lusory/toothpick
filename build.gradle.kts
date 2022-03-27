plugins {
    `java-library`
}

allprojects {
    group = "me.lusory"
    version = "0.0.1-SNAPSHOT"
}

subprojects {
    apply(plugin = "java-library")

    repositories {
        mavenCentral()
    }
}