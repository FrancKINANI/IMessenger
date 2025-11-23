# 🛠️ IMessenger - Technical Architecture & Configuration

This document details the file structure, Gradle dependencies, and Firebase data model for the development team.

---

## 1. Naming Conventions & Package

- **Package Name**: `I.imessenger`
- **Language**: Java
- **Min SDK**: API 24 (Android 7.0) - Covers ~95% of devices
- **Target SDK**: API 36 (Android 14)

---

## 2. Folder Structure

We use a **Layered Architecture** to simplify navigation.

```
app/
├── src/
│   ├── main/
│   │   ├── java/I/imessenger/
│   │   │   ├── activities/        # Main screens (Activity)
│   │   │   │   ├── LoginActivity.java
│   │   │   │   ├── RegisterActivity.java
│   │   │   │   ├── MainActivity.java      # Contains BottomNavBar
│   │   │   │   ├── ChatActivity.java      # 1-1 Chat Screen
│   │   │   │   └── ...
│   │   │   │
│   │   │   ├── adapters/          # List Managers (RecyclerView)
│   │   │   │   ├── UsersAdapter.java      # Student list
│   │   │   │   ├── MessageAdapter.java    # Chat bubbles
│   │   │   │   └── FeedPostAdapter.java   # News feed
│   │   │   │
│   │   │   ├── fragments/         # Navigation tabs
│   │   │   │   ├── ChatListFragment.java
│   │   │   │   ├── FeedFragment.java
│   │   │   │   ├── ToolsFragment.java
│   │   │   │   └── ProfileFragment.java
│   │   │   │
│   │   │   ├── models/            # Data Objects (POJO)
│   │   │   │   ├── User.java
│   │   │   │   ├── ChatMessage.java
│   │   │   │   └── Post.java
│   │   │   │
│   │   │   └── utils/             # Utility Functions
│   │   │       ├── FirebaseUtils.java     # Firestore/Auth shortcuts
│   │   │       ├── DateUtils.java         # Date formatting (e.g., "5min ago")
│   │   │       └── Constants.java         # Static keys
│   │   │
│   │   ├── res/
│   │   │   ├── layout/            # XML Files (Design)
│   │   │   │   ├── activity_login.xml
│   │   │   │   ├── item_message_sent.xml  # Sent message bubble
│   │   │   │   ├── item_message_received.xml
│   │   │   │   └── ...
│   │   │   ├── values/
│   │   │   │   ├── colors.xml     # Palette (ISMAGI Blue)
│   │   │   │   └── strings.xml    # App texts
│   │   │   └── drawable/          # Images and icons
│   │   └── AndroidManifest.xml
```

---

## 3. Gradle Configuration (Dependencies)

Add these essential libraries to your `build.gradle` (Module: app):

```gradle
dependencies {
    // 1. Firebase (Use BoM to manage versions)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")       // Authentication
    implementation("com.google.firebase:firebase-firestore")  // Database
    implementation("com.google.firebase:firebase-storage")    // File/Image Storage
    implementation("com.google.firebase:firebase-messaging")  // Notifications

    // 2. Image Management (To load profile photos from URL)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // 3. Circular Image (For profile avatars)
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // 4. Modern UI Components
    implementation("com.google.android.material:material:1.11.0") // BottomNav, Cards
    
    // 5. (Optional) To display PDFs directly in-app
    implementation("com.github.barteksc:android-pdf-viewer:2.8.2")
}
```

> **Important**: Don't forget to add the `google-services.json` file (downloaded from Firebase Console) into the `app/` folder of your project.

---

## 4. Database Modeling (Firestore)

Proposed NoSQL structure to manage requested features.

### Collection: `users`

Each document ID = User UID (provided by Auth).

```json
{
  "uid": "XyZ123...",
  "email": "student@ismagi.ma",
  "fullName": "Karim Bennani",
  "role": "student",  // or "admin", "teacher", "alumni"
  "level": "3rd Year",
  "profileImage": "https://firebasestorage...",
  "fcmToken": "token_for_notifications..."
}
```

### Collection: `chats` (Conversations)

Contains discussion metadata.

```json
{
  "chatId": "auto-generated-id",
  "participants": ["uid_user_1", "uid_user_2"],
  "lastMessage": "See you in class?",
  "lastMessageTime": 1709560000
}
```

### Sub-Collection: `chats/{chatId}/messages`

The actual messages inside a conversation.

```json
{
  "senderId": "uid_user_1",
  "message": "Hi, do you have the course PDF?",
  "type": "text",  // or "image", "pdf"
  "timestamp": 1709560000
}
```

### Collection: `posts` (News Feed)

```json
{
  "authorId": "uid_admin",
  "content": "Chess tournament this Saturday!",
  "imageUrl": "https://...",
  "likesCount": 42,
  "timestamp": 1709560000
}
```

---

## 5. Firebase Security Rules (Firestore Rules)

To be configured in the Firebase Console to secure the app.

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Everyone can read/write if logged in
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
    
    // (Later, we will refine: only profs/admins can post in "Announcements")
  }
}
```

---

## 6. Team Best Practices (Git)

Since there are 3 developers, respect this Git flow:

- **Main (Master)**: Clean code that always works. Do not touch directly.
- **Develop**: The common working branch.
- **Feature Branches**: Create a branch for each task.

**Examples:**
```bash
# Dev A
git checkout -b feature/login-screen

# Dev B
git checkout -b feature/chat-layout
```

Once the task is done → Merge to `Develop`.

---

## 7. Color & Theme Configuration

To respect the graphic charter, configure `res/values/colors.xml`:

```xml
<resources>
    <color name="ismagi_blue_primary">#003366</color> <!-- Institutional Dark Blue -->
    <color name="ismagi_blue_light">#0055AA</color>
    <color name="ismagi_accent">#FF9800</color> <!-- Orange for notifications/buttons -->
    <color name="white">#FFFFFF</color>
    <color name="background_gray">#F2F2F2</color>
    <color name="chat_bubble_me">#003366</color>
    <color name="chat_bubble_other">#E0E0E0</color>
</resources>
```

---

## 📝 Notes

- Always use `ConstraintLayout` for XML layouts
- Follow Material Design 3 guidelines
- Implement proper error handling for all Firebase operations
- Never block the main UI thread
