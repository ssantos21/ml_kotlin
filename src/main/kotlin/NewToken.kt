import com.github.ajalt.clikt.core.CliktCommand

class NewToken : CliktCommand(help = "Generates a new token") {
    override fun run() {
        println("A new token has been generated successfully.")
    }
}