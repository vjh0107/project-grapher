package kr.junhyung.projectgrapher

import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.model.Link
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.model.MutableNode
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import javax.inject.Inject

open class ProjectGrapherRootProjectExtension @Inject constructor(private val project: Project) {
    @Input
    @Optional
    var label: String = project.name

    @get:Nested
    var projectNode: (MutableNode, Project) -> MutableNode = { node, _ -> node }

    @Input
    @Optional
    var rankSep: Double = 0.75

    @Input
    var dpi: Int = 300

    @get:Nested
    var link: (link: Link, projectDependencyWrapper: ProjectDependencyWrapper) -> Link = { it, moduleDependency ->
        when (moduleDependency.configuration.name) {
            "implementation" -> it.with(Style.DASHED)
            "compileOnly" -> it.with(Style.DOTTED)
            "runtimeOnly" -> it.with(Style.DASHED)
            "api" -> it.with(Style.SOLID)
            else -> it
        }
    }

    @get:Nested
    var outputFormats: List<Format> = listOf(Format.PNG, Format.SVG)

    @get:Nested
    var graph: (MutableGraph) -> MutableGraph = { it }

    @get:Nested
    var graphviz: (Graphviz) -> Graphviz = { it }

    @get:Internal
    internal val outputFileName = "project-graph"

    @get:Internal
    internal val outputFileNameDot = "$outputFileName.dot"

    @get:Nested
    var color: (Project) -> Color = { Color.rgb("#BDBDBD").fill() }

    fun color(supplier: (Project) -> Color) {
        this.color = supplier
    }
}