package io.getstream.chat.android.command.kdocs.plugin

import io.getstream.chat.android.command.release.task.KdocsCheckTask
import io.getstream.chat.android.command.utils.registerExt
import org.gradle.api.Plugin
import org.gradle.api.Project

private const val COMMAND_NAME = "kdocs-check-punctuation"

class KdocsCheckPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.registerExt<KdocsCheckTask>(COMMAND_NAME)
    }
}
