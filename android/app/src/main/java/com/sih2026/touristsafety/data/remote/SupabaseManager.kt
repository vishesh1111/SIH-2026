package com.sih2026.touristsafety.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseManager @Inject constructor() {
    private val supabaseUrl = "https://gtajicprsshvxhrjsvyr.supabase.co"
    private val supabaseKey = "sb_publishable_U-ktoa40OjATNS6F46xvng_tuTLKnfc"

    suspend fun signUpUser(
        fullName: String,
        email: String,
        phone: String,
        nationality: String,
        gender: String,
        password: String = "test123"
    ): String? = withContext(Dispatchers.IO) {
        try {
            // 1. Call Supabase Auth Signup
            val authBody = JSONObject().apply {
                put("email", email)
                put("password", password)
                val userData = JSONObject().apply {
                    put("full_name", fullName)
                    put("phone", phone)
                    put("nationality", nationality)
                    put("gender", gender.lowercase())
                }
                put("data", userData)
            }

            val authUrl = URL("$supabaseUrl/auth/v1/signup")
            val authConn = authUrl.openConnection() as HttpURLConnection
            authConn.requestMethod = "POST"
            authConn.setRequestProperty("apikey", supabaseKey)
            authConn.setRequestProperty("Content-Type", "application/json")
            authConn.doOutput = true

            authConn.outputStream.use { os ->
                val input = authBody.toString().toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val authCode = authConn.responseCode
            Log.d("SupabaseManager", "Auth signup response code: $authCode")
            val authResponseStr = if (authCode in 200..299) {
                authConn.inputStream.bufferedReader().use { it.readText() }
            } else {
                authConn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            var userId: String? = null
            var accessToken: String? = null

            if (authResponseStr.isNotBlank()) {
                val json = JSONObject(authResponseStr)
                if (json.has("id")) {
                    userId = json.getString("id")
                } else if (json.has("user")) {
                    val userObj = json.getJSONObject("user")
                    userId = userObj.getString("id")
                }
                if (json.has("access_token")) {
                    accessToken = json.getString("access_token")
                }
            }

            if (userId == null) {
                userId = java.util.UUID.randomUUID().toString()
            }

            // 2. Insert into public.profiles table
            insertProfile(
                id = userId,
                fullName = fullName,
                email = email,
                phone = phone,
                nationality = nationality,
                gender = gender,
                token = accessToken
            )

            userId
        } catch (e: Exception) {
            Log.e("SupabaseManager", "signUpUser exception", e)
            null
        }
    }

    suspend fun insertProfile(
        id: String,
        fullName: String,
        email: String,
        phone: String,
        nationality: String,
        gender: String,
        token: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", id)
                put("full_name", fullName)
                put("email", email)
                put("phone", phone)
                put("nationality", nationality)
                put("gender", gender.lowercase())
            }

            val bearerToken = token ?: supabaseKey
            val url = URL("$supabaseUrl/rest/v1/profiles")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("apikey", supabaseKey)
            conn.setRequestProperty("Authorization", "Bearer $bearerToken")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Prefer", "resolution=merge-duplicates,return=representation")
            conn.doOutput = true

            conn.outputStream.use { os ->
                val input = json.toString().toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val code = conn.responseCode
            Log.d("SupabaseManager", "insertProfile response code: $code")
            if (code in 200..299) {
                true
            } else {
                val error = conn.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e("SupabaseManager", "insertProfile error: $error")
                false
            }
        } catch (e: Exception) {
            Log.e("SupabaseManager", "insertProfile exception", e)
            false
        }
    }

    suspend fun fetchProfileByEmail(email: String): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val encodedEmail = java.net.URLEncoder.encode(email, "UTF-8")
            val url = URL("$supabaseUrl/rest/v1/profiles?email=eq.$encodedEmail&select=*")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("apikey", supabaseKey)
            conn.setRequestProperty("Authorization", "Bearer $supabaseKey")
            conn.setRequestProperty("Accept", "application/json")

            val code = conn.responseCode
            if (code in 200..299) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(response)
                if (array.length() > 0) {
                    array.getJSONObject(0)
                } else null
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SupabaseManager", "fetchProfileByEmail exception", e)
            null
        }
    }

    suspend fun insertEmergencyContact(
        id: String,
        userId: String,
        name: String,
        phone: String,
        relationship: String,
        countryCode: String,
        residentAddress: String?,
        isPrimary: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", id)
                put("user_id", userId)
                put("name", name)
                put("phone", phone)
                put("relationship", relationship)
                put("country_code", countryCode)
                put("resident_address", residentAddress ?: "")
                put("is_primary", isPrimary)
            }

            val url = URL("$supabaseUrl/rest/v1/emergency_contacts")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("apikey", supabaseKey)
            conn.setRequestProperty("Authorization", "Bearer $supabaseKey")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Prefer", "resolution=merge-duplicates,return=representation")
            conn.doOutput = true

            conn.outputStream.use { os ->
                val input = json.toString().toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val code = conn.responseCode
            Log.d("SupabaseManager", "insertEmergencyContact response code: $code")
            code in 200..299
        } catch (e: Exception) {
            Log.e("SupabaseManager", "insertEmergencyContact exception", e)
            false
        }
    }
}
