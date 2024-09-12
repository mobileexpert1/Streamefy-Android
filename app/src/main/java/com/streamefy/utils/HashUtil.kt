package com.streamefy.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant

object HashUtil {

    // Generate the expiration timestamp (Unix time in seconds)
    @RequiresApi(Build.VERSION_CODES.O)
    fun getExpirationTimestamp(): Long {
        val now = Instant.now()
        val expirationTime = now.plus(Duration.ofHours(2))
        return expirationTime.epochSecond
    }

    // Compute SHA-256 hash and return it as a hexadecimal string
    fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { String.format("%02x", it) }
    }

    // Generate the hash for the given token_security_key, video_id, and expiration
    fun generateHash(tokenSecurityKey: String, videoId: String, expiration: Long): String {
        val input = "$tokenSecurityKey$videoId$expiration"
        return sha256Hex(input)
    }


    fun main() {
        val signerObject = TokenSigner()
        val securityKey = "229248f0-f007-4bf9-ba1f-bbf1b4ad9d40"
        val expiry = "315569520" // in seconds
        val pathAllowed = "/"
        try {
            System.out.println(
                signerObject.signUrl(
                    "https://token-tester.b-cdn.net/300kb.jpg", securityKey, expiry,
                    null, false, pathAllowed, "CA", null
                )
            )
        } catch (e: Exception) {
            println("Failed to sign")
        }
    }


}