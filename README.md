# TimeLimit - Digital Wellbeing & Focus

TimeLimit is a modern Android application designed to help users take control of their digital wellbeing by monitoring app usage, setting strict schedules, and providing motivational content to stay focused.

Built with **Kotlin**, **Jetpack Compose**, and **Clean Architecture** principles, it serves as a robust example of a production-ready Android app.

## 🚀 Key Features

-   **App Monitoring**: A persistent foreground service tracks real-time app usage with high accuracy.
-   **Smart Blocking**: Automatically block distracting apps during scheduled "Focus Sessions."
-   **Focus Feed [NEW]**: A curated feed of motivational quotes fetched from a remote API to keep you inspired.
-   **Usage Analytics**: Detailed daily and weekly statistics with interactive charts.
-   **Flexible Schedules**: Custom blocking rules for every day of the week.
-   **Privacy-First**: All usage data is stored locally in a Room database. No tracking, no cloud uploads.

## 🛠 Tech Stack

-   **UI**: Jetpack Compose with Material 3 Design
-   **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
-   **Dependency Injection**: Hilt (Dagger)
-   **Local Database**: Room
-   **KeyValue Storage**: DataStore (Preferences)
-   **Networking**: Retrofit + OkHttp + GSON
-   **Concurrency**: Kotlin Coroutines & Flow
-   **Navigation**: Jetpack Navigation Compose (Type-safe)
-   **Charts**: Custom Canvas implementation + Vico Library

## 🏗 Architecture & Design Patterns

The project follows modern Android development best practices:

-   **Unidirectional Data Flow (UDF)**: ViewModels expose UI states via `StateFlow` to Compose screens.
-   **Clean Architecture**: Separation of concerns between Data, Presentation, and Service layers.
-   **Sealed UI States**: Robust handling of `Loading`, `Success`, and `Error` states for network and database operations.
-   **Scalable DI**: Modules are organized for easy testing and dependency replacement.

## 📡 Network Layer Implementation

To demonstrate proficiency in content aggregation:
-   **Rest API Integration**: Uses Retrofit to fetch motivational content from external sources.
-   **Repository Pattern**: Abstracted data source handling for both local (Room) and remote (Retrofit) data.
-   **Error Handling**: Centralized result-wrapper implementation for consistent error reporting in the UI.

## 📁 Project Structure

```text
app/src/main/java/com/example/timelimit/
├── data/
│   ├── database/    # Room entities & DAOs
│   ├── remote/      # Retrofit API definitions & Models [NEW]
│   └── repository/  # Single source of truth for UI data
├── di/              # Hilt Modules
├── service/         # TimeLimitService (Foreground Monitor)
└── ui/
    ├── focus/       # Focus Feed feature [NEW]
    ├── main/        # Dashboard & Navigation [Refactored]
    ├── permission/  # Onboarding & Permissions [Refactored]
    ├── settings/    # App blocking & Schedules
    └── theme/       # Material 3 Design System
```

## 🔨 How to Build

1.  Clone the repository:
    ```bash
    git clone https://github.com/melihgulbay/TimeLimit.git
    ```
2.  Open in **Android Studio Hedgehog** (or newer).
3.  Ensure you have **JDK 17** configured in Gradle settings.
4.  Build and run:
    ```bash
    ./gradlew installDebug
    ```

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
**Author**: Melih Gülbay
**LinkedIn**: [melih-gulbay](https://tr.linkedin.com/in/melih-gulbay)
**GitHub**: [melihgulbay](https://github.com/melihgulbay)
