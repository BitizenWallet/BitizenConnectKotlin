package org.bitizen.connect

interface Client {
    fun connect(callback: (Session.MethodCall.Response) -> Unit): String

    fun performMethodCall(
        call: Session.MethodCall,
        callback: ((Session.MethodCall.Response) -> Unit)?
    )

    fun kill()
}
