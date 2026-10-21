package com.example.eventpulse.data.repository

import com.example.eventpulse.data.local.SessionManager
import com.example.eventpulse.data.model.Role
import com.example.eventpulse.data.model.User
import com.example.eventpulse.data.remote.EventPulseApi
import kotlinx.coroutines.flow.Flow

/**
 * Repository that handles authentication against `POST /api/auth/register` and
 * `POST /api/auth/login`, then persists the resulting JWT + user metadata via
 * [SessionManager] (DataStore).
 *
 * Both endpoints return the same shape:
 * ```json
 * { "token": "eyJ...", "user": { "id": "...", "name": "...", "email": "...", "role": "USER" } }
 * ```
 */
class AuthRepository(
    private val api: EventPulseApi,
    private val sessionManager: SessionManager
) {

    // ── Register ───────────────────────────────────────────────────────────────

    /**
     * `POST /api/auth/register`
     *
     * @param role Must be `"USER"` or `"ORGANIZER"` (the server ignores `"ADMIN"` here).
     */
    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String = "USER"
    ): Result<User> {
        return try {
            val body = mapOf("name" to name, "email" to email, "password" to password, "role" to role)
            val response = api.register(body)
            if (response.isSuccessful && response.body() != null) {
                val user = parseAuthResponse(response.body()!!)
                Result.success(user)
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Login ──────────────────────────────────────────────────────────────────

    /**
     * `POST /api/auth/login`
     *
     * Saves JWT, role, and user ID to [SessionManager] on success.
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val cleanEmail = email.trim()
            val cleanPassword = password.trim()
            val body = mapOf("email" to cleanEmail, "password" to cleanPassword)
            val response = api.login(body)
            if (response.isSuccessful && response.body() != null) {
                val user = parseAuthResponse(response.body()!!)
                Result.success(user)
            } else {
                val errJson = response.errorBody()?.string()
                val serverMsg = try {
                    com.google.gson.JsonParser.parseString(errJson).asJsonObject.get("message")?.asString
                } catch (_: Exception) { null }
                val errorMsg = serverMsg ?: "Login failed (${response.code()}: ${response.message()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Session ────────────────────────────────────────────────────────────────

    suspend fun logout() = sessionManager.clearSession()

    suspend fun updateProfile(name: String, email: String, phoneNumber: String = ""): Result<User> {
        return try {
            val body = mutableMapOf("name" to name)
            if (phoneNumber.isNotBlank()) body["phoneNumber"] = phoneNumber
            val response = api.updateProfile(body)
            if (response.isSuccessful && response.body() != null) {
                val updated = response.body()!!
                sessionManager.saveUserDetails(updated.name, updated.email)
                Result.success(updated)
            } else {
                sessionManager.saveUserDetails(name, email)
                Result.success(User(name = name, email = email, phoneNumber = phoneNumber))
            }
        } catch (e: Exception) {
            sessionManager.saveUserDetails(name, email)
            Result.success(User(name = name, email = email, phoneNumber = phoneNumber))
        }
    }

    fun getJwtToken(): Flow<String?> = sessionManager.getJwtToken()
    fun getUserRole(): Flow<Role?> = sessionManager.getUserRole()
    fun getUserId(): Flow<String?> = sessionManager.getUserId()
    fun getUserName(): Flow<String?> = sessionManager.getUserName()
    fun getUserEmail(): Flow<String?> = sessionManager.getUserEmail()

    // ── Private helpers ────────────────────────────────────────────────────────

    /**
     * Parses the `{ token, user }` response body returned by both register and login.
     * Saves session data to [SessionManager] as a side-effect.
     */
    @Suppress("UNCHECKED_CAST")
    private suspend fun parseAuthResponse(data: Map<String, Any>): User {
        val token = data["token"] as? String
            ?: throw IllegalStateException("Missing token in auth response")
        val userMap = data["user"] as? Map<*, *>
            ?: throw IllegalStateException("Missing user object in auth response")

        val roleStr = (userMap["role"] as? String) ?: "USER"
        val userRole = try { Role.valueOf(roleStr) } catch (_: IllegalArgumentException) { Role.USER }
        val name = (userMap["name"] as? String) ?: ""
        val email = (userMap["email"] as? String) ?: ""
        val id = (userMap["id"] as? String) ?: ""

        // Persist session
        sessionManager.saveJwtToken(token)
        sessionManager.saveUserRole(userRole)
        sessionManager.saveUserId(id)
        sessionManager.saveUserDetails(name, email)

        return User(
            id = id,
            name = name,
            email = email,
            role = userRole
        )
    }
}
