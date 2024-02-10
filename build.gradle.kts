plugins {
    kotlin("jvm") version "1.8.22"
    `maven-publish`
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
            displayName = "ProjectGrapher"
            description = "The powerful project visualizer for Gradle multi-project environment."
        }
    }
}

pluginBundle {
    this.description = "The powerful project visualizer for Gradle multi-project environment."
    this.vcsUrl =   extra["project.url.scm"]!!.toString()
    this.website = extra["project.url"]!!.toString()
    this.tags = listOf("project-grapher", "grapher")
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}