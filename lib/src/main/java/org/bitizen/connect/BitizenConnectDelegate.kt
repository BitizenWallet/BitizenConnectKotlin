package org.bitizen.connect

interface BitizenConnectDelegate {
    fun failedToConnect() // 断开websocket链接
    fun didConnect(chainId: Int?,accounts: String?) // 成功选择连接钱包
    fun didDisconnect() // 断开和钱包的连接，针对于disconnect
}