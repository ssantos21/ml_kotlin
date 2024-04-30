import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument

class TransferSend: CliktCommand(help = "Send the specified coin to an statechain address") {

    private val walletName: String by argument(help = "Name of the wallet")

    private val statechainId: String by argument(help = "Statechain id")

    private val toAddress: String by argument(help = "Address to send the funds")

    private val appContext: AppContext by lazy {
        requireNotNull(currentContext.findObject() as? AppContext) {
            "ClientConfig not found in context"
        }
    }
    
    override fun run() {
        TODO("Not yet implemented")
    }
}