package kr.junhyung.projectgrapher

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

open class ProjectGrapherSubProjectExtension {
    @Optional
    @Input
    var isEnabled: Boolean = true

    @Optional
    @Input
    var group: String? = null
}