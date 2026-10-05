# EventPulse 🎟️

EventPulse is a modern, offline-first Android application built for personalized event discovery, seamless ticketing, and complete event management. Built with Kotlin and MVVM architecture, it connects event-goers with local experiences while providing organizers and admins with robust management tools.

## 👥 User Roles & Features

*   **User:** Discover nearby events using location services, book tickets with integrated payments, and access offline QR code tickets via local caching.
*   **Organizer:** Create events, upload banners, and scan attendee QR code tickets at the gate using the device camera.
*   **Admin:** Oversee the platform, approve pending events, and monitor platform metrics.

## 🛠️ Tech Stack

*   **Language:** Kotlin
*   **UI:** Jetpack Compose 
*   **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
*   **Local Storage:** Room Database (Offline-first tickets) & DataStore
*   **Networking:** Retrofit & OkHttp
*   **Asynchronous:** Kotlin Coroutines & Flow
*   **Integrations:** Google Maps SDK, Firebase Cloud Messaging, ML Kit (QR Scanning)

## 🚀 Getting Started

### Prerequisites
*   Android Studio (Latest Release)
*   JDK 17+

### Installation

1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/EventPulse-Android.git](https://github.com/yourusername/EventPulse-Android.git)
