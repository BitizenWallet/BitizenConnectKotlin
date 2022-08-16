package org.bitizen.connect

interface Client {
    fun approvedAccounts(): List<String>?

    fun performMethodCall(
        call: Session.MethodCall,
        callback: ((Session.MethodCall.Response) -> Unit)? = null
    )
    fun kill()

    fun addCallback(cb: Session.Callback)
    fun removeCallback(cb: Session.Callback)
    fun clearCallbacks()
}
