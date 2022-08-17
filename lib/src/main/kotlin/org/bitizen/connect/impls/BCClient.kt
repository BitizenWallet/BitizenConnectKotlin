package org.bitizen.connect.impls

import org.bitizen.connect.Client
import org.bitizen.connect.Session
import org.bitizen.connect.nullOnThrow
import org.komputing.khex.extensions.toNoPrefixHexString
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class BCClient(
    private val payloadAdapter: Session.PayloadAdapter,
    transportBuilder: Session.Transport.Builder,
    clientMeta: Session.PeerMeta,
    bridgeURL: String,
    handleStatus: (Session.Transport.Status) -> Unit,
) : Client {

    private val keyLock = Any()
    private val clientMeta = clientMeta

    // Persisted state
    private var peerId: String = UUID.randomUUID().toString()
    private val currentKey = ByteArray(32).also { Random().nextBytes(it) }.toNoPrefixHexString()

    private val wcURL = Session.Config(
        handshakeTopic = UUID.randomUUID().toString(),
        bridge = bridgeURL,
        key = currentKey
    )

    // Getters
    private val encryptionKey: String
        get() = currentKey

    private val decryptionKey: String
        get() = currentKey

    // Non-persisted state
    private var transport: Session.Transport =
        transportBuilder.build(bridgeURL, handleStatus, ::handleMessage)
    private val requests: MutableMap<Long, (Session.MethodCall.Response) -> Unit> =
        ConcurrentHashMap()

    @Suppress("NewApi")
    override fun connect(callback: (Session.MethodCall.Response) -> Unit): String {
        transport.send(
            Session.Transport.Message(
                peerId, "sub", ""
            )
        )
        send(
            Session.MethodCall.SessionRequest(
                WCSession.createCallId(),
                Session.PeerData(id = peerId, clientMeta)
            ),
            topic = wcURL.handshakeTopic,
            callback
        )
        return "https://bitizen.org/wallet/wc?uri=" + wcURL.toWCUri()
    }

    override fun performMethodCall(
        call: Session.MethodCall,
        callback: ((Session.MethodCall.Response) -> Unit)?
    ) {
        send(call, callback = callback)
    }

    @Suppress("NewApi")
    private fun handleMessage(message: Session.Transport.Message) {
        println("bingo handleMessage $message")
        if (message.type != "pub") return
        val data: Session.MethodCall
        synchronized(keyLock) {
            try {
                data = payloadAdapter.parse(message.payload, decryptionKey)
            } catch (e: Exception) {
                return
            }
        }
        println("bingo handleMessage parse ${data::class.java.typeName} $data")
        when (data) {
            is Session.MethodCall.Response -> {
                val callback = requests[data.id] ?: return
                callback(data)
            }
        }
    }

    // Returns true if method call was handed over to transport
    private fun send(
        msg: Session.MethodCall,
        topic: String? = peerId,
        callback: ((Session.MethodCall.Response) -> Unit)? = null
    ): Boolean {
        topic ?: return false

        val payload: String
        synchronized(keyLock) {
            payload = payloadAdapter.prepare(msg, encryptionKey)
        }
        callback?.let {
            requests[msg.id()] = callback
        }
        transport.send(Session.Transport.Message(topic, "pub", payload))
        return true
    }

    private fun internalClose() {
        transport.close()
    }

    override fun kill() {
        internalClose()
    }
}
