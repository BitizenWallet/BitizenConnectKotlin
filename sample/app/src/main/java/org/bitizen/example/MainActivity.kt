package org.bitizen.example

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import kotlinx.android.synthetic.main.screen_main.*
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.bitizen.connect.Client
import org.bitizen.connect.Session
import org.bitizen.connect.impls.BCClient
import org.bitizen.connect.impls.MoshiPayloadAdapter
import org.bitizen.connect.impls.OkHttpTransport


class MainActivity : Activity() {
    private val TAG: String = "bingo::MainActivity"
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val moshi = Moshi.Builder().build()

    private var accounts: List<String>? = null
    private var client: Client = BCClient(
        MoshiPayloadAdapter(moshi),
        OkHttpTransport.Builder(OkHttpClient.Builder().build(), moshi),
        Session.PeerMeta(
            name = "Example App",
            url = "https://example.com",
            description = "example app",
            icons = listOf("https://bitizen.org/wp-content/uploads/2022/04/cropped-vi-192x192.png")
        ),
        "https://bridge.walletconnect.org",
        ::handleStatus
    )


    private fun handleStatus(status: Session.Transport.Status) {
        Log.e(TAG, "handleStatus $status")
        //TODO nb parse disconnect payload
        when (status) {
            Session.Transport.Status.Disconnected -> sessionClosed()
        }
    }

    private fun sessionConnected() {
        uiScope.launch {
            screen_main_status.text = "Connected"
            screen_main_connect_button.visibility = View.GONE
            screen_main_disconnect_button.visibility = View.VISIBLE
            screen_main_tx_button.visibility = View.VISIBLE
        }
    }

    private fun sessionClosed() {
        uiScope.launch {
            screen_main_status.text = "Disconnected"
            screen_main_connect_button.visibility = View.VISIBLE
            screen_main_disconnect_button.visibility = View.GONE
            screen_main_tx_button.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_main)
    }

    @SuppressLint("NewApi")
    override fun onStart() {
        super.onStart()
        screen_main_connect_button.setOnClickListener {
            val url = client.connect { resp ->
                Log.e("bingo", "connect: $resp")
                if (resp.result != null) {
                    val param = resp.result as Map<String, Any>
                    accounts = param.get("accounts") as List<String>
                    sessionConnected()
                }
            }
            Log.e(TAG, "onStart: $url")
//            val i = Intent(Intent.ACTION_VIEW)
//            i.data = Uri.parse(url)
//            startActivity(i)
        }
        screen_main_disconnect_button.setOnClickListener {
            client.kill()
        }
        screen_main_tx_button.setOnClickListener {
            Log.e(TAG, "screen_main_tx_button: clicked")
            client.performMethodCall(
                Session.MethodCall.SignMessage(
                    System.currentTimeMillis(),
                    accounts!![0],
                    "0xff",
                )
            ) { resp ->
                Log.e(TAG, "onStart: $resp")
            }
        }
    }

    override fun onDestroy() {
        client.kill()
        super.onDestroy()
    }
}
