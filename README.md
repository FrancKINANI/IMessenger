# 📱 IMessenger

> The centralized digital ecosystem for students, professors, and alumni of ISMAGI.

---

## 📋 About the Project

IMessenger is a native Android mobile application developed as part of a 3-month academic project. It aims to modernize and centralize communication within the institute(ISMAGI).

The application replaces dispersed informal groups (WhatsApp, Facebook) with an official, secure platform dedicated to student and academic life.

---

## 🌟 Key Features

### 🔐 Unified Authentication
- Secure login via `@ismagi.ma` email
- Student/professor status verification
- Role-based access control

### 💬 Instant Messaging (Real-Time)
- **Private 1-to-1 Chat**: Direct messaging between users
- **Group Discussions**: Organized by Class, Clubs, and Projects
- **Media Sharing**: Images and files support

### 📰 News Feed
- Administration announcements
- Student Council events
- Club news and updates

### 📂 Document Space
- Sharing and storage of course materials (PDF, PPT)
- Organized by subject and year
- Easy search and download

### 🎓 Profiles & Networking
- Highlighting skills (Mini-CV)
- Alumni directory
- Professional networking features

### 📅 Student Life Tools
- Event Calendar
- Polls and Voting
- Campus Geolocation

---

## 🛠️ Tech Stack

### Core Technologies
- **Language**: Java
- **IDE**: Android Studio Iguana (or higher)
- **Architecture**: MVVM (recommended) / Clean Layered Architecture
- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)

### Main Libraries
- **Firebase BOM**: Auth, Firestore, Storage, Cloud Messaging
- **Glide**: Image loading and caching
- **CircleImageView**: User avatars
- **Material Design 3**: Modern UI components

---

## 🚀 Installation and Configuration

### Prerequisites
- Android Studio (latest stable version)
- JDK 8 or higher
- Firebase account

### Setup Steps

#### 1. Clone the Repository
```bash
git clone https://github.com/FrancKINANI/IMessenger.git
cd IMessenger
```

#### 2. Open in Android Studio
1. Open Android Studio
2. Select **File > Open...**
3. Choose the cloned project folder
4. Wait for Gradle to synchronize dependencies

#### 3. ⚠️ Firebase Configuration (CRUCIAL)

This project requires a Firebase connection to function. The configuration file is not included in the repository for security reasons.

**Steps:**
1. Create a project on the [Firebase Console](https://console.firebase.google.com/)
2. Enable the following services:
   - Authentication (Email/Password)
   - Firestore Database
   - Storage
3. Add an Android app with the package name: `I.iMessenger`
4. Download the `google-services.json` file
5. Place this file in the `app/` folder:
   ```
   IMessenger/app/google-services.json
   ```
6. Rebuild the project: **Build > Rebuild Project**

---

## 📂 Code Structure

Source code is organized by technical features:

```
ma.ismagi.connect/
├── activities/    # Main screens (Login, Main, Chat...)
├── adapters/      # Adapters for RecyclerViews (Lists)
├── fragments/     # Secondary screens and tabs (Profile, Feed...)
├── models/        # Data classes (User, Message, Post)
└── utils/         # Helpers (Date, Firebase, Permissions)
```

---

## 📅 Roadmap (3 Months)

- [ ] **Month 1**: Setup, Authentication, Basic Chat (MVP)
- [ ] **Month 2**: News Feed, Document Sharing, Profiles
- [ ] **Month 3**: Notifications, Geolocation, Admin Panel, Polishing

For detailed weekly breakdown, see [roadmap.md](./roadmap.md)

---

## 👥 The Team

Project created by 3 ISMAGI students:

- **[Dev 1 Name]** - Lead Backend & Architecture
- **[Dev 2 Name]** - Lead UI/UX & Social Features
- **[Dev 3 Name]** - Lead Features & Tools

---

## 📚 Documentation

- [Technical Specifications](./app/Details/tech_specs.md) - Detailed technical architecture
- [Project Roadmap](./app/Details/roadmap.md) - Complete development timeline

---

## 📄 License

This project is for academic purposes.

---

## 🤝 Contributing

This is an academic project. For questions or suggestions, please contact the development team.
