package com.example.securemessenger

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.start_chat_button)
        startButton.setOnClickListener {
            // Пока просто переходим к чату
            startActivity(Intent(this, RealChatActivity::class.java))
        }
    }
}