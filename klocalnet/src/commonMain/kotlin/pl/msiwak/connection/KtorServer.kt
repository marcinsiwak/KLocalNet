package pl.msiwak.connection

import kotlinx.coroutines.flow.Flow
import pl.msiwak.connection.model.WebSocketEvent

internal interface KtorServer {

    val messages: Flow<WebSocketEvent>
    suspend fun startServer(host: String, port: Int)
    suspend fun stopServer()

    suspend fun sendMessage(userId: String, message: String)
    suspend fun sendMessageToAll(message: String)

    suspend fun closeSocket(userId: String)

    suspend fun closeAllSockets()

    fun isRunning(): Boolean
}