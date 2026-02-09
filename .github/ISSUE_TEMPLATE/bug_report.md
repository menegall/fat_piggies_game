---
name: 🐞 Bug Report
about: Create a report to help us improve Fat Piggies
title: "[BUG] "
labels: bug
assignees: ''
---

## 🐛 Bug Description
**Example:** The player sprite disappears when colliding with the left wall of the Arena.

## 🔁 Steps to Reproduce
1. Launch the game on [Desktop / Android].
2. Navigate to 'Lobby'.
3. Join a match as 'Player 2'.
4. Walk towards the left wall and jump.
5. **Error:** The sprite renders behind the background layer.

## 😯 Expected Behavior
The player should bounce off the wall and remain visible in the foreground.

## 📸 Screenshots / Video
## 📱 Environment Details
* **Device:** [e.g. Samsung Galaxy S21, Pixel Emulator, Desktop PC]
* **OS:** [e.g. Android 12, Windows 10]
* **Game Version:** [e.g. v0.1.0]
* **Rendering Engine:** [e.g. LibGDX Desktop / Android Build]

## 📜 Logcat / Error Stack Trace
```java
Exception in thread "LWJGL Application" java.lang.NullPointerException
    at com.fatpiggies.game.Entities.Player.update(Player.java:45)
