plugins {
    id("org.cadixdev.licenser") version "0.6.1"
    `maven-publish`
    `java-library`
}

allprojects {
    group = "me.lusory.toothpick"
    version = "0.0.1-SNAPSHOT"
}

subprojects {
    apply {
        plugin("java-library")
        plugin("maven-publish")
        plugin("org.cadixdev.licenser")
    }

    license {
        header(rootProject.file("license_header.txt"))
    }

    repositories {
        mavenCentral()
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                pom {
                    name.set("toothpick")
                    description.set("A simple, lightweight dependency injection framework for JVM based languages")
                    url.set("https://github.com/lusory/toothpick")
                    licenses {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("https://github.com/lusory/toothpick/blob/master/LICENSE")
                        }
                    }
                    developers {
                        developer {
                            id.set("zlataovce")
                            name.set("Matouš Kučera")
                            email.set("mk@kcra.me")
                        }
                        developer {
                            id.set("tlkh40") // troll
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/lusory/toothpick.git")
                        developerConnection.set("scm:git:ssh://github.com/lusory/toothpick.git")
                        url.set("https://github.com/lusory/toothpick/tree/master")
                    }
                }
            }
        }

        repositories {
            maven {
                url = if ((project.version as String).endsWith("-SNAPSHOT")) uri("https://repo.lusory.dev/snapshots")
                    else uri("https://repo.lusory.dev/releases")
                credentials {
                    username = System.getenv("REPO_USERNAME")
                    password = System.getenv("REPO_PASSWORD")
                }
            }
        }
    }
}
