# 🐽 Fat Piggies

> **Status:** Final Version
>
> **Engine:** LibGDX (Java)

Fat Piggies is a high-energy, multiplayer physics-based brawler developed as a collaborative
university project. The goal is simple: be the heftiest pig on the block and shove your opponents
off the arena to claim the title of Champion. Developed by a team of 7 for
TDT4120-Software Architecture course at NTNU.

---

## 📱 How to Download & Install

You don't need to be a developer to play! You can grab the latest playable version right here on
GitHub:

1. Go to the [Releases](https://github.com/menegall/fat_piggies_game/releases) page on the right
   side of this repository.
2. Under the latest release, click on `android-release.apk` to download the file to your Android
   phone.
3. Open the downloaded file to install the game.
    * *Note: Your phone might warn you about installing apps from "Unknown Sources."
      You will need to allow this in your device settings to proceed with the installation.*
4. Open the app and get ready to brawl!

## 🎮 How to play

In a world where weight matters, players take control of bouncy pigs.
Using momentum and physics, you must bump, slide, and ram into your rivals to knock them out
of the field of play.

1. **Create a Profile:** Enter your name and pick your favorite pig color.
2. **Host or Join:** One player taps Host to create a lobby and receives a 4-digit code. Give that
   code to your friend. Up to 3 other friends can enter the code, tap join and enter the lobby.
3. **The Brawl:** Use the virtual joystick to build up speed and ram into your opponents.
4. **Power-ups:** Keep an eye out for items spawning in the arena!
   Grab an Apple for a speed boost, a Donut to become heavy and immovable, or a Life to heal up. You
   can also grab a beer, but be aware of the consequence.
5. **Survive & Win:** Push your rivals out of bounds. The last piggy remaining in the arena wins!
6. **Customize:** Winning games earns you coins. Head to the Shop to spend your hard-earned coins
   and unlock amazing new arena themes (like Space, Volcano, or Pirates).

### 🚀 Key Features
* **Multi-Device Connectivity:** Seamlessly join the action using your own device.
* **Lobby System:** A dedicated joining lobby to gather your friends before the mayhem starts.
* **Physics-Driven Combat:** Master the weight and "bounce" of your pig to dominate the arena.
* **Local/Network Multiplayer:** Designed for social play and competitive fun.

---

## 🛠️ Tech Stack

* **Language:** Java
* **Framework:** [LibGDX](https://libgdx.com/), [Ashley](https://github.com/libgdx/ashley)
* **IDE:** Android Studio
* **Networking:** Firebase Realtime Database & Firebase Auth
    * *Backend Agnostic:* The game logic is decoupled from Firebase. You can easily swap the backend
      by creating new classes that implement our `DatabaseService` and `AuthService` interfaces.

---

## 💻 Getting Started (Development)

To run the project locally and contribute to the code:

1.  **Clone the repo:**
    ```bash
    git clone https://github.com/menegall/fat_piggies_game.git
    ```
2.  **Open in Android Studio:**
    * File → Open → Select the `build.gradle` file in the root folder.
3.  **Sync Gradle:** Wait for dependencies to download.
4. **Run:** Select the `Android` configuration and press Play.

> **Firebase Setup Required:**
> This project uses Firebase for multiplayer networking. The database credentials
> (`google-services.json`) are not included in this repository for security reasons.
> To build and run the game locally, you must create your own Firebase project and place your
> `google-services.json` file inside the `android/` folder.

## 🧩 Game Extension

Take a look at these files based on what you would like to extend in the game:

* [**Add new power-up**](./docs/NEW_POWERUP.md)
* [**Add new theme**](./docs/NEW_THEME.md)
* [**Add new pig color**](./docs/NEW_PIG_COLOR.md)
* [**Implementing a Custom Backend**](./docs/CUSTOM_BACKEND.md)

---

## 👥 The Fat Piggies Team

Built with ❤️ and coffee by:

* **Gabin [Mayer]** - *Developer*
* **Patrick [Menegalli-Boggelli]** - *Developer*
* **Liangchen [Liu]**
* **Paula [Lorentz]**
* **Pia [Pirner]**
* **Gina [Giske]**
* **Ingvild [Kirkaune Sandven]**
