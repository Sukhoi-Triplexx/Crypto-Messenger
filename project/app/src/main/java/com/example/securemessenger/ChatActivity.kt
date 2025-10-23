package com.example.securemessenger

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.security.KeyPair
import javax.crypto.SecretKey

class ChatActivity : AppCompatActivity() {

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    
    // Криптографические ключи
    private lateinit var rsaKeyPair: KeyPair
    private lateinit var aesKey: SecretKey
    
    // Для демонстрации будем использовать один ключ для всех сообщений
    // В реальном приложении для каждого сеанса связи генерируется новый ключ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        initViews()
        initCrypto()
        setupRecyclerView()
        setupClickListeners()
        
        // Добавляем приветственное сообщение
        messageAdapter.addMessage(
            Message(
                content = "Добро пожаловать в Secure Messenger! Все сообщения шифруются с помощью AES-256.",
                sender = "Система",
                isEncrypted = false
            )
        )
    }
    
    private fun initViews() {
        messagesRecyclerView = findViewById(R.id.messages_recycler_view)
        messageEditText = findViewById(R.id.message_edit_text)
        sendButton = findViewById(R.id.send_button)
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
            layoutManager = LinearLayoutManager(this@ChatActivity)
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
    
    private fun sendMessage(text: String) {
        try {
            // Шифруем сообщение с помощью AES
            val encryptedBytes = CryptoUtils.encryptWithAES(text.toByteArray(), aesKey)
            val encryptedMessage = CryptoUtils.encodeToBase64(encryptedBytes)
            
            // Создаем объект сообщения
            val message = Message(
                content = encryptedMessage,
                sender = "Вы",
                isEncrypted = true
            )
            
            // Добавляем в список сообщений
            messageAdapter.addMessage(message)
            
            // Прокручиваем к последнему сообщению
            messagesRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
            
            // Имитируем получение ответа
            simulateResponse()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка отправки сообщения: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun simulateResponse() {
        // Имитируем получение зашифрованного сообщения и его расшифровку
        val responses = listOf(
            "Принято. Сообщение зашифровано с помощью AES-256.",
            "Сообщение доставлено и расшифровано.",
            "Шифрование работает корректно.",
            "End-to-end шифрование активно."
        )
        
        val randomResponse = responses.random()
        
        try {
            // Шифруем ответ
            val encryptedBytes = CryptoUtils.encryptWithAES(randomResponse.toByteArray(), aesKey)
            val encryptedMessage = CryptoUtils.encodeToBase64(encryptedBytes)
            
            // Создаем объект сообщения
            val message = Message(
                content = encryptedMessage,
                sender = "Собеседник",
                isEncrypted = true
            )
            
            // Добавляем в список сообщений
            messageAdapter.addMessage(message)
            
            // Прокручиваем к последнему сообщению
            messagesRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка обработки ответа: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}