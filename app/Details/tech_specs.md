# Technical Specifications

## Architecture: MVVM (Model-View-ViewModel)

The project adopts the MVVM architecture to promote a clean separation of concerns and enhance testability.

### Components

1.  **View (UI Layer)**:
    *   **Activities**: `LoginActivity`, `RegisterActivity`, `MainActivity`, `ChatActivity`, `SettingsActivity`, `EditProfileActivity`, `UserProfileActivity`.
    *   **Fragments**: `ChatListFragment`, `ProfileFragment`.
    *   **Responsibility**: Display data to the user and capture user interactions. They observe `LiveData` from ViewModels and update the UI accordingly.

2.  **ViewModel (Presentation Logic)**:
    *   **Classes**: `LoginViewModel`, `RegisterViewModel`, `ChatListViewModel`, `ChatViewModel`, `ProfileViewModel`.
    *   **Responsibility**:
        *   Hold and manage UI-related data in a lifecycle-conscious way.
        *   Expose data streams (`LiveData`) to the View.
        *   Handle UI logic and communicate with the Repository.
        *   Survive configuration changes (e.g., screen rotations).

3.  **Model (Data Layer)**:
    *   **Entities**: `User`, `ChatMessage`, `Group`, `ChatConversation`.
    *   **Repositories**: `AuthRepository`, `ChatRepository`, `UserRepository`.
    *   **Responsibility**:
        *   **Entities**: POJOs representing data structures.
        *   **Repositories**: Abstract the data sources. They handle data operations (fetching, saving) with Firebase Firestore and Authentication. They provide a clean API for the ViewModels.

## Data Flow

1.  **User Action**: User interacts with the View (e.g., clicks "Login").
2.  **ViewModel Call**: View calls a method in the ViewModel (e.g., `loginViewModel.login()`).
3.  **Repository Operation**: ViewModel delegates the operation to the Repository (e.g., `authRepository.login()`).
4.  **Data Source**: Repository interacts with Firebase (Auth/Firestore).
5.  **LiveData Update**: Repository returns a `LiveData` or callback. ViewModel updates its `LiveData`.
6.  **UI Update**: View observes the `LiveData` and updates the UI (e.g., navigate to Home, show error).

## Libraries & Tools

*   **Android Jetpack**:
    *   **ViewModel**: For managing UI-related data.
    *   **LiveData**: For observable data holders.
    *   **ViewBinding**: For safer interaction with views.
*   **Firebase**:
    *   **Authentication**: User identity management.
    *   **Firestore**: NoSQL cloud database for real-time data.
    *   **Cloud Messaging (FCM)**: Push notifications.
*   **Glide**: Efficient image loading and caching.
*   **Material Design**: Modern UI components.

## Directory Structure

```
app/src/main/java/I/imessenger/
├── activities/       # Activities (View)
├── adapters/         # RecyclerView Adapters
├── fragments/        # Fragments (View)
├── models/           # Data Models
├── repositories/     # Data Repositories
├── services/         # Background Services (FCM)
├── utils/            # Utility Classes
└── viewmodels/       # ViewModels
```
