package org.bitizenexample

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.screen_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bitizen.connect.*
import org.komputing.khex.extensions.toHexString


private const val TAG: String = "example.MainActivity"

class MainActivity : Activity(),BitizenConnectDelegate {
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val moshi = Moshi.Builder().build()

    private var accounts: List<String>? = null

    //    private var client: Client? = null
    private val realClient = BitizenConnectApi(this)
//    private fun handleStatus(status: Session.Transport.Status) {
//        Log.e(TAG, "handleStatus $status")
//        when (status) {
//            Session.Transport.Status.Disconnected -> sessionClosed()
//            Session.Transport.Status.Connected -> sessionConnected()
//            else -> {}
//        }
//    }

//    private fun handleMessages(message: Session.MethodCall) {
//        Log.e(TAG, "handleMessages $message")
//        when (message) {
//            is Session.MethodCall.SessionUpdate -> sessionUpdate(message)
//            else -> {}
//        }
//    }

    private fun sessionUpdate(update: Session.MethodCall.SessionUpdate) {
        if (update.params.accounts == null) {
            sessionClosed()
        } else {
            accounts = update.params.accounts
            sessionConnected()
        }
    }

    private fun sessionConnected() {
        if (accounts?.isNotEmpty() == true)
            uiScope.launch {
                screen_main_status.text = "Connected"
                screen_main_connect_button_group.visibility = View.GONE
                screen_main_disconnect_button.visibility = View.VISIBLE
                screen_main_reconnect_button.visibility = View.VISIBLE
                screen_main_eth_sign_button.visibility = View.VISIBLE
                screen_main_personal_sign_button.visibility = View.VISIBLE
                screen_main_eth_signTypedData_button.visibility = View.VISIBLE
                screen_main_send_transaction_button.visibility = View.VISIBLE
            }
    }

    private fun sessionClosed() {
        uiScope.launch {
            screen_main_status.text = "Disconnected"
            screen_main_connect_button_group.visibility = View.VISIBLE
            screen_main_disconnect_button.visibility = View.GONE
            screen_main_reconnect_button.visibility = View.GONE
            screen_main_eth_sign_button.visibility = View.GONE
            screen_main_personal_sign_button.visibility = View.GONE
            screen_main_eth_signTypedData_button.visibility = View.GONE
            screen_main_send_transaction_button.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_main)
    }

    @SuppressLint("NewApi")
    override fun onStart() {
        super.onStart()
        Glide.with(this).load(LOGO_URI).into(screen_main_connect_button)
        screen_main_connect_button.setOnClickListener {
            realClient.connect(
                context = this@MainActivity,
                dappName = "Example App",
                dappDescription = "FFFFFFFFFFF",
                dappUrl = "https://example.com",
                icons = listOf("https://bitizen.org/wp-content/uploads/2022/04/cropped-vi-192x192.png"),
                callbackUrl = "bitizendapp://wc",
            )
//            Log.e(TAG, "screen_main_connect_button: clicked")
//            client = BCClient(
//                Session.PeerMeta(
//                    name = "Example App",
//                    url = "https://example.com",
//                    description = "example app",
//                    icons = listOf("https://bitizen.org/wp-content/uploads/2022/04/cropped-vi-192x192.png")
//                ),
//                MoshiPayloadAdapter(moshi),
//                OkHttpTransport.Builder(OkHttpClient.Builder().build(), moshi),
//                ::handleStatus,
//                ::handleMessages
//            )
//            var url =
//                client?.connect(
//                    Client.Config(
//                        bridge = "https://bridge.walletconnect.org",
//                        peerId = null,
//                        topic = null,
//                        key = null
//                    )
//                ) { resp ->
//                    if (resp.result != null) {
//                        val param = resp.result as Map<*, *>
//                        accounts = param.get("accounts") as List<String>
//                        sessionConnected()
//                    }
//                }
//            Log.e(TAG, "onStart: $url")
//            screen_main_status.text = url;
//            val i = Intent(Intent.ACTION_VIEW)
//            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            i.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
//            i.data = Uri.parse(url)
//            startActivity(i)
        }
        screen_main_disconnect_button.setOnClickListener {
            realClient.disconnect()
        }
        screen_main_reconnect_button.setOnClickListener {
            // mock app being killed or losing network
//            client?.transport()?.close()
//
//            val config = client?.serialize()
//            client?.connect(config!!) {}
            realClient.reconnectIfNeeded();
        }
        screen_main_eth_sign_button.setOnClickListener {
            realClient.ethSign(this@MainActivity,message = "0xff",accounts!![0]){
                uiScope.launch {
                    screen_main_response.text = it.result.toString()
                }
            }
//            performMethodCall(
//                Session.MethodCall.SignMessage(
//                    System.currentTimeMillis(),
//                    accounts!![0],
//                    "0xff",
//                )
//            ) { resp ->
//                uiScope.launch {
//                    screen_main_response.text = resp.result.toString()
//                }
//            }
        }
        screen_main_personal_sign_button.setOnClickListener {
            realClient.personalSign(this@MainActivity,
                message = "test personal_sign".toByteArray().toHexString("0x"),
                account = accounts!![0]
            ){ resp ->
                uiScope.launch {
                    screen_main_response.text = resp.result.toString()
                }
            }
//            performMethodCall(
//                Session.MethodCall.Custom(
//                    System.currentTimeMillis(),
//                    "personal_sign",
//                    listOf("test personal_sign".toByteArray().toHexString("0x"), accounts!![0])
//                )
//            ) { resp ->
//                uiScope.launch {
//                    screen_main_response.text = resp.result.toString()
//                }
//            }
        }
        screen_main_eth_signTypedData_button.setOnClickListener {
            realClient.ethSignTypedData(this@MainActivity,
                message = "{\"types\":{\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}],\"Person\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"wallet\",\"type\":\"address\"}],\"Mail\":[{\"name\":\"from\",\"type\":\"Person\"},{\"name\":\"to\",\"type\":\"Person\"},{\"name\":\"contents\",\"type\":\"string\"}]},\"primaryType\":\"Mail\",\"domain\":{\"name\":\"Ether Mail\",\"version\":\"1\",\"chainId\":1,\"verifyingContract\":\"0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC\"},\"message\":{\"from\":{\"name\":\"Cow\",\"wallet\":\"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826\"},\"to\":{\"name\":\"Bob\",\"wallet\":\"0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB\"},\"contents\":\"Hello, Bob!\"}}",
                account = accounts!![0],
            ){resp ->
                uiScope.launch {
                    screen_main_response.text = resp.result.toString()
                }

            }
//            performMethodCall(
//                Session.MethodCall.Custom(
//                    System.currentTimeMillis(),
//                    "eth_signTypedData",
//                    listOf(
//                        accounts!![0],
//                        "{\"types\":{\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}],\"Person\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"wallet\",\"type\":\"address\"}],\"Mail\":[{\"name\":\"from\",\"type\":\"Person\"},{\"name\":\"to\",\"type\":\"Person\"},{\"name\":\"contents\",\"type\":\"string\"}]},\"primaryType\":\"Mail\",\"domain\":{\"name\":\"Ether Mail\",\"version\":\"1\",\"chainId\":1,\"verifyingContract\":\"0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC\"},\"message\":{\"from\":{\"name\":\"Cow\",\"wallet\":\"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826\"},\"to\":{\"name\":\"Bob\",\"wallet\":\"0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB\"},\"contents\":\"Hello, Bob!\"}}"
//                    )
//                )
//            ) { resp ->
//                uiScope.launch {
//                    screen_main_response.text = resp.result.toString()
//                }
//            }
        }
        screen_main_send_transaction_button.setOnClickListener {
            realClient.ethSendTransaction(this@MainActivity,Transaction(
                from = accounts!![0],
                to = accounts!![0],
                nonce = null,
                gasPrice = null,
                gasLimit = null,
                value = "0xff",
                data = "0x",
            )){resp ->
                uiScope.launch {
                    screen_main_response.text = resp.result.toString()
                }

            }
//            performMethodCall(
//                Session.MethodCall.SendTransaction(
//                    System.currentTimeMillis(),
//                    accounts!![0],
//                    accounts!![0],
//                    value = "0xff",
//                    data = "0x",
//                    gasLimit = null,
//                    gasPrice = null,
//                    nonce = null
//                )
//            ) { resp ->
//                uiScope.launch {
//                    screen_main_response.text = resp.result.toString()
//                }
//            }
        }
    }

//    private fun performMethodCall(
//        call: Session.MethodCall,
//        callback: ((Session.MethodCall.Response) -> Unit)?
//    ) {
//        val i = Intent(Intent.ACTION_VIEW)
//        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        i.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
//        i.data = Uri.parse("https://bitizen.org/wallet/wc")
//        startActivity(i)
//        client?.performMethodCall(call, callback)
//    }

    override fun onDestroy() {
        realClient.disconnect()
        super.onDestroy()
    }

    override fun failedToConnect() {
        TODO("Not yet implemented")
    }

    override fun didConnect(chainId: Int?, accounts: String?) {
        this@MainActivity.accounts = mutableListOf<String>().apply {
            add(accounts!!)
        }
        sessionConnected()
    }

    override fun didDisconnect() {
        sessionClosed()

    }
}
