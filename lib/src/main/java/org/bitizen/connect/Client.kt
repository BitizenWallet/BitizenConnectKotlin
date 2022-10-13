package org.bitizen.connect

val LOGO_URI: String = "https://bitizen.org/sdk/assets/logo.png"

interface Client {
    data class Config(
        val bridge: String,
        var topic: String?,
        var key: String?,
        var peerId: String?,
    )

    fun ethSign(message: String, account: String,callback: (Session.MethodCall.Response) -> Unit)
    fun personalSign(message: String, account: String,callback: (Session.MethodCall.Response) -> Unit)
    fun ethSignTypedData(message: String, account: String,callback: (Session.MethodCall.Response) -> Unit)
    fun ethSendTransaction(transaction: Transaction,callback: (Session.MethodCall.Response) -> Unit)
    fun serialize(): Config?
    fun transport(): Session.Transport?

    fun connect(
        config: Config,
        callback: (Session.MethodCall.Response) -> Unit
    ): String

    fun performMethodCall(
        call: Session.MethodCall,
        callback: ((Session.MethodCall.Response) -> Unit)?
    )

    fun disconnect()
    fun reconnectIfNeeded()
}
