package com.fatpiggies.game.android.network;

import static com.fatpiggies.game.utils.Config.TAG_DATABASE;

import android.util.Log;

import androidx.annotation.NonNull;

import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.network.NetworkError;
import com.fatpiggies.game.network.dto.GameState;
import com.fatpiggies.game.network.dto.LobbyInfo;
import com.fatpiggies.game.network.dto.PlayerInput;
import com.fatpiggies.game.network.dto.PlayerSetup;
import com.fatpiggies.game.view.PlayerColor;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.Map;
import java.util.Random;

public class AndroidDatabase implements DatabaseService {
    // DOC: https://firebase.google.com/docs/database/android/read-and-write?hl=it&authuser=0

    private final DatabaseReference lobbiesRef;
    private DatabaseReference lobbyStatusRef;
    private DatabaseReference playersSetupRef;

    private com.google.firebase.database.ValueEventListener lobbyStatusListener;
    private com.google.firebase.database.ValueEventListener playersSetupListener;

    private DatabaseReference inputsRef;
    private com.google.firebase.database.ValueEventListener inputsListener;

    private DatabaseReference gameStateRef;
    private com.google.firebase.database.ValueEventListener gameStateListener;

    public AndroidDatabase() {
        lobbiesRef = FirebaseDatabase.getInstance().getReference().child("lobbies");
    }

    @Override
    public void createLobby(String hostId, String playerName, PlayerColor playerColor, LobbyCallback callback) {
        final long startTime = System.currentTimeMillis();
        Log.i(TAG_DATABASE, playerName + " create a new lobby");
        // Push new lobby node and get its ID
        DatabaseReference newLobbyRef = lobbiesRef.push();
        String lobbyId = newLobbyRef.getKey();
        if (isLobbyIdNull(lobbyId, callback)) return;

        newLobbyRef.onDisconnect().removeValue(); // If host disconnect remove lobby node

        String lobbyCode = generateRandomCode();

        LobbyInfo info = new LobbyInfo("waiting", lobbyCode, hostId);
        PlayerSetup hostSetup = new PlayerSetup(playerName, playerColor);

        info.playersSetup.put(hostId, hostSetup); // Add host to playersSetup

        // Set lobby info in DB
        newLobbyRef.child("info").setValue(info)
            .addOnSuccessListener(aVoid -> {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                Log.d(TAG_DATABASE, "Lobby created successfully. Duration: " + duration + " ms");
                callback.onSuccess(lobbyId);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG_DATABASE, "Error creating lobby: " + e.getMessage());
                callback.onError(NetworkError.DATABASE_ERROR);
            });
    }

    @Override
    public void joinLobby(String lobbyCode, String playerId, String playerName, PlayerColor playerColor, LobbyCallback callback) {
        final long startTime = System.currentTimeMillis();
        Log.i(TAG_DATABASE, playerName + " join lobby " + lobbyCode);
        // Find lobby by code
        lobbiesRef.orderByChild("info/code").equalTo(lobbyCode).get()
            .addOnCompleteListener(task -> {
                if (hasTaskFailed(task, callback)) return;

                // Get the result of the query
                com.google.firebase.database.DataSnapshot dataSnapshot = task.getResult();

                if (isLobbyMissing(dataSnapshot, callback, lobbyCode)) return;
                // Iterate through the lobbies and find the first one with status "waiting"
                for (com.google.firebase.database.DataSnapshot lobbySnapshot : dataSnapshot.getChildren()) {
                    String lobbyId = lobbySnapshot.getKey();
                    if (isLobbyIdNull(lobbyId, callback)) return;
                    if (isGameAlreadyStarted(lobbySnapshot, callback)) return;
                    if (isLobbyFull(lobbySnapshot, callback)) return;
                    if (isPlayerNameTaken(lobbySnapshot, playerName, callback)) return;
                    if (isPlayerColorTaken(lobbySnapshot, playerColor, callback)) return;

                    assert lobbyId != null; // Assert that lobbyId is not null
                    DatabaseReference playerSetupRef = lobbiesRef.child(lobbyId)
                        .child("info/playersSetup").child(playerId);

                    playerSetupRef.onDisconnect().removeValue(); // If client disconnect remove node

                    // Prepare player setup
                    long numPlayers = lobbySnapshot.child("info")
                        .child("playersSetup").getChildrenCount();
                    PlayerSetup setup = new PlayerSetup(playerName, playerColor);

                    // Write player setup to the database
                    playerSetupRef.setValue(setup)
                        .addOnSuccessListener(aVoid -> {
                            long endTime = System.currentTimeMillis();
                            long duration = endTime - startTime;
                            Log.d(TAG_DATABASE, playerName + " joined lobby " + lobbyId
                                + " successfully. Duration: " + duration + " ms");
                            callback.onSuccess(lobbyId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG_DATABASE, "Error joining lobby: " + lobbyId + ". " + e.getMessage());
                            callback.onError(NetworkError.DATABASE_ERROR);
                        });
                }
            });

    }

    @Override
    public void leaveLobby(String lobbyId, String playerId) {
        Log.i(TAG_DATABASE, "Player " + playerId + " leave the lobby " + lobbyId);
        DatabaseReference lobbyRef = lobbiesRef.child(lobbyId);
        // Check who is the host
        lobbyRef.child("info/hostId").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String hostId = task.getResult().getValue(String.class);

                if (playerId.equals(hostId)) {
                    // CASE 1: Host is leaving.
                    // Delete all the lobby node
                    lobbyRef.removeValue()
                        .addOnSuccessListener(aVoid -> Log.d(TAG_DATABASE, "Lobby deleted successfully"))
                        .addOnFailureListener(e -> Log.e(TAG_DATABASE, "Error: " + e.getMessage()));
                } else {
                    // CASE 2: Client is leaving.
                    // Remove only the player from the playersSetup node
                    lobbyRef.child("info/playersSetup").child(playerId).removeValue()
                        .addOnSuccessListener(aVoid -> Log.d(TAG_DATABASE, "Client leave the lobby successfully."))
                        .addOnFailureListener(e -> Log.e(TAG_DATABASE, "Error: " + e.getMessage()));
                }
            } else {
                Log.e(TAG_DATABASE, "Impossible determine host.");
            }
        });

        stopListening();
    }

    @Override
    public void startGame(String lobbyId) {
        Log.i(TAG_DATABASE, "Start Game");
        lobbiesRef.child(lobbyId).child("info/status").setValue("playing");
    }

    @Override
    public void endGame(String lobbyId) {
        Log.i(TAG_DATABASE, "End Game");
        lobbiesRef.child(lobbyId).child("info/status").setValue("over");
    }

    @Override
    public void resetLobbyToWaiting(String lobbyId) {
        Log.i(TAG_DATABASE, "Reset lobby to WAITING");

        DatabaseReference lobbyRef = lobbiesRef.child(lobbyId);
        Map<String, Object> updates = new java.util.HashMap<>();

        updates.put("info/status", "waiting");
        updates.put("inputs", null);
        updates.put("game_state", null);

        lobbyRef.updateChildren(updates);
    }

    @Override
    public void listenToPlayersSetup(String lobbyId, PlayersSetupCallback callback) {
        playersSetupRef = lobbiesRef.child(lobbyId).child("info").child("playersSetup");
        // Create a listener to listen for changes in the lobby info node
        playersSetupListener = new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    GenericTypeIndicator<Map<String, PlayerSetup>> typeIndicator = new GenericTypeIndicator<Map<String, PlayerSetup>>() {
                    };
                    Map<String, PlayerSetup> playersSetup = snapshot.getValue(typeIndicator);
                    if (playersSetup != null) {
                        callback.onPlayersSetupUpdated(playersSetup);
                    }
                }
                if (!snapshot.exists()) {
                    callback.onError(NetworkError.LOBBY_NOT_FOUND);
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                callback.onError(NetworkError.DATABASE_ERROR);
            }
        };

        // Attach the listener to the lobby info node
        playersSetupRef.addValueEventListener(playersSetupListener);
    }

    @Override
    public void listenToLobbyStatus(String lobbyId, LobbyStatusCallback callback) {
        lobbyStatusRef = lobbiesRef.child(lobbyId).child("info").child("status");

        // Create a listener to listen for changes in the lobby info node
        lobbyStatusListener = new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.getValue(String.class);
                    if (status != null) {
                        callback.onStatusUpdated(status);
                    }
                }
                if (!snapshot.exists()) {
                    callback.onError(NetworkError.LOBBY_NOT_FOUND);
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                callback.onError(NetworkError.DATABASE_ERROR);
            }
        };

        //  Attach the listener to the lobby info node
        lobbyStatusRef.addValueEventListener(lobbyStatusListener);
        Log.d(TAG_DATABASE, "listenToLobbyStatus");
    }

    @Override
    public void getLobbyCodeOnce(String lobbyId, CodeCallback callback) {
        DatabaseReference codeRef = lobbiesRef.child(lobbyId).child("info/code");

        // .get() fetches the value exactly once and returns a Task
        codeRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG_DATABASE, "Error getting lobby code", task.getException());
                callback.onError(NetworkError.DATABASE_ERROR);
                return;
            }

            com.google.firebase.database.DataSnapshot snapshot = task.getResult();
            if (snapshot.exists()) {
                String code = snapshot.getValue(String.class);
                callback.onCodeRetrieved(code);
            } else {
                callback.onError(NetworkError.LOBBY_NOT_FOUND);
            }
        });
    }

    @Override
    public void pushGameState(String lobbyId, GameState state) {
        gameStateRef = lobbiesRef.child(lobbyId).child("game_state");
        // Write the game state data to the database
        gameStateRef.setValue(state);
        // TODO only host can push game state

        // For fast push (10/s) no need of listener, it will only slow the system
    }

    @Override
    public void listenToInputs(String lobbyId, InputsCallback callback) {
        inputsRef = lobbiesRef.child(lobbyId).child("inputs");

        // Create a listener to listen for changes in the player input node
        inputsListener = new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Firebase automatically convert JSON to Java Object
                    Map<String, PlayerInput> inputs = new java.util.HashMap<>();

                    for (com.google.firebase.database.DataSnapshot inputSnapshot : snapshot.getChildren()) {
                        String playerId = inputSnapshot.getKey();
                        PlayerInput input = inputSnapshot.getValue(PlayerInput.class);
                        if (playerId != null && input != null) {
                            inputs.put(playerId, input);
                        }
                    }
                    callback.onInputsReceived(inputs);
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                callback.onError(NetworkError.DATABASE_ERROR);
            }
        };

        // Attach the listener to the player input node
        inputsRef.addValueEventListener(inputsListener);
    }

    @Override
    public void pushPlayerInput(String lobbyId, String playerId, PlayerInput data) {
        DatabaseReference playerInputRef = lobbiesRef.child(lobbyId).child("inputs").child(playerId);
        // Write the player input data to the database
        playerInputRef.setValue(data);

        // For fast push (10/s) no need of listener, it will only slow the system
    }

    @Override
    public void listenToGameState(String lobbyId, GameStateCallback callback) {
        gameStateRef = lobbiesRef.child(lobbyId).child("game_state");

        // Create a listener to listen for changes in the game state node
        gameStateListener = new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Firebase automatically convert JSON to Java Object
                    GameState state = snapshot.getValue(GameState.class);
                    if (state != null) {
                        callback.onDataReceived(state);
                    }
                } else {
                    // Node doesn't exist anymore! Host or server deleted it.
                    Log.w(TAG_DATABASE, "Lobby was destroyed by host or server.");
                    callback.onError(NetworkError.LOBBY_NOT_FOUND);

                    // TODO Controller Must call databaseService.stopListening() to destroy listener.
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                callback.onError(NetworkError.DATABASE_ERROR);
            }
        };

        // Attach the listener to the game state node
        gameStateRef.addValueEventListener(gameStateListener);
    }

    @Override
    public void stopListening() {
        // Remove listener to avoid memory leak and improve performance
        if (lobbyStatusRef != null && lobbyStatusListener != null) {
            lobbyStatusRef.removeEventListener(lobbyStatusListener);
            lobbyStatusListener = null;
        }

        if (playersSetupRef != null && playersSetupListener != null) {
            playersSetupRef.removeEventListener(playersSetupListener);
            playersSetupListener = null;
        }

        if (inputsRef != null && inputsListener != null) {
            inputsRef.removeEventListener(inputsListener);
            inputsListener = null;
        }

        if (gameStateRef != null && gameStateListener != null) {
            gameStateRef.removeEventListener(gameStateListener);
            gameStateListener = null;
        }
    }

    // --- Helper Method ---
    private String generateRandomCode() {
        int length = 4;
        String chars = "0123456789";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return code.toString();
    }

    private boolean hasTaskFailed(com.google.android.gms.tasks.Task<?> task,
                                  LobbyCallback callback) {
        if (!task.isSuccessful()) {
            Exception e = task.getException();
            String errorMsg = (e != null && e.getMessage() != null) ? e.getMessage() : "Unknown Error";
            callback.onError(NetworkError.DATABASE_ERROR);
            return true;
        } else {
            return false;
        }
    }

    private boolean isLobbyMissing(com.google.firebase.database.DataSnapshot dataSnapshot,
                                   LobbyCallback callback,
                                   String lobbyCode) {
        if (!dataSnapshot.exists()) {
            callback.onError(NetworkError.LOBBY_NOT_FOUND);
            return true;
        } else return false;
    }

    private boolean isLobbyIdNull(String lobbyId, LobbyCallback callback) {
        if (lobbyId == null) {
            Log.d(TAG_DATABASE, "Something went wrong with lobbyId");
            callback.onError(NetworkError.DATABASE_ERROR);
            return true;
        } else return false;
    }

    private boolean isLobbyFull(com.google.firebase.database.DataSnapshot lobbySnapshot,
                                LobbyCallback callback) {
        long numPlayers = lobbySnapshot.child("info").child("playersSetup").getChildrenCount();
        if (numPlayers >= 4) {
            callback.onError(NetworkError.LOBBY_FULL);
            return true; // Indicates the lobby is full and the error was handled
        }
        return false;
    }

    private boolean isPlayerNameTaken(com.google.firebase.database.DataSnapshot lobbySnapshot,
                                      String playerName,
                                      LobbyCallback callback) {
        for (com.google.firebase.database.DataSnapshot playerSnapshot : lobbySnapshot.child("info/playersSetup").getChildren()) {
            String playerNameDB = playerSnapshot.child("name").getValue(String.class);
            if (java.util.Objects.equals(playerNameDB, playerName)) {
                callback.onError(NetworkError.NAME_ALREADY_EXIST);
                return true; // Indicates the name is taken and the error was handled
            }
        }
        return false;
    }

    private boolean isPlayerColorTaken(com.google.firebase.database.DataSnapshot lobbySnapshot,
                                       PlayerColor playerColor,
                                       LobbyCallback callback) {
        for (com.google.firebase.database.DataSnapshot playerSnapshot : lobbySnapshot.child("info/playersSetup").getChildren()) {
            String colorStr = playerSnapshot.child("color").getValue(String.class);
            if (colorStr != null && colorStr.equals(playerColor.name())) {
                callback.onError(NetworkError.COLOR_ALREADY_TAKEN);
                return true;
            }
        }
        return false;
    }

    private boolean isGameAlreadyStarted(com.google.firebase.database.DataSnapshot lobbySnapshot,
                                         LobbyCallback callback) {
        String status = lobbySnapshot.child("info").child("status").getValue(String.class);
        if (!"waiting".equals(status)) {
            callback.onError(NetworkError.LOBBY_ALREADY_STARTED);
            return true; // Indicates the game is not waiting and the error was handled
        }
        return false;
    }
}
