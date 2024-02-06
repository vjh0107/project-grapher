import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin

plugins {
    kotlin("jvm") version "1.8.22"
    `maven-publish`
    signing
    id("com.gradle.plugin-publish") version "1.2.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(localGroovy())
    implementation(gradleApi())
    api("guru.nidi:graphviz-java:0.18.1")
}

version = extra["project.version"]!!.toString()
group = extra["project.group"]!!.toString()

gradlePlugin {
    plugins {
        register("projectGrapher") {
            id = "kr.junhyung.project-grapher"
            implementationClass = "kr.junhyung.projectgrapher.ProjectGrapherPlugin"
        }
    }
}

KotlinMultiplatformPlugin

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

if (extra.properties.keys.any { it.startsWith("signing.") } || System.getenv().keys.any { it.startsWith("SIGNING_") }) {
    tasks.withType<Jar> {
        archiveClassifier.set("")
    }

    extensions.getByType<JavaPluginExtension>().run {
        withSourcesJar()
        withJavadocJar()
    }

    publishing {
        repositories {
            maven {
                name = "sonatype"
                setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = System.getenv("OSSRH_USERNAME")
                    password = System.getenv("OSSRH_PASSWORD")
                }
            }
        }

        publications {
            create<MavenPublication>("mavenCentral") {
                this.groupId = extra["project.group"]!!.toString()
                this.version = extra["project.version"]!!.toString()
                this.artifactId = extra["project.artifact"]!!.toString()
                from(components["java"])

                pom {
                    name.set(project.rootProject.name)
                    url.set(extra["project.url"]!!.toString())
                    description.set(extra["project.description"]!!.toString())

                    licenses {
                        license {
                            name.set(extra["project.license"]!!.toString())
                            url.set(extra["project.license.url"]!!.toString())
                        }
                    }
                    developers {
                        developer {
                            id.set(extra["project.developer.id"]!!.toString())
                            name.set(extra["project.developer.name"]!!.toString())
                            email.set(extra["project.developer.email"]!!.toString())
                        }
                        scm {
                            url.set(extra["project.url.scm"]!!.toString())
                        }
                    }
                }
                signing {
                    setRequired {
                        !project.version.toString()
                            .contains("-SNAPSHOT") && gradle.taskGraph.allTasks.any { it is PublishToMavenRepository }
                    }
                    if (!extra.has("signing.keyId")) {
                        extra["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
                    }
                    if (!extra.has("signing.password")) {
                        extra["signing.password"] = System.getenv("SIGNING_PASSPHRASE")
                    }
                    if (!extra.has("signing.secretKeyRingFile")) {
                        extra["signing.secretKeyRingFile"] = if (System.getenv("SIGNING_SECRET_KEY_RING_FILE_ABSOLUTE") != null) {
                            System.getenv("SIGNING_SECRET_KEY_RING_FILE_ABSOLUTE")
                        } else {
                            System.getenv("HOME") + "/" + System.getenv("SIGNING_SECRET_KEY_RING_FILE")
                        }
                    }
                    sign(this@publications)
                }
            }
        }
    }

    tasks.withType<AbstractPublishToMaven>().configureEach {
        val signingTasks = tasks.withType<Sign>()
        mustRunAfter(signingTasks)
    }
}