import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class Deposit: CliktCommand(help = "Generates a new deposit address", name = "new-deposit-address") {

    private val walletName: String by argument(help = "Name of the wallet to create")

    private val appContext: AppContext by lazy {
        requireNotNull(currentContext.findObject() as? AppContext) {
            "ClientConfig not found in context"
        }
    }

    private suspend fun getTokenFromServer() : Token {
        val endpoint = "tokens/token_init"

        val clientConfig = appContext.clientConfig;

        val url = clientConfig.statechainEntity + "/" + endpoint;

        // val client = HttpClient(CIO)
        val httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        val token: Token = httpClient.get(url).body();

        println("token.tokenId: " + token.tokenId);

        return token

    }

    private suspend fun addTokenToWallet(wallet: Wallet) {
        val token = getTokenFromServer()
        token.confirmed = true
        wallet.tokens = wallet.tokens.plus(token)

        println("wallet.tokens.count: ${wallet.tokens.count()}")

        appContext.sqliteManager.updateWallet(wallet)
    }

    override fun run() {
        val wallet = appContext.sqliteManager.loadWallet(walletName)

        runBlocking {
            addTokenToWallet(wallet)
        }

        println("A new token has been added to wallet ${wallet.name}.")
    }
}