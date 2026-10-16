package com.example.eventpulse.data.repository

import com.example.eventpulse.data.local.SessionManager
import com.example.eventpulse.data.model.Role
import com.example.eventpulse.data.model.User
import com.example.eventpulse.data.remote.EventPulseApi
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val api: EventPulseApi,
    private val sessionManager: SessionManager
) {
    
    suspend fun register(name: String, email: String, password: String, role: String): Result<User> {
        return try {
            val body = mapOf(
                "name" to name,
                "email" to email,
                "password" to password,
                "role" to role
            )
            val response = api.register(body)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val body = mapOf(
                "email" to email,
                "password" to password
            )
            val response = api.login(body)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val token = data["token"] as? String
                val user = data["user"] as? Map<*, *>
                
                if (token != null && user != null) {
                    sessionManager.saveJwtToken(token)
                    val userRole = Role.valueOf((user["role"] as? String) ?: "USER")
                    sessionManager.saveUserRole(userRole)
                    sessionManager.saveUserId((user["id"] as? String) ?: "")
                    
                    Result.success(User(
                        id = (user["id"] as? String) ?: "",
                        name = (user["name"] as? String) ?: "",
                        email = (user["email"] as? String) ?: "",
                        role = userRole
                    ))
                } else {
                    Result.failure(Exception("Invalid response format"))
                }
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }

    fun getJwtToken(): Flow<String?> = sessionManager.getJwtToken()
    
    fun getUserRole(): Flow<Role?> = sessionManager.getUserRole()
    
    fun getUserId(): Flow<String?> = sessionManager.getUserId()
}
