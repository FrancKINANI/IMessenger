# 📱 IMessenger - Project Charter & Roadmap

## Project Overview

- **Project**: Mobile application for communication and student life at ISMAGI
- **Team**: 3 Developers
- **Duration**: 3 Months
- **Tech Stack**: Android (Java), Firebase (Auth, Firestore, Storage, FCM)
- **Architecture**: MVVM (Model-View-ViewModel)

---

## 1. Project Vision

The goal is to centralize ISMAGI student life into a single application. More than just a chat, it is an ecosystem connecting administration, professors, current students, and alumni. The visual identity will rely on the institutional blue (based on your logo).

---

## 2. Technical Architecture (Firebase & MVVM)

To meet the 3-month deadline and ensure code maintainability, we use a **Serverless architecture** combined with **MVVM**:

-   **Architecture**: MVVM (Model-View-ViewModel) using Android Jetpack components (ViewModel, LiveData).
-   **Authentication**: Firebase Auth (Strict restriction to `@ismagi.ma` emails or authorized domains).
-   **Database**: Cloud Firestore (NoSQL). Flexible structure for chats and posts.
-   **File Storage**: Firebase Storage (Course PDFs, profile images, feed photos).
-   **Notifications**: Firebase Cloud Messaging (FCM).
-   **Backend Logic (Optional)**: Cloud Functions (if complex server logic is needed).

---

## 3. Role Distribution (Suggestion for 3 people)

### Dev A (Lead Backend & Chat)
- Firestore architecture & Repositories
- Authentication management
- Real-time chat logic (1-1 messaging, groups)
- [x] Security

### Dev B (Lead UI/UX & Social)
- Interface design (XML) & ViewModels
- News Feed
- User Profiles
- Media management (images/videos)

### Dev C (Lead Features & Tools)
- Calendar
- Document drive
- Polls, Manage calls for project

---

## 4. 3-Month Roadmap (Agile Method)

### 🗓️ MONTH 1: The Core (MVP - Minimum Viable Product)

**Objective**: By the end of the month, users must be able to log in and chat.

#### Week 1: Setup & Auth
- [x] Android Studio & Firebase configuration
- [x] Splash Screen (with animated logo)
- [x] Login/Register: ISMAGI email verification
- [x] Profile Creation (Student/Prof, Major, Year)
- [x] MVVM Architecture Setup (Repositories, ViewModels)

#### Week 2: User List & Structure
- [x] Display list of students/profs (RecyclerView)
- [x] Bottom Navigation Bar (Chat, Feed, Tools, Profile)
- [x] Dark Mode Implementation

#### Week 3: Instant Messaging (Base)
- [x] Real-time 1-to-1 Chat
- [x] Status (Online/Offline)
- [x] Message bubble design (similar to WhatsApp/Telegram)
- [x] Push Notifications (FCM)

#### Week 4: Basic Groups
- [x] Group creation (Admin managed)
- [x] Sending images in chat
- [x] Public & Event Channels

---

### 🗓️ MONTH 2: Student Life (Social & Academic)

**Objective**: Make the app useful for studies and social life.

#### Week 5: News Feed
- [x] Post news (Admin/Student Council)
- [x] Likes/Comments system
- [x] Event display

#### Week 6: Document Sharing (Drive)
- [x] Upload interface for PDF/PPT
- [x] Organization by folders (Year > Subject)
- [x] File downloading

#### Week 7: Calendar & Notifications
- [x] Calendar view for classes/exams
- [x] Push Notifications implementation (New message, New grade)

#### Week 8: Advanced Profiles & Skills
- [x] Edit Profile (Settings)
- [x] View other user profiles
- [x] Add "Mini-CV" to profile
- [x] "Clubs" section (Join a club)

---

### 🗓️ MONTH 3: Innovation & Polishing

**Objective**: Add "Wow" features and stabilize the app.

#### Week 9: Advanced Tools
- [x] Polls / Voting

#### Week 10: Alumni & Geolocation
- [x] "Alumni" space (specific filter)

#### Week 11: Admin & Moderation
- [x] Admin panel (in-app or web) to report/block
- [x] Analytics (view counters, message stats)

#### Week 12: Testing & Deployment
- [x] Bug fixing (Crashlytics)
- [x] Performance optimization
- [x] Final presentation

---

## 5. Key Feature Details

### A. The "Modern WhatsApp" (Chat)

**Design**: Don't copy WhatsApp exactly. Use Google's "Material Design 3" for a more modern touch.

**Technique**: Use Firestore `SnapshotListener` within `ChatRepository` to listen for messages in real-time without reloading the page. Expose data via `LiveData` in `ChatViewModel`.

### B. The "Student LinkedIn" (Profiles)

- Highlight badges (e.g., "IT Club President", "Class Rep")
- Allow search by skill (e.g., a student looking for someone who knows "Photoshop" for a project)

### C. The "Documents" Space

**Important**: Do not store heavy files directly in the database. Put them in Firebase Storage and only keep the link (URL) in Firestore.

---

## 6. Visual Identity & Logo

Your current icon (the blue bubble with the "I") is a very good base, simple and effective.

### UI Improvement Suggestions

#### Color Palette
- **Primary**: ISMAGI Blue (Dark Royal Blue)
- **Secondary**: An orange or yellow for notifications/important actions (contrast)
- **Background**: White or very light gray (`#F5F5F5`) for cleanliness
- **Dark Mode**: Slate/Dark Blue background with light text.

#### Logo
Keep the "I" but try slightly rounding the edges of the square to make it more "organic" (iOS/OneUI style).

---

## 7. Risks & Advice

### ⚠️ Critical Warnings

1. **Don't do everything at once**
   - If the Chat doesn't work, the rest is useless
   - **Prioritize the Chat**

2. **Video is complex**
   - Don't try to code a video streaming engine (WebRTC) from scratch in 3 months
   - Use "Intents" to open Zoom/Teams/Meet from your app
   - It's more reliable

3. **Security**
   - Pay attention to "Firestore Security Rules"
   - A student should not be able to delete a professor's course

### 💡 Best Practices

- Test early and often
- Use version control (Git) properly
- Document your code
- Keep UI/UX consistent across all screens
- Optimize for performance from the start
- **Stick to MVVM**: Keep logic out of Activities/Fragments.

---

## 8. Success Metrics

By the end of Month 3, the app should:
- ✅ Allow secure login with ISMAGI email
- ✅ Enable real-time 1-to-1 and group messaging
- ✅ Display news feed with posts and events
- ✅ Share and organize course documents
- ✅ Show user profiles with skills
- ✅ Provide calendar and event management
- ✅ Support push notifications
- ✅ Include campus geolocation features

---

## 📝 Notes

- This roadmap is flexible and can be adjusted based on team progress
- Regular team meetings are recommended to track progress
- Use Agile methodology with weekly sprints
- Document all major decisions and changes
