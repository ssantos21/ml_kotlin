import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import org.electrumj.ElectrumClient
import java.sql.DriverManager

fun splitUrl(electrumServerUrl: String): Triple<String, String, Int> {
    val protocolEndIndex = electrumServerUrl.indexOf("://")
    if (protocolEndIndex == -1) throw IllegalArgumentException("Invalid URL: Protocol delimiter not found")

    val protocol = electrumServerUrl.substring(0, protocolEndIndex)
    val remainder = electrumServerUrl.substring(protocolEndIndex + 3)

    val colonIndex = remainder.lastIndexOf(':')
    if (colonIndex == -1) throw IllegalArgumentException("Invalid URL: Port delimiter not found")

    val host = remainder.substring(0, colonIndex)
    val port = remainder.substring(colonIndex + 1).toIntOrNull()
        ?: throw IllegalArgumentException("Invalid URL: Port is not an integer")

    return Triple(protocol, host, port)
}

fun getElectrumClient(clientConfig: ClientConfig): ElectrumClient{

    val (protocol, host, port) = splitUrl(clientConfig.electrumServer)

    val electrumClient = ElectrumClient(host, port)

    var isSecure = false

    if (protocol == "ssl") {
        isSecure = true
    }

    electrumClient.openConnection(isSecure)

    return electrumClient;
}

suspend fun getInfoConfig(clientConfig: ClientConfig): InfoConfig {
    val endpoint = "info/config"

    // TODO: add support to Tor

    val url = clientConfig.statechainEntity + "/" + endpoint;

    // val client = HttpClient(CIO)
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    val serverConfig: ServerConfig = httpClient.get(url).body();

    httpClient.close();

    val electrumClient = getElectrumClient(clientConfig)

    var feeRateBtcPerKb = electrumClient.blockchainEstimatefee(3);

    if (feeRateBtcPerKb <= 0.0) {
        feeRateBtcPerKb = 0.00001;
    }

    val feeRateSatsPerByte = (feeRateBtcPerKb * 100000.0).toULong();

    electrumClient.closeConnection()

    return InfoConfig(
        serverConfig.initlock,
        serverConfig.interval,
        feeRateSatsPerByte
    )
}

data class AppContext(
    val clientConfig: ClientConfig,
    val sqliteManager: SqliteManager
)

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
class MainCommand : CliktCommand() {
    private val clientConfig = ClientConfig()
    private val sqliteManager = SqliteManager(clientConfig)

    init {

        context {
            obj = AppContext(clientConfig, sqliteManager)
        }
    }

    override fun run() = Unit // Main command does nothing on its own
}

fun main(args: Array<String>) = MainCommand()
    .subcommands(CreateWallet(), Deposit())
    .main(args)

/*
fun main() {
    val name = "Kotlin"
    //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
    // to see how IntelliJ IDEA suggests fixing it.
    println("Hello, " + name + "!")

    for (i in 1..5) {
        //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
        // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
        println("i = $i")
    }

    try {
        val mnemonic = generateMnemonic()
        println("Generated Mnemonic: $mnemonic")
    } catch (e: MercuryException) {
        println("Failed to generate mnemonic: ${e.message}")
    }

}
 */