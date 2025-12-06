# MAD25_P03_Team03
##Disclaimer
This is a student assignment project for the Kotlin App Development module at Ngee Ann Polytechnic. Developed for educational purposes.

## Introduction

<img width="215" height="292" alt="image" src="https://github.com/user-attachments/assets/0ad60587-d0ef-45cd-aef3-6b97e7206178" />

This project is a Kotlin-based mobile application designed to provide an interactive and engaging music experience. The app combines elements of a quiz game, song discovery, and user account management to create a complete and user-friendly platform.

At its core, the Music Quiz App challenges users to identify songs through a multiple-choice guessing game, complete with a timer and a heart/life system to increase difficulty. Beyond the game, users can browse a structured song library, search for tracks, and even identify songs through an external API.

The application also includes a full login and signup system, ensuring personalized access for users. After logging in, users are greeted with a dedicated Home Page that serves as the central hub to navigate the game, song library, and other features.

Overall, this project showcases the integration of UI design, Kotlin development, API usage, and user authentication to deliver a fun and functional music-based mobile app.

---------------------------

## 🌟 Motivation / Objective

In today’s digital environment, music applications are widely available, but most focus solely on streaming or playlist creation. Users who want to test their music knowledge, identify unknown songs, or casually learn new tracks often need to use multiple separate apps.

This results in several pain points:

Fragmented user experience — Quiz apps, song identification apps, and music browsing apps exist separately, forcing users to switch between platforms.

Lack of casual learning tools — Many music apps do not provide features that help users learn song titles, artists, or genres in an interactive way.

Limited engagement — Streaming apps prioritize passive listening, but do not offer game-like elements that increase user retention and enjoyment.

From a project perspective, this gap presents an opportunity to create a single, integrated platform that addresses these shortcomings. By combining quiz mechanics, a searchable song library, and external API–based song identification, the app delivers both entertainment and practical value.

This project also provides business value by targeting a segment of users who enjoy music-based games, trivia, and discovery tools — features proven to greatly increase engagement and session duration in entertainment apps.

---------------------------

## 📚 App Category of the App

Primary Category: Music & Audio & Entertainment

This application falls under the Music & Entertainment category, as it focuses on providing an interactive experience involving song quizzes, music discovery, and song identification. The app combines fun gameplay with practical tools that allow users to explore and learn about music.

---------------------------

## Declaration of LLM Used

- chatgpt
- copilot


## Tasks & Featured for Each Member for Stage 1

| Name| Feature| Description | 
|------------------|------------------|------------------|
| Liew Zhan Yang | Song Guessing Game | Users listen to a short clip of a song and must guess its title; Points are awarded for correct guesses |
| Liew Zhan Yang | Home navigation | Link all pages together |
| Garence | Song Identifier | ... |
| Jayden | Song Library and search | ... |
| Leeuwin | Login/SignUp | ... |




### Planned Tasks & Featured for Each Member for Stage 2

|Feature| Description | 
|------------------|------------------|
| Difficulty Levels (Easy / Medium / Hard) | Easy = longer clips, fewer options; Hard = short clips, more options | 
| Different style music mode | KPop , Jazz , JPop  ,Rock,.....| 
|  Background Playback Service | A continuous “radio” that plays random songs from packs when app is in background. | 
|  Sleep Timer Activity | Let user set a timer to stop all music after N minutes (for sleeping) | 
|  Album | Let the user can store the songs in album  | 
| Leaderboard | Save best scores with player name and show ranked list (top 10)  | 
| Error Handling & Retry UI | Friendly screens when audio fails to load, with retry buttons.  |
| Skip song | User can skip the song when they encounter a difficult song   |
| Song Profile | User can    |
| Song Lyrics | User can    |
| Setting | User can    |



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

---

### **Flow 4: Song Identifier**

### **4.1 Identify Song**
------------------------------

| Step | Action                                     | Expected Outcome                                                                     | Screen/State            |
|------|--------------------------------------------|--------------------------------------------------------------------------------------|-------------------------|
| 1    | User opens **Song Identifier**             | Screen shows a gradient background and a big circular button with a music note icon | Song Identifier Screen  |
| 2    | User taps the **music note** button        | App asks for microphone permission if needed, or starts getting ready to listen     | Permission Dialog / Song Identifier |
| 3    | Permission is granted                      | Button text changes to **“STOP SEARCHING”** and status shows that the app is listening | Listening State         |
| 4    | User lets a song play near the phone       | App records a short sample and then starts processing the audio                     | Processing State        |
| 5    | AudD API finds a matching song             | Screen displays the song title and artist (e.g. `Song: … / Artist: …`)              | Result Shown            |
| 6    | User taps the button again                 | App gets ready to listen for another song and repeats the process                   | Listening State         |

---

Edge Cases and Alternatives
---------------------------

| Scenario                     | Trigger / Condition                                   | Resolution / Alternative Path                                                                 |
|------------------------------|-------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| Microphone Permission Denied | User taps **Deny** on the permission popup           | Show a short message: “Microphone permission required”, and keep status as “Tap the music note to start” |
| Recording Too Short / Invalid| Recording stops too early or file is empty/damaged   | Show message “Recording failed or was too short.” and ask user to tap the music note to try again       |
| No Match Found               | AudD cannot recognise the song                       | Show status “No match found please try again” so user knows to try with clearer or louder audio         |
| Network Error                | No internet connection or request fails              | Show simple error message such as “Network failure: …” and ask the user to try again later              |
| API Error (4xx / 5xx)        | AudD returns an error instead of a normal result     | Show `API error: <code>` at the bottom and reset status to “Tap the music note to start”                |
| User Stops Recording Early   | User taps the button while the app is listening      | App stops recording immediately and still tries to identify the song using the recorded audio           |





# User Problems and Opportunities

### Problem 1: Onboarding and progression feel confusing or frustrating
Many music apps force users through long sign-up flows, confusing menus, and unclear levels or rewards. New users can feel lost and give up before even playing a round.

**Opportunity for our app:**  
Keep the flow simple and direct – let users start playing or identifying songs quickly, with a clean layout and clear labels (e.g. “Play”, “Song Library”, “Song Identifier”). Progression (scores, levels, achievements) is shown in one place, not hidden behind multiple screens.

---

### Problem 2: Hard to track which songs were guessed correctly and what achievements were earned
In most quiz apps, once a round ends, the information disappears. Users can’t easily see:
- Which songs they got right
- Which ones they missed
- What achievements or rewards they have already earned

**Opportunity for our app:**  
Provide a **History / Profile / Stats** section that clearly shows:
- List of songs guessed correctly
- Rounds played and high scores
- Badges or achievements unlocked  
This helps users feel progress over time and motivates them to keep playing.

---

### Problem 3: Song libraries feel limited, repetitive, or inaccurate
Users often complain that song guessing games:
- Repeat the same tracks too often  
- Have poorly organised categories (wrong decade/genre)  
- Don’t cover enough variety for different ages or music tastes

**Opportunity for our app:**  
Design a **flexible, expandable song library**, with:
- Categories like genre, decade, language, and difficulty
- The ability to add more songs over time
- Cleaner organisation so users know what type of music they are playing with

---

### Problem 4: No single app that both identifies a song and lets you interact with it in the same place
Typical user flow today:
1. Use a song ID app (e.g. Shazam-style) to identify the song  
2. Switch to another app (Spotify/YouTube) to listen  
3. Use a separate game app if they want quizzes or challenges  

This means users keep jumping between apps just to enjoy or test themselves on one song.

**Opportunity for our app:**  
Combine **song identification + song library + guessing game** in one app:
- User can identify a song using the **Song Identifier**
- Immediately see it inside the app’s library
- Use it in a quiz/guessing mode or save it as a favourite  
This gives a smoother, all-in-one music experience.

---

### Problem 5: Song recognition isn’t always reliable
Song recognition tools sometimes:
- Fail to detect songs due to noise or low volume
- Struggle with certain audio setups
- Show generic error messages with no guidance

Users are left unsure what to do next.

**Opportunity for our app:**  
Handle recognition failures more gracefully by:
- Showing clear states: “Listening…”, “Processing…”, “No match found, please try again.”
- Allowing users to retry easily
- Still letting users play the **song guessing game** even when identification fails  
This makes the app feel more reliable and less frustrating, even when the external API cannot find a match.




# Competitor Analysis

### Competitor 1: SongPop
**What it does:**  
SongPop is a multiplayer music trivia game where players guess songs from short clips and themed playlists.

**Relevance to our app:**  
Shows how music quizzes use categories (genre, decade, theme) and fast rounds to keep users engaged.

**Opportunity for our app:**  
Apply a similar idea of playlists and difficulty, but keep the experience simpler and combine it with our song identifier feature.

---

### Competitor 2: Guess The Song – Music Quiz
**What it does:**  
A mobile game where users listen to a short audio clip and choose the correct song or artist from multiple-choice options.

**Relevance to our app:**  
Demonstrates that “listen then pick an answer” is easy to understand and works well for casual music games.

**Opportunity for our app:**  
Include proper tracking of correct guesses, scores, and basic achievements, not just instant “right/wrong” feedback.

---

### Competitor 3: Heardle
**What it does:**  
A daily music puzzle where users guess a song from very short snippets, with more of the song revealed after each wrong guess.

**Relevance to our app:**  
Shows the appeal of a simple daily challenge that brings users back regularly.

**Opportunity for our app:**  
Offer a challenge-style mode inspired by Heardle, but also provide free-play rounds and a music library so users can play more than once per day.

---

### Competitor 4: Shazam
**What it does:**  
A song identification app that listens to audio and shows the song title, artist, and links to play it.

**Relevance to our app:**  
Inspires our **Song Identifier** feature, where the app listens to real-world audio and tries to recognise the track.

**Opportunity for our app:**  
Instead of only showing details, allow users to interact with identified songs inside our app (e.g. add to library, use in guessing rounds).

---

### Competitor 5: Spotiguess
**What it does:**  
Spotiguess is a music quiz that connects to a user’s Spotify account and creates quizzes from their playlists and listening history.

**Relevance to our app:**  
Shows the value of quizzes built from curated or personalised song lists.

**Opportunity for our app:**  
Provide an in-app song library with categories and difficulty, without requiring external account linking, and combine this with our built-in song identifier in a single app.




