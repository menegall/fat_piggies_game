# 🖧 Implementing a Custom Backend

**Fat Piggies** is designed with a backend-agnostic architecture. While the default implementation
uses Firebase, the core game logic does not depend on any Firebase-specific SDKs.

We achieved this by using the **Dependency Inversion Principle**. The game communicates with the
network strictly through two interfaces: `DatabaseService` and `AuthService`.

## How to Swap the Backend

If you want to move away from Firebase and use your own server (e.g., WebSockets, Nakama, AWS, or a
custom Node.js backend), follow these steps:

### 1. Implement `AuthService`

Create a new class that implements the `com.fatpiggies.game.network.AuthService` interface.
You will need to handle:

* **`signIn(AuthCallback callback)`**: Authenticate the user silently in the background.
* **`signOut()`**: Clear the user's session.
* **`getCurrentUserId()`**: Return the unique identifier (UID) of the currently authenticated
  player.

### 2. Implement `DatabaseService`

Create a new class that implements the `com.fatpiggies.game.network.DatabaseService` interface.
This is the bridge for all game data. You must implement the following methods to match the game's
lifecycle:

* **Lobby Management:** `createLobby()`, `joinLobby()`, `leaveLobby()`, `getLobbyCodeOnce()`,
  `resetLobbyToWaiting()`
* **Lobby Synchronization:** `listenToLobbyStatus()`, `listenToPlayersSetup()`
* **Game Loop Updates:** `startGame()`, `endGame()`
* **Host Authority:** `pushGameState()`, `listenToInputs()`, `pushFinalRank()`
* **Client Actions:** `pushPlayerInput()`, `listenToGameState()`, `getFinalRank()`
* **Cleanup:** `stopListening()`

### 3. Inject Your New Services

Once your classes are ready, simply pass your new implementations into the game when it boots up
(usually inside your `AndroidLauncher` code).

```java
// Inside Launcher
public class AndroidLauncher extends AndroidApplication {
    private AuthService authService;
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true; // Recommended, but not required.
        
        // Instantiate your custom services
        this.authService = new MyCustomNodeAuth();
        this.databaseService = new MyCustomNodeDatabase();

        initialize(new FatPiggiesGame(this.authService, this.databaseService), configuration);
    }
}
```

By doing this, the entire game will seamlessly route all multiplayer traffic through your new
backend without needing to change a single line of the core game or physics code!
