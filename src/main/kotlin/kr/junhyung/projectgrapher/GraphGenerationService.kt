package kr.junhyung.projectgrapher

import guru.nidi.graphviz.model.MutableGraph

interface GraphGenerationService {
    fun generateGraph(): MutableGraph
}