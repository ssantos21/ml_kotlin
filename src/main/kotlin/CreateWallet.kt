import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class CreateWallet : CliktCommand(help = "Creates a new wallet") {
    private val walletName: String by argument(help = "Name of the wallet to create")

    private val clientConfig: ClientConfig by lazy {
        requireNotNull(currentContext.findObject() as? ClientConfig) {
            "ClientConfig not found in context"
        }
    }

    override fun run() {
        println("Wallet '$walletName' has been created successfully.")

        var mnemonic: String? = null

        try {
            mnemonic = generateMnemonic()
            println("Generated Mnemonic: $mnemonic")
        } catch (e: MercuryException) {
            println("Failed to generate mnemonic: ${e.message}")
        }

        println("clientConfig.statechainEntity is " + clientConfig.statechainEntity)

        var infoConfig: InfoConfig? = null

        // TODO: not recommend for production. Change to use CoroutineScope or another approach
        runBlocking {
            infoConfig = getInfoConfig(clientConfig)
        }

        if (infoConfig == null) {
            println("ERROR: infoConfig is null.")
        }

        val electrumClient = getElectrumClient(clientConfig)

        val blockHeader = electrumClient.blockchainHeadersSubscribe()
        val blockheight = blockHeader.height.toUInt();

        electrumClient.closeConnection()

        val notifications = false;
        val tutorials = false;

        val (electrumProtocol, electrumHost, electrumPort) = splitUrl(clientConfig.electrumServer)

        val settings = Settings(
            clientConfig.network,
            null,
            null,
            null,
            null,
            null,
            clientConfig.statechainEntity,
            null,
            electrumProtocol,
            electrumHost,
            electrumPort.toString(),
            clientConfig.electrumType,
            notifications,
            tutorials
        )

        val mutableTokenList: MutableList<Token> = mutableListOf()
        val mutableActivityList: MutableList<Activity> = mutableListOf()
        val mutableCoinList: MutableList<Coin> = mutableListOf()

        val wallet = Wallet(
            walletName,
            mnemonic!!,
            "0.0.1",
            clientConfig.statechainEntity,
            clientConfig.electrumServer,
            clientConfig.network,
            blockheight,
            infoConfig!!.initlock,
            infoConfig!!.interval,
            mutableTokenList,
            mutableActivityList,
            mutableCoinList,
            settings
        )

        val jsonWallet = Json.encodeToString(Wallet.serializer(), wallet)
        println(jsonWallet)




    }
}