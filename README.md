# BitizenConnectKotlin

[![](https://jitpack.io/v/BitizenWallet/BitizenConnectKotlin.svg)](https://jitpack.io/#BitizenWallet/BitizenConnectKotlin)

library to use BitizenConnect with Kotlin or Java

## 添加SDK依赖
将@jitpack添加到gradle文件中

```gradle
repositories {
 ...
 maven { url 'https://jitpack.io' }
}
```
添加sdk依赖

从<https://jitpack.io/#BitizenWallet/BitizenConnectKotlin>查找最新版本号

```gradle
dependencies {
 implementation 'com.github.BitizenWallet:BitizenConnectKotlin:40900783c1'
}
```

## 使用方式

### 初始化BitizenConnectApi
```kotlin
val connection = BitizenConnectApi(object :BitizenConnectDelegate{
        override fun didConnect(chainId: Int?, accounts: String?) {
            TODO("连接成功，返回chain id 和钱包地址")
        }

        override fun didDisconnect() {
            TODO("连接断开")
        }

        override fun failedToConnect() {
            TODO("连接失败")
        }

    })
```

### 连接钱包
需要配置deeplink，用于Bitizen App处理完事务后，返回当前App
```xml
<intent-filter >
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <!-- Accepts URIs that begin with "example://gizmos” -->
    <data android:scheme="bitizendapp"
        android:host="wc" />
</intent-filter>
```
调用连接钱包API
```kotlin
connection.connect(
        context = requireContext(),
        dappName = "Example App",
        dappDescription = "bitizenconnect_android_example",
        dappUrl = "https://example.com",
        icons = listOf("用于Bitizen App展示dapp的图片"),
        callbackUrl = "bitizendapp://wc", // 设置返回当前App的deeplink
    )
```

### 签名
SDK 支持 eth sign 、personal sign 和 signTypedData
> eth_sign 是危险操作，会导致资金丢失，Bitizen Wallet 已经把 eth_sign 封禁

```kotlin
connection.personalSign(
            requireContext(),
            message = "0xff",
            account = "钱包地址"
        ) {resp ->
            // 回调方式位于子线程，务必切换到UI线程进行UI展示
            uiScope.launch {    
                binding.txRespData.text = it.result.toString()
            }
        }
```

### 交易
```kotlin
connection.ethSendTransaction(
        requireContext(),
        Transaction(
            from = binding.txAddress.text.toString(),
            to = binding.txAddress.text.toString(),
            nonce = null,
            gasPrice = null,
            gasLimit = null,
            value = "0xff",
            data = "0x",
        )
    ) {
        uiScope.launch {
            binding.txRespData.text = it.result.toString()
        }
    }
```

### SDK Demo 
相关操作演示，请参考[BitizenConnectKotlin-Example](https://github.com/BitizenWallet/BitizenConnectKotlin-Example)
