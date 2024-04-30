import org.bitcoinj.core.NetworkParameters
import org.electrumj.Util
import org.electrumj.dto.BlockchainScripthashListUnspentResponseEntry

class CoinUpdate(wallet: Wallet, appContext: AppContext) {

//    fun scripthash(bitcoinjNetowrkParameters: NetworkParameters?, address: String?): String {
//        val addressBitcoinj: Address =
//            org.bitcoinj.core.Address.fromString(bitcoinjNetowrkParameters, address)
//        val script: org.bitcoinj.script.Script = ScriptBuilder.createOutputScript(addressBitcoinj)
//        val scriptArray: ByteArray = script.getProgram()
//        val scriptHash: ByteArray = Sha256Hash.hash(scriptArray)
//        val reversedHash: Sha256Hash = Sha256Hash.wrapReversed(scriptHash)
//        return reversedHash.toString()
//    }

    fun testElect(clientConfig: ClientConfig) {
        val electrumClient = getElectrumClient(clientConfig)

        val netParam = NetworkParameters.fromID(NetworkParameters.ID_TESTNET)
        val scripthash: String = Util.scripthash(netParam,"tb1p8jgrjg4rt4rt9nydw0r2ql9yl77ns069a3czkz3ml7zyc2whfdks0td33p")

        val responseEntries: List<BlockchainScripthashListUnspentResponseEntry> = electrumClient.blockchainScripthashListUnspent(scripthash)

        electrumClient.closeConnection()

        println("responseEntries.count: ${responseEntries.count()}")

        responseEntries.forEach { entry ->

            println("entry.height: ${entry.height}")
            println("entry.txHash: ${entry.txHash}")
            println("entry.txPos: ${entry.txPos}")
            println("entry.value: ${entry.value}")

        }
    }

    fun checkDeposit(coin: Coin, clientConfig: ClientConfig) {

        if (coin.statechainId == null && coin.utxoTxid == null && coin.utxoVout == null) {
            if (coin.status != CoinStatus.INITIALISED) {
                throw IllegalStateException("Coin does not have a statechain ID, a UTXO and the status is not INITIALISED")
            } else {
                return
            }
        }

        val electrumClient = getElectrumClient(clientConfig)

        // TODO: check wallet network
        val netParam = NetworkParameters.fromID(NetworkParameters.ID_TESTNET)
        val scripthash: String = Util.scripthash(netParam, coin.aggregatedAddress)

        val responseEntries: List<BlockchainScripthashListUnspentResponseEntry> = electrumClient.blockchainScripthashListUnspent(scripthash)

        electrumClient.closeConnection()

        println("responseEntries.count: ${responseEntries.count()}")

        responseEntries.forEach { entry ->

            println("entry.height: ${entry.height}")
            println("entry.txHash: ${entry.txHash}")
            println("entry.txPos: ${entry.txPos}")
            println("entry.value: ${entry.value}")

        }
    }

    init {

        val clientConfig = appContext.clientConfig
        val sqliteManager = appContext.sqliteManager

        testElect(clientConfig)

        wallet.coins.forEach { coin ->
            if (coin.status == CoinStatus.INITIALISED || coin.status == CoinStatus.IN_MEMPOOL || coin.status == CoinStatus.UNCONFIRMED) {

            }
        }
    }
}