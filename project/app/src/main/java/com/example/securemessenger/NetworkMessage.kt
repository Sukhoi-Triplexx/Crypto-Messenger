package com.example.securemessenger

import com.google.gson.Gson

data class NetworkMessage(
    val type: String,
    val content: String,
    val sender: String,
    val recipient: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        private val gson = Gson()
        
        fun fromJson(json: String): NetworkMessage {
            return gson.fromJson(json, NetworkMessage::class.java)
        }
    }
    
    fun toJson(): String {
        return gson.toJson(this)
    }
}