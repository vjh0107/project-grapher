package kr.junhyung.projectgrapher

import guru.nidi.graphviz.engine.Graphviz
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File
import javax.inject.Inject

@CacheableTask
open class ProjectGrapherTask @Inject constructor(
    private val projectGrapherRootProjectExtension: ProjectGrapherRootProjectExtension,
    private val service: GraphGenerationService,
) : DefaultTask() {
    @OutputDirectory
    lateinit var outputDirectory: File

    @TaskAction
    fun run() {
        val graph = service.generateGraph()
        val dot = File(outputDirectory, projectGrapherRootProjectExtension.outputFileNameDot)
        dot.writeText(graph.toString())

        val graphviz = Graphviz
            .fromGraph(graph)
            .run(projectGrapherRootProjectExtension.graphviz)

        projectGrapherRootProjectExtension.outputFormats.forEach {
            graphviz.render(it).toFile(File(outputDirectory, projectGrapherRootProjectExtension.outputFileName))
        }
    }
}
