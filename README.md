<div align="center">

# 📱 MU Social

### Modern Android Social Media Platform

Build • Connect • Share • Chat

<p>
<img src="https://img.shields.io/badge/Platform-Android-green?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Kotlin-2.0-blueviolet?style=for-the-badge&logo=kotlin"/>
<img src="https://img.shields.io/badge/Jetpack%20Compose-Latest-blue?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Firebase-Backend-orange?style=for-the-badge&logo=firebase"/>
<img src="https://img.shields.io/badge/MVVM-Architecture-red?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Hilt-DI-blue?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Room-Database-green?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Status-Under%20Development-yellow?style=for-the-badge"/>
</p>

A feature-rich Android social media application built with modern Android technologies and Firebase.

⭐ If you like this project, don't forget to star the repository.

</div>

---

# 📖 About

MU Social is a modern Android Social Media Application inspired by today's social networking platforms.

The project is built using **Kotlin**, **Jetpack Compose**, **Firebase**, **MVVM Architecture**, and follows clean Android development practices.

The main objective of this project is to demonstrate real-world Android application development including authentication, media uploads, chat, stories, AI features, notifications, and scalable architecture.

---

# ✨ Features

### 👤 Authentication
- Email Login
- User Registration
- Firebase Authentication
- Secure User Sessions

### 📰 Home Feed
- View Posts
- Like Posts
- Comment Support
- Share Support
- Feed Refresh

### 📸 Stories
- Upload Story
- Story Viewer
- Story Progress
- Story Analytics

### 📷 Posts
- Image Upload
- Video Upload
- AI Caption Generator
- Firebase Storage
- Firestore Database

### 👥 User Profile
- Profile Screen
- Followers
- Following
- Wallet
- Profile Statistics

### 🔍 Search
- Search Users
- User Profiles
- Navigation

### 💬 Chat
- Realtime Chat
- Seen Status
- Typing Indicator
- Image Messages
- Firebase Firestore

### 🔔 Notifications
- Notification Screen
- Activity Feed

### 💰 Wallet
- Coins
- Recharge UI
- Earnings UI
- Withdrawal UI

### 🤖 AI Features
- AI Caption Generator
- Smart Content Assistance

---

# 🏗 Architecture

```
                Jetpack Compose UI
                       │
                ViewModel (MVVM)
                       │
                Repository Layer
               ┌───────────────┐
               │               │
          Firebase        Room Database
               │
               ▼
      Firestore + Storage
```

---

# 🛠 Tech Stack

| Technology | Purpose |
|------------|----------|
| Kotlin | Programming Language |
| Jetpack Compose | Modern UI |
| MVVM | Architecture |
| Firebase Authentication | Login |
| Cloud Firestore | Database |
| Firebase Storage | Media Storage |
| Room Database | Local Cache |
| Hilt | Dependency Injection |
| Coroutines | Async Tasks |
| Navigation Compose | Navigation |
| Coil | Image Loading |
| Agora SDK | Video Calling |
| Gemini AI | AI Caption Generator |

---

# 📂 Project Structure

```
app
│
├── data
├── domain
├── presentation
├── navigation
├── di
├── ui
├── utils
└── MainActivity.kt
```

---

# 📱 Screens

- Login
- Register
- Home
- Search
- Profile
- Create Post
- Stories
- Story Viewer
- Notifications
- Wallet
- Chat List
- Chat Detail

---

# 🚀 Getting Started

## Clone Repository

```bash
git clone https://github.com/ayushshiva/MU-Social.git
```

Open Android Studio

```
File
→ Open
→ MU-Social
```

---

# 🔥 Firebase Setup

Create a Firebase Project and enable:

- Firebase Authentication
- Cloud Firestore
- Firebase Storage

Then place

```
google-services.json
```

inside

```
app/
```

---

# ▶ Build Project

```bash
./gradlew assembleDebug
```

Install APK

```bash
./gradlew installDebug
```

---

# 📊 Development Status

| Module | Status |
|---------|--------|
| Authentication | ✅ |
| Home Feed | ✅ |
| Stories | ✅ |
| Posts | ✅ |
| Search | ✅ |
| Notifications | ✅ |
| Wallet UI | ✅ |
| Firebase Integration | ✅ |
| Chat | 🚧 |
| Edit Profile | 🚧 |
| Video Calling | 🚧 |

---

# 🗺 Roadmap

- ✅ Authentication
- ✅ Home Feed
- ✅ Stories
- ✅ Posts
- ✅ Search
- ✅ Notifications
- ✅ Wallet
- 🚧 Realtime Chat Improvements
- 🚧 Edit Profile
- 🚧 Push Notifications
- 🚧 Video Calling
- 🚧 Performance Optimization

---

# 💡 Highlights

- Modern Material Design UI
- Firebase Backend
- Realtime Firestore
- AI Caption Generator
- MVVM Clean Architecture
- Dependency Injection with Hilt
- Local Caching using Room
- Compose Navigation
- Firebase Storage Uploads
- Scalable Project Structure

---

# 👨‍💻 Developer

## Aayush Kumar Mishra

Android Developer

### Skills

- Kotlin
- Jetpack Compose
- Firebase
- MVVM
- Room Database
- Hilt
- Coroutines
- REST APIs
- Android Studio

GitHub

https://github.com/ayushshiva

---

# 🤝 Contributing

Contributions, suggestions, and improvements are welcome.

1. Fork the repository
2. Create a new branch
3. Commit your changes
4. Push the branch
5. Open a Pull Request

---

# ⭐ Support

If you like this project, please consider giving it a ⭐ on GitHub.

It helps support future development and motivates continued improvements.

---

# 📄 License

This project is licensed under the MIT License.

---

<div align="center">

### Made with ❤️ by Aayush Kumar Mishra

⭐ Thanks for visiting the repository ⭐

</div>
