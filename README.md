# MAD25_P03_Team03
##Disclaimer
This is a student assignment project for the Kotlin App Development module at Ngee Ann Polytechnic. Developed for educational purposes.

## 🎶 Song Guessing Game Wireframes (part 1 - version)

### Overview

These wireframes detail the core mobile user experience (UX) and interface (UI) for the "Song Guessing Game," covering key flows such as user authentication, profile management, game, score tracking, and application settings.

### Design Preview

<img width="1241" height="926" alt="Screenshot 2025-12-02 164434" src="https://github.com/user-attachments/assets/1ba4359c-d071-41c8-8d06-06108691b4e0" />

To view the live, interactive wireframes and design annotations, use the link below:

View Full Wireframes on Figma: 
https://www.figma.com/design/Fbf0YaYMkNE8I4efAo8EwP/song-guessing-game-wireframe-version-1?t=VjOIe16cEkqFFL8z-0

---

## 🎵 Song Guessing Game – Detailed User Flows

This document outlines the end-to-end user paths within the application, focusing on steps, decision points, and potential scenarios for key features.


### **Flow 1: User Onboarding and Authentication**

#### **1.1 Sign-Up (Happy Path)**

| Step | Action | Expected Outcome | Screen/State |
|------|--------|------------------|--------------|
| 1 | User taps **“Sign Up”** | Display registration form | Sign-Up Screen |
| 2 | User enters valid Email, Username, and matching Passwords | **Sign Up** button becomes active | Sign-Up Screen |
| 3 | User taps **“Sign Up”** | Account created successfully | Profile Screen |

---

#### **Edge Cases and Alternatives**

| Scenario | Trigger | Resolution / Alternative Path |
|----------|---------|-------------------------------|
| Login Failure | Incorrect email or password | Show error **“Incorrect email or password.”** |
| Registration Exists | Email/username already used | Show **“This email or username is already taken.”** |
| Invalid Input | Invalid email/password format | Inline validation; sign-up disabled |
| Forgot Password | User taps link | Trigger email recovery |

---

### **Flow 2: Single-Player Gameplay**

#### **2.1 Game Completion (Happy Path)**

| Step | Action | Expected Outcome | Screen/State |
|------|--------|------------------|--------------|
| 1 | User selects a mode | Difficulty/Genre modal appears | Game Selection |
| 2 | User confirms Genre + Difficulty | Audio + timer start | Gameplay |
| 3 | User guesses correctly | Success screen; next song | Gameplay (Success) |
| 4 | Repeats until N songs | Show score summary | Game Report |

---

#### **Edge Cases**

| Scenario | Trigger | Resolution |
|----------|---------|------------|
| Incorrect Guess | Wrong title chosen | Deduct points, show correct answer |
| Time Out | Timer reaches zero | Miss; auto-continue |
| Skip Song | User taps Skip | Miss; next song |
| Game Interruption | App minimized / lost network | Save progress; show Resume option |

---

### **Flow 3: Profile & Settings**

#### **3.1 Updating Profile (Happy Path)**

| Step | Action | Expected Outcome | Screen |
|------|--------|------------------|--------|
| 1 | Open Profile/Settings | Show stats + settings | Profile/Settings |
| 2 | Tap editable field | Show input modal | Settings |
| 3 | Save new data | Success notification | Settings |

---

#### **Edge Cases**

| Scenario | Trigger | Resolution |
|----------|---------|------------|
| Invalid Settings Data | Malformed input | Inline error |
| Logout | User taps Logout | Confirm → Back to Login |
| Delete Account | User taps Delete | Severe warning + password confirmation |






