package kr.junhyung.projectgrapher

import kr.junhyung.projectgrapher.internal.ProjectGraphGenerationService
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class ProjectGrapherPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("projectGrapher", ProjectGrapherRootProjectExtension::class.java, project)
        val service = ProjectGraphGenerationService(project, extension)
        val task = project.tasks.register("generateProjectGraph", ProjectGrapherTask::class.java, extension, service)
        task.configure {
            it.group = "reporting"
            it.description = "Generates a project dependency graph"
            it.outputDirectory = File(project.buildDir, "reports/project-graph/")
        }

        project.subprojects.forEach { subproject ->
            subproject.extensions.add("projectGrapher", ProjectGrapherSubProjectExtension::class.java)
        }
    }
}
