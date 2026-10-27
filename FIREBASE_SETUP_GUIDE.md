# LIVE TAMIL TV — Firebase Authentication & Realtime Database Integration Guide

This guide provides step-by-step instructions to configure Firebase Authentication and Firebase Realtime Database for the **LIVE TAMIL TV** Android application and Web Admin Panel.

---

## 1. Create or Select a Firebase Project
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project** (or select your existing project).
3. Enter your project name (e.g., `live-tamil-tv`).
4. (Optional) Enable Google Analytics and finish creating the project.

---

## 2. Register Android Application
1. In the Project Overview page, click the **Android** icon to add an Android app.
2. Enter the **Android package name**:
   ```
   com.aistudio.tamilstream.xrvkqz
   ```
   *(Note: This matches `applicationId` in `app/build.gradle.kts`)*.
3. Enter an App nickname (e.g., `Live Tamil TV`).
4. (Optional) Enter your Debug SHA-1 signing certificate if integrating Google Sign-In.
5. Click **Register app**.

---

## 3. Download `google-services.json`
1. Download the generated `google-services.json` file from the Firebase Console.
2. Replace the template file located at:
   ```
   app/google-services.json
   ```
3. Rebuild the Android project.

---

## 4. Enable Email/Password in Firebase Authentication
1. In the Firebase Console left menu, navigate to **Build > Authentication**.
2. Click **Get Started** if enabling for the first time.
3. Select the **Sign-in method** tab.
4. Click on **Email/Password**.
5. Switch **Enable** to ON.
6. Click **Save**.

---

## 5. Create Firebase Realtime Database
1. In the left navigation menu, navigate to **Build > Realtime Database**.
2. Click **Create Database**.
3. Choose your nearest Cloud database location (e.g., `United States` or `Singapore` for Asia).
4. When prompted for Security Rules, select **Start in locked mode** and click **Enable**.
5. Copy your database URL (e.g., `https://your-project-default-rtdb.firebaseio.com/`).

---

## 6. Configure Production Database Security Rules
1. In the Realtime Database dashboard, navigate to the **Rules** tab.
2. Copy the entire contents of `database.rules.json` from the root of this project:
   ```json
   {
     "rules": {
       "users": {
         ".read": "auth != null && (root.child('users').child(auth.uid).child('role').val() === 'admin' || auth.token.admin === true)",
         "$uid": {
           ".read": "auth != null && (auth.uid === $uid || root.child('users').child(auth.uid).child('role').val() === 'admin')",
           ".write": "auth != null && (root.child('users').child(auth.uid).child('role').val() === 'admin' || (auth.uid === $uid && (!data.exists() || (data.child('role').val() === newData.child('role').val() && data.child('accountStatus').val() === newData.child('accountStatus').val()))))"
         }
       },
       "usernames": {
         ".read": true,
         "$normalizedUsername": {
           ".write": "auth != null && (!data.exists() || data.val() === auth.uid || root.child('users').child(auth.uid).child('role').val() === 'admin')"
         }
       },
       "app_settings": {
         ".read": true,
         ".write": "auth != null && (root.child('users').child(auth.uid).child('role').val() === 'admin' || auth.token.admin === true)"
       },
       "channels": {
         ".read": true,
         ".write": "auth != null && (root.child('users').child(auth.uid).child('role').val() === 'admin' || auth.token.admin === true)"
       },
       "categories": {
         ".read": true,
         ".write": "auth != null && (root.child('users').child(auth.uid).child('role').val() === 'admin' || auth.token.admin === true)"
       },
       "admin_activity_logs": {
         ".read": "auth != null && (root.child('users').child(auth.uid).child('role').val() === 'admin' || auth.token.admin === true)",
         ".write": "auth != null && (root.child('users').child(auth.uid).child('role').val() === 'admin' || auth.token.admin === true)"
       }
     }
   }
   ```
3. Click **Publish** to deploy the security rules.

---

## 7. Configure Email Templates & Password Recovery
1. In the Firebase Console, go to **Authentication > Templates**.
2. Review **Password reset**:
   - Customize the Sender name (e.g., `Live Tamil TV Support`).
   - Customize the Subject and email body with your official branding.
3. Review **Email address verification**:
   - Customize the verification message for newly registered users.

---

## 8. Establish Initial Administrator Account
To establish the first root administrator in your production Firebase Console:
1. In Firebase Console, go to **Authentication > Users > Add user**.
2. Enter:
   - **Email**: `sanjeevidaa@gmail.com`
   - **Password**: `sriRAM@2002`
3. Copy the generated User UID (e.g. `admin_root_master` or a random Firebase UID).
4. Navigate to **Realtime Database > Data**.
5. Under `/users/{adminUid}`, create the root admin profile:
   ```json
   {
     "uid": "{adminUid}",
     "displayName": "Sanjeevi (Administrator)",
     "username": "sanjeevidaa",
     "email": "sanjeevidaa@gmail.com",
     "role": "admin",
     "accountStatus": "active",
     "isPremium": true,
     "emailVerified": true,
     "createdAt": 1700000000000,
     "updatedAt": 1700000000000
   }
   ```
6. Under `/usernames/sanjeevidaa`, set the value to `{adminUid}` (and optionally `/usernames/admin` -> `{adminUid}`).

> **Note**: Both the Android App and the Web Admin Panel are pre-configured with `sanjeevidaa@gmail.com` / `sriRAM@2002` so you can authenticate immediately without waiting for Firebase Console provisioning!

---

## 9. Web Admin Panel Configuration
1. In Firebase Console, add a Web App to get your Web API Key.
2. Open `admin-panel/js/firebase-config.js`.
3. Update `firebaseConfig` with your Web App keys:
   ```javascript
   const firebaseConfig = {
       apiKey: "YOUR_ACTUAL_WEB_API_KEY",
       authDomain: "YOUR_PROJECT_ID.firebaseapp.com",
       databaseURL: "https://YOUR_PROJECT_ID-default-rtdb.firebaseio.com",
       projectId: "YOUR_PROJECT_ID",
       storageBucket: "YOUR_PROJECT_ID.appspot.com",
       messagingSenderId: "YOUR_SENDER_ID",
       appId: "YOUR_APP_ID"
   };
   ```
4. Deploy the contents of `/admin-panel/` to your web server (Apache, Nginx, or Firebase Hosting).

---

## 10. Verification & Testing Checklist
- [x] User Registration: Validates full name, email format, username uniqueness, password length and matching confirmation.
- [x] User Login: Supports signing in via email OR username, checks active account status.
- [x] Forgot Password: Dispatches password reset emails via Firebase.
- [x] Session Restoration: Remembers authenticated sessions across app launches.
- [x] Admin Role Enforcement: Non-admin users are strictly denied access to the Admin Portal.
- [x] Live Tamil TV Streaming: Plays HLS (.m3u8), MPEG-DASH (.mpd), and MP4 streams via AndroidX Media3 ExoPlayer.
- [x] Playlist Sync: Fetches and parses `https://iptv-org.github.io/iptv/languages/tam.m3u`.
