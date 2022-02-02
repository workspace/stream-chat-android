package io.getstream.chat.android.command.release.task

import io.getstream.chat.android.command.utils.output.StdoutPrinter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class KdocsCheckTask : DefaultTask() {

    @TaskAction
    private fun command() {
        val printer = StdoutPrinter()
        printer.printline("To be implemented")
    }
}
