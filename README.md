# BitizenConnectKotlin

[![](https://jitpack.io/v/BitizenWallet/BitizenConnectKotlin.svg)](https://jitpack.io/#BitizenWallet/BitizenConnectKotlin)

library to use BitizenConnect with Kotlin or Java

## Add this library to your project

Add the @jitpack repository to your gradle file:

```gradle
repositories {
 ...
 maven { url 'https://jitpack.io' }
}
```

Add the dependency: get the latest release at <https://jitpack.io/#BitizenWallet/BitizenConnectKotlin>

```gradle
dependencies {
 implementation 'com.github.BitizenWallet:BitizenConnectKotlin:12a132c098'
}
```

## Basic usage

The sample application is in [/sample](/sample), search for `//FIXME: for sample app` to enable `:sample:app`.

```kotlin
// new client
val client = BCClient(
 // dapp metadata
 Session.PeerMeta(
  name = "Example App",
  url = "https://example.com",
  description = "example app",
  icons = listOf("https://bitizen.org/wp-content/uploads/2022/04/cropped-vi-192x192.png")
 ),
 MoshiPayloadAdapter(moshi),
 OkHttpTransport.Builder(OkHttpClient.Builder().build(), moshi),
 ::handleStatus,
 ::handleMessages
)

// connect to Bitizen
var url = client?.connect(
 Client.Config(
  bridge = "https://bridge.walletconnect.org",
  peerId = null,
  topic = null,
  key = null
 )
) { resp ->
 if (resp.result != null) {
  // get authorized accounts
  val param = resp.result as Map<*, *>
  accounts = param.get("accounts") as List<String>
 }
}

// open Bitizen by universal link
val i = Intent(Intent.ACTION_VIEW)
i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
i.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
i.data = Uri.parse(url)
startActivity(i)
```

For more example in the sample application.
