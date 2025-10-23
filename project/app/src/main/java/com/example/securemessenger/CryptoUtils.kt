package com.example.securemessenger

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

class CryptoUtils {
    
    companion object {
        private const val RSA_ALGORITHM = "RSA"
        private const val AES_ALGORITHM = "AES"
        private const val RSA_KEY_SIZE = 2048
        private const val AES_KEY_SIZE = 256
        
        /**
         * Генерирует пару RSA ключей
         */
        fun generateRSAKeyPair(): KeyPair {
            val keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM)
            keyPairGenerator.initialize(RSA_KEY_SIZE)
            return keyPairGenerator.generateKeyPair()
        }
        
        /**
         * Генерирует AES ключ
         */
        fun generateAESKey(): SecretKey {
            val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM)
            keyGenerator.init(AES_KEY_SIZE)
            return keyGenerator.generateKey()
        }
        
        /**
         * Шифрует данные с помощью открытого RSA ключа
         */
        fun encryptWithRSA(data: ByteArray, publicKey: PublicKey): ByteArray {
            val cipher = Cipher.getInstance(RSA_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            return cipher.doFinal(data)
        }
        
        /**
         * Расшифровывает данные с помощью приватного RSA ключа
         */
        fun decryptWithRSA(encryptedData: ByteArray, privateKey: PrivateKey): ByteArray {
            val cipher = Cipher.getInstance(RSA_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            return cipher.doFinal(encryptedData)
        }
        
        /**
         * Шифрует данные с помощью AES ключа
         */
        fun encryptWithAES(data: ByteArray, key: SecretKey): ByteArray {
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return cipher.doFinal(data)
        }
        
        /**
         * Расшифровывает данные с помощью AES ключа
         */
        fun decryptWithAES(encryptedData: ByteArray, key: SecretKey): ByteArray {
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, key)
            return cipher.doFinal(encryptedData)
        }
        
        /**
         * Конвертирует SecretKey в ByteArray
         */
        fun secretKeyToByteArray(secretKey: SecretKey): ByteArray {
            return secretKey.encoded
        }
        
        /**
         * Конвертирует ByteArray в SecretKey
         */
        fun byteArrayToSecretKey(keyBytes: ByteArray): SecretKey {
            return SecretKeySpec(keyBytes, AES_ALGORITHM)
        }
        
        /**
         * Кодирует ByteArray в строку Base64
         */
        fun encodeToBase64(data: ByteArray): String {
            return Base64.getEncoder().encodeToString(data)
        }
        
        /**
         * Декодирует строку Base64 в ByteArray
         */
        fun decodeFromBase64(encodedData: String): ByteArray {
            return Base64.getDecoder().decode(encodedData)
        }
    }
}