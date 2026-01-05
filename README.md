# 🌱 EquadX – Smart QR-Based Waste Management System

For Testing Application :
Student Email id : test@gmail.com
Student Password : 123456

Admin Email id : admin@gmail.com
Admin Password : 123456

EquadX is an Android application designed to promote responsible waste disposal using **QR code scanning, rewards, and real-time tracking**.  
Users earn points by scanning QR codes placed on smart waste bins, while admins manage bins, QR codes, and monitor activity.

---

## 🚀 Features

### 👤 Student Module
- 🔐 Secure login & signup (Firebase Authentication)
- 📷 Scan QR codes on waste bins
- 🎁 Earn reward points for valid scans
- ⏱ Prevents multiple scans on the same bin per day
- 📊 Real-time wallet (points update instantly)
- 🏆 Leaderboard to compare points
- 🗺 View nearby bins on Google Maps
- 👤 Profile management & scan history

### 🛠 Admin Module
- 🔐 Admin-only login
- 📦 Generate and manage QR codes for bins
- 📍 Store bin locations (latitude & longitude)
- 📊 View scan history of all users
- 🚫 Prevents student login via admin accounts

---

## 🧠 Smart Validations Implemented

- ✔ Role-based authentication (Admin / Student)
- ✔ Auto-login with correct dashboard redirection
- ✔ Camera permission handling (runtime permission request)
- ✔ QR format validation (`EQUADX_BIN:BIN_ID`)
- ✔ Location-based scan validation (user must be near bin)
- ✔ Duplicate scan prevention (once per day per bin)
- ✔ Firestore transaction-based point updates

---

## 🧩 Tech Stack

### Android
- Kotlin
- XML (Material UI)
- CameraX
- RecyclerView
- Google Maps SDK

### Google / Firebase Technologies
- Firebase Authentication
- Firebase Firestore (NoSQL Database)
- Firebase Analytics
- Firebase Cloud Messaging (optional, client-side)
- Google ML Kit (QR/Barcode scanning)
- Google Maps API

---

## 🗺 Firestore Database Structure

users/
└─ userId/
├─ fullName
├─ email
├─ role (student/admin)
├─ points
└─ scans/
└─ binId/
├─ lastScanDate
├─ points
└─ timestamp

bins/
└─ binId/
├─ active
├─ points
├─ lat
└─ lng

yaml
Copy code

---

## 📱 App Flow

1. Splash Screen checks authentication
2. Role-based auto redirection:
   - Admin → Admin Dashboard
   - Student → Student Dashboard
3. Student scans QR → location + validity checked
4. Points rewarded instantly
5. Data synced in real time

---

## 🔐 Permissions Used

- Camera (QR scanning)
- Internet (Firebase & Maps)
- Location (distance-based validation)

---

## 🧪 How to Run the Project

1. Clone the repository  
2. Open in **Android Studio**
3. Add your `google_maps_api_key` in `AndroidManifest.xml`
4. Connect Firebase project (Authentication + Firestore)
5. Run on physical device (recommended for camera & maps)

---

## ⚠ Notes

- App is forced to **Light Mode** for consistent UI
- Firebase Functions are **not required**
- Firestore rules must allow authenticated access

---

## 📸 Screenshots

> Login • Signup • Student Dashboard • Admin Dashboard • QR Scan • Maps

---

## 👨‍💻 Developed By

**EquadX Team**  
Smart Sustainable Tech Initiative 🌍

---

## 📜 License

This project is for **educational & demonstration purposes**.
