import kotlinx.serialization.json.Json
import java.sql.DriverManager

class SqliteManager(clientConfig: ClientConfig) {

    private val databaseUrl = "jdbc:sqlite:" + clientConfig.databaseFile

    init {
        createDatabase()
    }

    private fun createDatabase() {
        DriverManager.getConnection(databaseUrl).use { conn ->
            conn.createStatement().use { statement ->
                statement.execute("CREATE TABLE IF NOT EXISTS wallet (wallet_name TEXT NOT NULL UNIQUE, wallet_json TEXT NOT NULL)")
                statement.execute("CREATE TABLE IF NOT EXISTS backup_txs (statechain_id TEXT NOT NULL, txs TEXT NOT NULL)")
            }
        }
    }

    fun insertWallet(wallet: Wallet) {

        val walletJson = Json.encodeToString(Wallet.serializer(), wallet)

        DriverManager.getConnection(databaseUrl).use { conn ->
            conn.prepareStatement("INSERT INTO wallet (wallet_name, wallet_json) VALUES (?, ?)").use { statement ->
                statement.setString(1, wallet.name)
                statement.setString(2, walletJson)
                statement.executeUpdate()
            }
        }
    }

    fun loadWallet(walletName: String) : Wallet {
        DriverManager.getConnection(databaseUrl).use { conn ->
            conn.prepareStatement("SELECT wallet_json FROM wallet WHERE wallet_name = ?").use { statement ->
                statement.setString(1, walletName)
                val rs = statement.executeQuery()
                if (rs.next()) {
                    val walletJson = rs.getString("wallet_json");
                    val wallet = Json.decodeFromString(Wallet.serializer(), walletJson)
                    return wallet
                } else {
                    throw InternalException("Wallet $walletName not found !")
                }
            }
        }
    }

    fun updateWallet(wallet: Wallet) {

        val walletJson = Json.encodeToString(Wallet.serializer(), wallet)

        DriverManager.getConnection(databaseUrl).use { conn ->
            conn.prepareStatement("UPDATE wallet SET wallet_json = ? WHERE wallet_name = ?").use { statement ->
                statement.setString(1, walletJson)
                statement.setString(2, wallet.name)
                statement.executeUpdate()
            }
        }
    }
}