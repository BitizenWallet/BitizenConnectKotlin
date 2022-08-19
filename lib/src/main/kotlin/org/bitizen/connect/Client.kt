package org.bitizen.connect

interface Client {
    data class Config(
        val bridge: String,
        var topic: String?,
        var key: String?,
        var peerId: String?,
    )

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
}
