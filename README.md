# IMessenger

IMessenger is a modern Android messaging application designed for school environments, facilitating communication between students, alumni, and administration.

## Features

-   **User Authentication**: Secure login and registration using Firebase Authentication (Email/Password & Google Sign-In).
-   **Real-time Messaging**: Instant messaging with individuals and groups using Firebase Firestore.
-   **Group Chats**:
    -   **Class Groups**: Automatically added based on student level.
    -   **Club Groups**: Join clubs and societies.
    -   **Alumni Network**: Dedicated channel for alumni.
    -   **Public Channels**: General school announcements.
    -   **Event Channels**: Discussions for upcoming events.
-   **Profile Management**: Edit profile details, view other users' profiles.
-   **Dark Mode**: Fully supported dark theme for comfortable viewing at night.
-   **Push Notifications**: Receive alerts for new messages (FCM).
-   **Modern UI**: Clean, responsive interface built with Material Design components.

## Architecture

The application follows the **MVVM (Model-View-ViewModel)** architectural pattern combined with the **Single Activity Architecture** recommended by Google. This ensures a robust, testable, and maintainable codebase.

-   **Single Activity**: `MainActivity` serves as the entry point and container for the entire application, hosting various Fragments via the Navigation Component.
-   **Navigation Component**: Manages app navigation, including the back stack, deep linking, and fragment transitions with animations.
-   **Model**: Represents the data and business logic (e.g., `User`, `ChatMessage`, `Group`).
-   **View**: UI components (Fragments) that observe the ViewModel and update the UI.
-   **ViewModel**: Acts as a bridge between the View and the Repository, holding UI-related data and handling logic.
-   **Repository**: Manages data operations, abstracting the data sources (Firebase Firestore, Auth).

### Key Components

-   **Repositories**: `AuthRepository`, `ChatRepository`, `UserRepository`.
-   **ViewModels**: `LoginViewModel`, `RegisterViewModel`, `ChatListViewModel`, `ChatViewModel`, `ProfileViewModel`.
-   **Fragments**: `LoginFragment`, `ChatListFragment`, `ChatFragment`, `ProfileFragment`, `SettingsFragment`, etc.
-   **Navigation**: `nav_graph.xml` defines the application flow and transitions.

## Tech Stack

-   **Language**: Java
-   **Backend**: Firebase (Authentication, Firestore, Storage, Cloud Messaging)
-   **UI**: XML Layouts, Material Design Components (Material 3 recommended), AndroidX
-   **Navigation**: Android Jetpack Navigation Component
-   **Image Loading**: Glide
-   **Architecture**: MVVM, Single Activity, Android Jetpack (ViewModel, LiveData, ViewBinding)

## Setup

1.  Clone the repository.
2.  Open the project in Android Studio.
3.  Connect the project to your Firebase Console project.
    -   Add `google-services.json` to the `app/` directory.
    -   Enable Authentication (Email/Password, Google).
    -   Enable Firestore Database.
    -   Enable Cloud Messaging.
4.  Build and run the application.

## Roadmap

See [roadmap.md](app/Details/roadmap.md) for future plans.
