package org.bitizen.connect

data class Transaction(
    val from: String,
    val to: String?,
    val nonce: String?,
    val gasPrice: String?,
    val gasLimit: String?,
    val value: String,
    val data: String
)