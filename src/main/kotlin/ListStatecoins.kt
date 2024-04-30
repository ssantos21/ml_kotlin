import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class ListStatecoins: CliktCommand(help = "List all wallet' statecoins") {

    private val walletName: String by argument(help = "Name of the wallet to create")

    private val appContext: AppContext by lazy {
        requireNotNull(currentContext.findObject() as? AppContext) {
            "ClientConfig not found in context"
        }
    }

    override fun run() {

        // TODO: not recommend for production. Change to use CoroutineScope or another approach
        runBlocking {
            val wallet = appContext.sqliteManager.loadWallet(walletName)

            CoinUpdate.execute(wallet, appContext)

            val resultJson = buildJsonObject {
                putJsonArray("coins") {
                    wallet.coins.forEach { coin ->
                        add(buildJsonObject {
                            put("statechain_id", coin.statechainId!!)
                            put("amount", coin.amount.toString())
                            put("status", coin.status.toString())
                            put("deposit_address", coin.aggregatedAddress!!)
                        })
                    }
                }
            }

            println(resultJson)
        }
    }
}