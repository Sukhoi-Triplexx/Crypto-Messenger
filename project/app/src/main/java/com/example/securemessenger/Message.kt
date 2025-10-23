package com.example.securemessenger

data class Message(
    val id: Long = System.currentTimeMillis(),
    val content: String,
    val sender: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isEncrypted: Boolean = true
)