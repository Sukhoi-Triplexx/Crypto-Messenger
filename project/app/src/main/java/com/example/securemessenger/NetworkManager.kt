package com.example.securemessenger

import android.util.Log
import java.io.*
import java.net.*
import java.security.PublicKey
import java.util.concurrent.Executors
import javax.crypto.SecretKey

class NetworkManager(
    private val serverIp: String,
    private val serverPort: Int,
    private val username: String,
    private val onMessageReceived: (Message) -> Unit,
    private val onConnectionStatusChanged: (Boolean) -> Unit
) {
    private var socket: Socket? = null
    private var outputStream: PrintWriter? = null
    private var inputStream: BufferedReader? = null
    private var isConnected = false
    private var isRunning = false
    private val executor = Executors.newSingleThreadExecutor()
    
    companion object {
        private const val TAG = "NetworkManager"
    }
    
    fun connect() {
        executor.execute {
            try {
                socket = Socket(serverIp, serverPort)
                outputStream = PrintWriter(socket?.getOutputStream(), true)
                inputStream = BufferedReader(InputStreamReader(socket?.getInputStream()))
                
                isConnected = true
                isRunning = true
                onConnectionStatusChanged(true)
                
                // Send username to server
                sendUsername()
                
                // Start listening for messages
                listenForMessages()
            } catch (e: Exception) {
                Log.e(TAG, "Connection error: ${e.message}")
                isConnected = false
                onConnectionStatusChanged(false)
            }
        }
    }
    
    private fun sendUsername() {
        try {
            val message = NetworkMessage(
                type = "USERNAME",
                content = username,
                sender = username
            )
            sendMessage(message)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending username: ${e.message}")
        }
    }
    
    fun sendMessage(content: String, recipient: String? = null) {
        if (!isConnected) return
        
        executor.execute {
            try {
                val message = NetworkMessage(
                    type = "MESSAGE",
                    content = content,
                    sender = username,
                    recipient = recipient
                )
                val json = message.toJson()
                outputStream?.println(json)
                outputStream?.flush()
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message: ${e.message}")
            }
        }
    }
    
    private fun listenForMessages() {
        try {
            while (isRunning && isConnected) {
                val line = inputStream?.readLine()
                if (line != null) {
                    val message = NetworkMessage.fromJson(line)
                    when (message.type) {
                        "MESSAGE" -> {
                            val displayMessage = Message(
                                content = message.content,
                                sender = message.sender,
                                isEncrypted = false // Will be encrypted at the UI level
                            )
                            onMessageReceived(displayMessage)
                        }
                        "USER_JOINED" -> {
                            val displayMessage = Message(
                                content = "${message.sender} присоединился к чату",
                                sender = "Система",
                                isEncrypted = false
                            )
                            onMessageReceived(displayMessage)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listening for messages: ${e.message}")
            isConnected = false
            onConnectionStatusChanged(false)
        }
    }
    
    fun disconnect() {
        isRunning = false
        isConnected = false
        try {
            outputStream?.close()
            inputStream?.close()
            socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing connection: ${e.message}")
        }
        onConnectionStatusChanged(false)
    }
    
    fun isConnected(): Boolean = isConnected
}