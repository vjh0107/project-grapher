package kr.junhyung.projectgrapher

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency

data class ProjectDependencyWrapper(
    val project: Project,
    val dependency: Project,
    val configuration: Configuration
) {
    companion object {
        fun of(project: Project, projectDependency: ProjectDependency, configuration: Configuration): ProjectDependencyWrapper {
            return ProjectDependencyWrapper(project, projectDependency.dependencyProject, configuration)
        }
    }
}