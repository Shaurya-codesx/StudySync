<div align="center">
  <h1>📚 StudySync</h1>
  <p><strong>A modern, full-stack collaborative flashcard application powered by AI and WebSockets.</strong></p>

  <!-- Badges -->
  <img src="https://img.shields.io/badge/Kotlin-1.9.0-7F52FF?logo=kotlin" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?logo=android" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Ktor-Backend-000000?logo=ktor" alt="Ktor" />
  <img src="https://img.shields.io/badge/PostgreSQL-Database-336791?logo=postgresql" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?logo=github-actions" alt="GitHub Actions" />
</div>

<br/>

## 🎬 Demo & Screenshots

*(Insert Demo Video or GIF here)*

### 📚 Organization & Profile
| Login Screen | Home List | Folder View | User Profile |
| :---: | :---: | :---: | :---: |
| <img width="1280" height="2856" alt="Login Screen" src="https://github.com/user-attachments/assets/d2b4dbed-17ae-4eda-ad9e-5c222f4e1405" />
 | <img width="1280" height="2856" alt="Home" src="https://github.com/user-attachments/assets/49e329f7-4394-4d9c-88d8-0f38be22f622" />
 | <img width="1280" height="2856" alt="Folder Deck Screen" src="https://github.com/user-attachments/assets/7bb6b299-03e7-4858-b0a5-b262eccebc20" />
 | <img width="1280" height="2856" alt="User Profile" src="https://github.com/user-attachments/assets/f71bfa83-7aa9-4d18-a273-fe3aaccb8b10" />
 |

### 🧠 Study, AI & Marketplace
| Deck Review | AI Generation | Marketplace |
| :---: | :---: | :---: |
| <img width="1280" height="2856" alt="Deck Review" src="https://github.com/user-attachments/assets/2a6d8c7f-92f6-4869-b7b1-ddb5d8a22335" />
 | <img width="1280" height="2856" alt="Deck Generation" src="https://github.com/user-attachments/assets/182cbed0-ac38-4552-919f-97198fec35c6" />
 | <img width="1280" height="2856" alt="MarketPlace" src="https://github.com/user-attachments/assets/acb743a9-c0e0-4753-8b19-ae3368f4dbba" />
 |

### 📊 Analytics
| Library Status | Retention Curve | Upcoming Reviews |
| :---: | :---: | :---: |
| <img width="1280" height="2856" alt="Library Status" src="https://github.com/user-attachments/assets/9ef2de51-107d-4a69-8749-4e8ddc89bc9b" />
 | <img width="1280" height="2856" alt="Retention Curve" src="https://github.com/user-attachments/assets/3f66c9cd-8a7a-4b93-a5e8-f6e22d83472d" />
 | <img width="1280" height="2856" alt="upcoming reviews" src="https://github.com/user-attachments/assets/79824d49-1aa6-488f-85d6-b43cd828a465" />
 |

---

## 📖 Table of Contents
- [About the Project](#-about-the-project)
- [Key Features](#-key-features)
- [Tech Stack & Infrastructure](#️-tech-stack--infrastructure)
- [Architecture & Folder Structure](#️-architecture--folder-structure)
- [Testing Strategy](#-testing-strategy)
- [Getting Started (Local Development)](#-getting-started-local-development)
- [Future Roadmap](#-future-roadmap)
- [Contact](#-contact)

---

## 💡 About the Project
**StudySync** is a comprehensive, full-stack learning platform designed to fundamentally redefine how students interact with educational material. Recognizing that traditional memorization tools are often isolating and tedious, StudySync was engineered to bridge the gap between solitary study and community-driven learning. 

By combining the cognitive science of the **SM-2 spaced repetition algorithm** with the cutting-edge capabilities of **Google's Gemini AI**, the app dynamically adapts to individual learning curves while dramatically reducing the friction of content creation. Under the hood, a robust offline-first architecture ensures flawless performance, while real-time **WebSockets** enable users to study collaboratively with peers anywhere in the world. StudySync isn't just a flashcard app—it's a synchronized, scalable, and intelligent study ecosystem.

---

## ✨ Key Features

- 🤖 **AI-Powered Deck Generation:** Instantly generate detailed, structured flashcards from raw notes using Gemini AI. Built with strict server-side **rate limiting** to prevent API abuse and robust Ktor error handling for malformed inputs.
- 🤝 **Real-Time Study Rooms:** Study together with peers using Ktor WebSockets. Room timers, live member counts, and card flips sync instantaneously across all connected clients.
- 🧠 **Spaced Repetition (SM-2):** Optimized learning retention built on the proven SuperMemo-2 algorithm to schedule flashcards based on individual recall performance.
- 🌍 **Public Marketplace:** Easily publish your curated decks and discover, download, and study flashcard collections created by the community globally. Features smooth **pagination** to efficiently load large libraries of community decks without performance degradation.
- 📊 **Analytics Dashboard:** Gamified learning! Natively tracks study streaks, daily progress, and retention metrics.
- 📴 **Offline-First Architecture:** Powered by Room Database and Kotlin Flows. The UI updates instantly via local cache, silently syncing with the Ktor REST API in the background.
- 🎨 **Modern UI/UX:** Built entirely with Jetpack Compose. Features 3D card-flip physics, smooth Material Shared-Axis navigation transitions, and custom pulsing skeleton loaders.

---

## 🛠️ Tech Stack & Infrastructure

### 📱 Android Client (Frontend)
- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel) with Clean Architecture principles
- **Asynchrony:** Coroutines & StateFlow
- **Dependency Injection:** Dagger Hilt
- **Local Database:** Room (with `@Upsert` cascade protection)
- **Networking:** Ktor Client & WebSockets
- **Pagination:** Jetpack Paging 3
- **Testing:** JUnit4, Mockito, Coroutines Test, Compose UI Test Manifest

### ⚙️ Backend (Server)
- **Framework:** Ktor Server (Netty)
- **Language:** Kotlin
- **Database:** PostgreSQL (with JetBrains Exposed ORM)
- **Serialization:** `kotlinx.serialization`
- **Authentication:** JWT (JSON Web Tokens)
- **Testing:** Ktor Server TestHost, Kotlin Test, MockEngine

### 🌍 Infrastructure & CI/CD
- **Local Environment:** Docker & Docker Compose
- **Production Deployment:** Railway App Hosting
- **CI/CD:** Automated testing and release compilation pipelines.

---

## 🏗️ Architecture & Folder Structure

StudySync strictly separates concerns, relying on the **Repository Pattern** and **Unidirectional Data Flow (UDF)**. 

### Android Client Structure
```text
android/app/src/main/java/com/example/studysyncandroid
├── data/
│   ├── local/      # Room DAOs, Entities, and local Database config
│   ├── remote/     # Ktor Client API definitions & WebSocket managers
│   └── repository/ # Single Source of Truth mediators (Offline-first logic)
├── di/             # Hilt Dependency Injection Modules
├── ui/             # Jetpack Compose UI Layer
│   ├── analytics/  
│   ├── auth/       
│   ├── components/ # Reusable UI blocks (Dialogs, Buttons)
│   ├── decks/      
│   ├── marketplace/
│   ├── navigation/ # NavGraphs & Route definitions
│   ├── onboarding/ 
│   ├── profile/    
│   ├── review/     # Flashcard 3D flip physics & session logic
│   ├── rooms/      # WebSocket UI & Timer synchronization
│   ├── session/    
│   └── theme/      # Colors, Typography, Shapes
└── util/           # Extension functions and constants
```

### Ktor Backend Structure
```text
server/server/src/main/kotlin
├── dto/            # Data Transfer Objects shared with the Android client
├── models/         # Database Schemas and Exposed tables
├── plugins/        # Ktor Configurations: Routing, WebSockets, JWT Auth, CORS
├── repositories/   # Database Abstractions and SQL transactions
├── routes/         # REST API Endpoints & WebSocket connection handlers
├── services/       # Core Business Logic (e.g., AiService for Gemini)
└── utils/          # Hashing, Token Generation, Response mapping
```

---

## 🧪 Testing Strategy
- **Unit Testing:** Ensuring the integrity of ViewModel StateFlow emissions and Ktor backend business logic (e.g., proper JWT generation and AI prompt parsing).
- **Network Mocking:** Utilizing Ktor's `MockEngine` to simulate HTTP API responses (like 429 Too Many Requests) and WebSocket frames for robust, isolated client tests.
- **UI Testing:** Jetpack Compose UI test artifacts used to verify the correct rendering of complex layouts, dialog states, and skeleton loading indicators.

---

## 🚀 Getting Started (Local Development)

Follow these steps to set up StudySync on your local machine for development and testing.

### 1. Prerequisites
Ensure you have the following installed before starting:
- **Docker Desktop** (for running the database and backend)
- **Android Studio** (Koala or newer recommended)
- **Gemini API Key** (Get one from [Google AI Studio](https://aistudio.google.com/))

### 2. Set Up the Backend
1. Open a terminal and navigate to the `server/` directory:
   ```bash
   cd server
   ```
2. Create your environment configuration file by copying the example:
   ```bash
   cp .env.example .env
   ```
3. Open the newly created `.env` file in your text editor and paste your **Gemini API key** into the appropriate field.
4. Build and start the backend containers (Ktor Server and PostgreSQL) in detached mode:
   ```bash
   docker-compose up -d
   ```
   *The server is now running locally on port 8080.*

### 3. Build and Run the Android App
1. Launch **Android Studio**.
2. Click **Open** and select the `android/` directory from the cloned StudySync repository.
3. Wait for Android Studio to index the project and sync all Gradle dependencies automatically.
4. Create or start an **Android Virtual Device (Emulator)**.
5. Click the **Run** button (▶) in the top toolbar to build and install the app.
*(Note: The Android app is pre-configured to connect to `http://10.0.2.2:8080/`, which is how the Android emulator accesses your machine's localhost where Docker is running.)*

---

## 🔮 Future Roadmap
- [ ] Deep Linking for directly sharing Decks and Rooms via URLs.
- [ ] Expanded AI capabilities (e.g., generating quizzes from flashcards).
- [ ] iOS cross-platform migration utilizing Kotlin Multiplatform (KMP).

---

## 📫 Contact

Feel free to reach out if you have any questions or want to collaborate!

- **Name:** Shaurya Sharma
- **LinkedIn:** [https://www.linkedin.com/in/shaurya-sharma-828741325/](https://www.linkedin.com/in/shaurya-sharma-828741325/)
- **Email:** shaurya.shrma7@gmail.com

---
<div align="center">
  <sub>Built with ❤️ by Shaurya.</sub>
</div>
