package org.bitizen.connect.impls

import org.bitizen.connect.Client
import org.bitizen.connect.Session
import org.bitizen.connect.nullOnThrow
import org.komputing.khex.extensions.toHexString
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class BCClient(
    private val payloadAdapter: Session.PayloadAdapter,
    transportBuilder: Session.Transport.Builder,
    clientMeta: Session.PeerMeta,
    bridgeURL: String,
) : Client {

    private val keyLock = Any()
    private val clientMeta = clientMeta

    // Persisted state
    private var peerId: String = UUID.randomUUID().toString()
    private var currentKey: String = MoshiPayloadAdapter.createRandomBytes(32).toHexString()

    private var approvedAccounts: List<String>? = null
    private var chainId: Long? = null
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
        transportBuilder.build(wcURL.toWCUri(), ::handleStatus, ::handleMessage)
    private val requests: MutableMap<Long, (Session.MethodCall.Response) -> Unit> =
        ConcurrentHashMap()
    private val sessionCallbacks: MutableSet<Session.Callback> =
        Collections.newSetFromMap(ConcurrentHashMap<Session.Callback, Boolean>())

    fun connect(): String {
        send(
            Session.MethodCall.SessionRequest(
                WCSession.createCallId(),
                Session.PeerData(id = wcURL.handshakeTopic, clientMeta)
            ),
            topic = peerId,
            callback = { resp ->
                System.out.println(resp.toString())
            }
        )
        return "https://bitizen.org/wallet/wc?uri=" + wcURL.toWCUri()
    }

    override fun addCallback(cb: Session.Callback) {
        sessionCallbacks.add(cb)
    }

    override fun removeCallback(cb: Session.Callback) {
        sessionCallbacks.remove(cb)
    }

    override fun clearCallbacks() {
        sessionCallbacks.clear()
    }

    private fun propagateToCallbacks(action: Session.Callback.() -> Unit) {
        sessionCallbacks.forEach {
            try {
                it.action()
            } catch (t: Throwable) {
                // If error propagation fails, don't try again
                nullOnThrow { it.onStatus(Session.Status.Error(t)) }
            }
        }
    }

    override fun approvedAccounts(): List<String>? = approvedAccounts

//    override fun init() {
//        if (transport.connect()) {
//            // Register for all messages for this client
//            transport.send(
//                Session.Transport.Message(
//                    config.handshakeTopic, "sub", ""
//                )
//            )
//        }
//    }


    override fun performMethodCall(
        call: Session.MethodCall,
        callback: ((Session.MethodCall.Response) -> Unit)?
    ) {
        send(call, callback = callback)
    }

    private fun handleStatus(status: Session.Transport.Status) {
        when (status) {
            Session.Transport.Status.Connected -> {
                // Register for all messages for this client
                transport.send(
                    Session.Transport.Message(
                        wcURL.handshakeTopic, "sub", ""
                    )
                )
            }
        }
        propagateToCallbacks {
            onStatus(
                when (status) {
                    Session.Transport.Status.Connected -> Session.Status.Connected
                    Session.Transport.Status.Disconnected -> Session.Status.Disconnected
                    is Session.Transport.Status.Error -> Session.Status.Error(
                        Session.TransportError(
                            status.throwable
                        )
                    )
                }
            )
        }
    }

    private fun handleMessage(message: Session.Transport.Message) {
        if (message.type != "pub") return
        val data: Session.MethodCall
        synchronized(keyLock) {
            try {
                data = payloadAdapter.parse(message.payload, decryptionKey)
            } catch (e: Exception) {
                handlePayloadError(e)
                return
            }
        }
        var accountToCheck: String? = null
        when (data) {
            is Session.MethodCall.SessionRequest -> {
                peerId = data.peer.id
            }
            is Session.MethodCall.SessionUpdate -> {
                if (!data.params.approved) {
                    endSession()
                }
                // TODO handle session update -> not important for our usecase
            }
            is Session.MethodCall.SendTransaction -> {
                accountToCheck = data.from
            }
            is Session.MethodCall.SignMessage -> {
                accountToCheck = data.address
            }
            is Session.MethodCall.Response -> {
                val callback = requests[data.id] ?: return
                callback(data)
            }
        }

        if (accountToCheck?.let { accountCheck(data.id(), it) } != false) {
            propagateToCallbacks { onMethodCall(data) }
        }
    }

    private fun accountCheck(id: Long, address: String): Boolean {
        approvedAccounts?.find { it.equals(address, ignoreCase = true) } ?: run {
            handlePayloadError(Session.MethodCallException.InvalidAccount(id, address))
            return false
        }
        return true
    }

    private fun handlePayloadError(e: Exception) {
        propagateToCallbacks { Session.Status.Error(e) }
    }

    private fun endSession() {
        approvedAccounts = null
        chainId = null
        internalClose()
        propagateToCallbacks { onStatus(Session.Status.Closed) }
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
        val params = Session.SessionParams(false, null, null, null)
        send(Session.MethodCall.SessionUpdate(WCSession.createCallId(), params))
        endSession()
    }
}
