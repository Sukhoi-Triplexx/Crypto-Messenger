package com.example.securemessenger

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.security.KeyPair
import javax.crypto.SecretKey

class RealChatActivity : AppCompatActivity() {
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var connectButton: Button
    private lateinit var statusText: TextView
    
    // Криптографические ключи
    private lateinit var rsaKeyPair: KeyPair
    private lateinit var aesKey: SecretKey
    
    // Сетевой менеджер
    private var networkManager: NetworkManager? = null
    private var username: String = "Пользователь"
    private var serverIp: String = "192.168.1.100"
    private var serverPort: Int = 8080
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        initViews()
        initCrypto()
        setupRecyclerView()
        setupClickListeners()
        showConnectionDialog()
        
        // Добавляем приветственное сообщение
        messageAdapter.addMessage(
            Message(
                content = "Добро пожаловать в Secure Messenger! Подключитесь к серверу для начала общения.",
                sender = "Система",
                isEncrypted = false
            )
        )
    }
    
    private fun initViews() {
        messagesRecyclerView = findViewById(R.id.messages_recycler_view)
        messageEditText = findViewById(R.id.message_edit_text)
        sendButton = findViewById(R.id.send_button)
        connectButton = Button(this).apply {
            text = "Подключиться"
        }
        statusText = TextView(this).apply {
            text = "Не подключено"
        }
    }
    
    private fun initCrypto() {
        try {
            rsaKeyPair = CryptoUtils.generateRSAKeyPair()
            aesKey = CryptoUtils.generateAESKey()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка инициализации криптографии: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(mutableListOf())
        messagesRecyclerView.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(this@RealChatActivity)
        }
    }
    
    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageEditText.setText("")
            }
        }
    }
    
    private fun showConnectionDialog() {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }
        
        val usernameEditText = EditText(this).apply {
            hint = "Имя пользователя"
            setText(username)
        }
        
        val ipEditText = EditText(this).apply {
            hint = "IP адрес сервера"
            setText(serverIp)
        }
        
        val portEditText = EditText(this).apply {
            hint = "Порт"
            setText(serverPort.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        
        dialogView.addView(usernameEditText)
        dialogView.addView(ipEditText)
        dialogView.addView(portEditText)
        
        AlertDialog.Builder(this)
            .setTitle("Подключение к серверу")
            .setView(dialogView)
            .setPositiveButton("Подключиться") { _, _ ->
                username = usernameEditText.text.toString().trim().takeIf { it.isNotEmpty() } ?: "Пользователь"
                serverIp = ipEditText.text.toString().trim().takeIf { it.isNotEmpty() } ?: "192.168.1.100"
                serverPort = portEditText.text.toString().trim().toIntOrNull() ?: 8080
                
                connectToServer()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun connectToServer() {
        try {
            networkManager = NetworkManager(
                serverIp = serverIp,
                serverPort = serverPort,
                username = username,
                onMessageReceived = { message ->
                    runOnUiThread {
                        // Шифруем входящее сообщение
                        val encryptedMessage = encryptMessage(message.content)
                        val encryptedMsg = message.copy(
                            content = encryptedMessage,
                            isEncrypted = true
                        )
                        messageAdapter.addMessage(encryptedMsg)
                        messagesRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                    }
                },
                onConnectionStatusChanged = { connected ->
                    runOnUiThread {
                        if (connected) {
                            statusText.text = "Подключено к $serverIp:$serverPort"
                            messageAdapter.addMessage(
                                Message(
                                    content = "Успешно подключено к серверу",
                                    sender = "Система",
                                    isEncrypted = false
                                )
                            )
                        } else {
                            statusText.text = "Отключено"
                            messageAdapter.addMessage(
                                Message(
                                    content = "Потеряно соединение с сервером",
                                    sender = "Система",
                                    isEncrypted = false
                                )
                            )
                        }
                    }
                }
            )
            
            networkManager?.connect()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка подключения: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun sendMessage(text: String) {
        try {
            // Шифруем сообщение с помощью AES
            val encryptedBytes = CryptoUtils.encryptWithAES(text.toByteArray(), aesKey)
            val encryptedMessage = CryptoUtils.encodeToBase64(encryptedBytes)
            
            // Отправляем через сеть
            networkManager?.sendMessage(encryptedMessage)
            
            // Создаем объект сообщения для отображения
            val message = Message(
                content = encryptedMessage,
                sender = username,
                isEncrypted = true
            )
            
            // Добавляем в список сообщений
            messageAdapter.addMessage(message)
            
            // Прокручиваем к последнему сообщению
            messagesRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка отправки сообщения: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun encryptMessage(text: String): String {
        try {
            val encryptedBytes = CryptoUtils.encryptWithAES(text.toByteArray(), aesKey)
            return CryptoUtils.encodeToBase64(encryptedBytes)
        } catch (e: Exception) {
            return "[Ошибка шифрования]"
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        networkManager?.disconnect()
    }
}