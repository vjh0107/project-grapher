package kr.junhyung.projectgrapher.internal

import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.attribute.Rank.RankType
import guru.nidi.graphviz.model.Factory
import guru.nidi.graphviz.model.Graph
import guru.nidi.graphviz.model.Factory as GraphvizFactory
import guru.nidi.graphviz.model.Link
import guru.nidi.graphviz.model.MutableGraph
import kr.junhyung.projectgrapher.GraphGenerationService
import kr.junhyung.projectgrapher.ProjectDependencyWrapper
import kr.junhyung.projectgrapher.ProjectGrapherSubProjectExtension
import kr.junhyung.projectgrapher.ProjectGrapherRootProjectExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

internal class ProjectGraphGenerationService(
    private val rootProject: Project,
    private val rootProjectExtension: ProjectGrapherRootProjectExtension
) : GraphGenerationService {
    override fun generateGraph(): MutableGraph {
        val graph = createRootGraph()
        val subProjects = getEnabledProjects()
        val dependencies = subProjects.flatMap { project ->
            project.configurations.flatMap { configuration ->
                configuration.dependencies.withType(ProjectDependency::class.java).map { projectDependency ->
                    ProjectDependencyWrapper.of(project, projectDependency, configuration)
                }
            }
        }

        val groupedNodes = getGroupedProjects().map { (groupName, projects) ->
            val nodes = projects.map { project ->
                GraphvizFactory.mutNode(project.path)
            }
            val groupedGraph = createClusteredGraph(groupName)
                .named(groupName)
                .with(nodes)
                .toMutable()
                .graphAttrs()
                .add(
                    Label.of(groupName).locate(Label.Location.TOP),

                    Font.size(15),
                )
            groupedGraph
        }

        val topLevelDependencies = getTopLevelDependencyProjects(dependencies)
        subProjects.forEach { project ->
            val node = GraphvizFactory.mutNode(project.path)
                .add(Label.of(project.name))
            val isTopLevelDependencyProject = topLevelDependencies.contains(project)
            if (isTopLevelDependencyProject) {
                node.add(Shape.RECTANGLE)
            } else {
                node.add(Shape.OVAL)
            }
            node.add(rootProjectExtension.color.invoke(project))
            graph.add(rootProjectExtension.projectNode(node, project))
        }

        graph.add(groupedNodes)
        graph.nodeAttrs().add(Style.FILLED)

        dependencies.forEach { moduleDependency ->
            val startNode = graph.rootNodes().find { node ->
                node.name().toString() == moduleDependency.project.path
            } ?: return@forEach
            val endNode = graph.rootNodes().find { node ->
                node.name().toString() == moduleDependency.dependency.path
            } ?: return@forEach
            val toLink = rootProjectExtension.link(Link.to(endNode), moduleDependency)
            val link = startNode.addLink(toLink)
            graph.add(link)
        }

        return rootProjectExtension.graph(graph)
    }

    private fun createRootGraph(): MutableGraph {
        return Factory
            .mutGraph()
            .setDirected(true)
            .graphAttrs()
            .add(
                Label.of(rootProjectExtension.label).locate(Label.Location.TOP),
                Font.size(35),
                GraphAttr.dpi(rootProjectExtension.dpi)
            )
            .graphAttrs()
            .add(Rank.dir(Rank.RankDir.TOP_TO_BOTTOM), Rank.sep(rootProjectExtension.rankSep))
    }

    private fun createClusteredGraph(
        name: String,
    ): Graph {
        return GraphvizFactory.graph(name)
            .cluster()
            .graphAttr()
            .with(Rank.inSubgraph(RankType.SAME))
    }

    private fun getTopLevelDependencyProjects(moduleDependencies: Collection<ProjectDependencyWrapper>): Collection<Project> {
        return getEnabledProjects().filter { project ->
            moduleDependencies.none { moduleDependency ->
                moduleDependency.dependency == project
            }
        }
    }

    private fun getGroupedProjects(): Map<String, Collection<Project>> {
        return getEnabledProjects().groupBy { project ->
            project.extensions.findByType(ProjectGrapherSubProjectExtension::class.java)?.group ?: ""
        }.filterKeys { it.isNotBlank() }
    }

    private fun getEnabledProjects(): Collection<Project> {
        return rootProject.subprojects.filter { project ->
            project.extensions.findByType(ProjectGrapherSubProjectExtension::class.java)?.isEnabled == true
        }
    }
}
