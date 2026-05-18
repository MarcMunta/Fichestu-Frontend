package com.fichestu.frontend.data.remote

import com.fichestu.frontend.BuildConfig
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.net.URLEncoder
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class MatchRealtimeClient(
    private val onMatchChanged: (Int, String) -> Unit
) {
    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var subscribedMatchId: Int? = null
    private var subscribedToken: String? = null
    private val reconnectExecutor = Executors.newSingleThreadScheduledExecutor()
    private var reconnectTask: ScheduledFuture<*>? = null

    fun connect(matchId: Int, token: String) {
        if (subscribedMatchId == matchId && webSocket != null) return
        close()
        subscribedMatchId = matchId
        subscribedToken = token
        openSocket(matchId, token)
    }

    private fun openSocket(matchId: Int, token: String) {
        val encodedToken = URLEncoder.encode(token, Charsets.UTF_8.name())
        val request = Request.Builder()
            .url("${webSocketBaseUrl()}ws/matches?token=$encodedToken")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                reconnectTask?.cancel(false)
                reconnectTask = null
                webSocket.send("""{"type":"SUBSCRIBE_MATCH","matchId":$matchId}""")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val json = runCatching { JsonParser.parseString(text).asJsonObject }.getOrNull() ?: return
                if (json.get("type")?.asString != "MATCH_CHANGED") return
                val eventMatchId = json.get("matchId")?.asInt ?: return
                val event = json.get("event")?.asString ?: "MATCH_CHANGED"
                onMatchChanged(eventMatchId, event)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (subscribedMatchId == matchId) {
                    this@MatchRealtimeClient.webSocket = null
                    scheduleReconnect(matchId, token)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (subscribedMatchId == matchId) {
                    this@MatchRealtimeClient.webSocket = null
                    if (code != 1000) {
                        scheduleReconnect(matchId, token)
                    }
                }
            }
        })
    }

    fun close() {
        reconnectTask?.cancel(false)
        reconnectTask = null
        webSocket?.close(1000, "closed")
        webSocket = null
        subscribedMatchId = null
        subscribedToken = null
    }

    private fun scheduleReconnect(matchId: Int, token: String) {
        if (subscribedMatchId != matchId || subscribedToken != token || reconnectTask?.isDone == false) return
        reconnectTask = reconnectExecutor.schedule({
            if (subscribedMatchId == matchId && subscribedToken == token && webSocket == null) {
                openSocket(matchId, token)
            }
        }, 1500, TimeUnit.MILLISECONDS)
    }

    private fun webSocketBaseUrl(): String {
        val base = BuildConfig.BASE_URL.trimEnd('/') + "/"
        return when {
            base.startsWith("https://") -> base.replaceFirst("https://", "wss://")
            base.startsWith("http://") -> base.replaceFirst("http://", "ws://")
            else -> base
        }
    }
}
