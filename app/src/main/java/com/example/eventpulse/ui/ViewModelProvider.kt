package com.example.eventpulse.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventpulse.data.local.SessionManager
import com.example.eventpulse.data.remote.RetrofitClient
import com.example.eventpulse.data.repository.AuthRepository
import com.example.eventpulse.data.repository.BookingRepository
import com.example.eventpulse.data.repository.ChatRepository
import com.example.eventpulse.data.repository.EventRepository
import com.example.eventpulse.ui.admin.AdminViewModel
import com.example.eventpulse.ui.auth.AuthViewModel
import com.example.eventpulse.ui.chat.ChatViewModel
import com.example.eventpulse.ui.organizer.OrganizerViewModel
import com.example.eventpulse.ui.user.UserViewModel

class EventPulseViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    
    private val sessionManager by lazy { SessionManager(context) }
    private val api by lazy { RetrofitClient.provideApi(sessionManager) }
    
    private val authRepository by lazy { AuthRepository(api, sessionManager) }
    private val eventRepository by lazy { EventRepository(api) }
    private val bookingRepository by lazy { BookingRepository(api) }
    private val chatRepository by lazy { ChatRepository(api) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                UserViewModel(eventRepository, bookingRepository) as T
            }
            modelClass.isAssignableFrom(OrganizerViewModel::class.java) -> {
                OrganizerViewModel(eventRepository, bookingRepository) as T
            }
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> {
                AdminViewModel() as T
            }
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(chatRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

@Composable
fun rememberViewModelFactory(): EventPulseViewModelFactory {
    val context = LocalContext.current
    return remember { EventPulseViewModelFactory(context.applicationContext) }
}

@Composable
inline fun <reified T : ViewModel> eventPulseViewModel(): T {
    val factory = rememberViewModelFactory()
    return viewModel(factory = factory)
}
