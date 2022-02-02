package io.getstream.chat.android.command.utils.output

class StdoutPrinter : Printer {
    override fun printline(text: String) {
        println(text)
    }
}
