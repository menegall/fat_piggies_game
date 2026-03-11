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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;
import java.util.Random;

public class AndroidDatabase implements DatabaseService {
    // DOC: https://firebase.google.com/docs/database/android/read-and-write?hl=it&authuser=0

    private final DatabaseReference dbRef;
    private DatabaseReference lobbyInfoRef;
    private com.google.firebase.database.ValueEventListener lobbyInfoListener;

    private DatabaseReference inputsRef;
    private com.google.firebase.database.ValueEventListener inputsListener;

    private DatabaseReference gameStateRef;
    private com.google.firebase.database.ValueEventListener gameStateListener;

    public AndroidDatabase() {
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void createLobby(String hostId, String playerName, LobbyCallback callback) {
        final long startTime = System.currentTimeMillis();
        Log.i(TAG_DATABASE, playerName + " create a new lobby");
        DatabaseReference lobbiesRef = dbRef.child("lobbies");
        // Push new lobby node and get its ID
        DatabaseReference newLobbyRef = lobbiesRef.push();
        String lobbyId = newLobbyRef.getKey();

        if (lobbyId == null) {
            callback.onError(NetworkError.DATABASE_ERROR, "Impossible to generate lobby uid");
            Log.w(TAG_DATABASE, "Impossible to generate lobby uid");
            return;
        }

        // If host disconnect remove lobby node
        newLobbyRef.onDisconnect().removeValue();

        // Generate random lobby code
        String lobbyCode = generateRandomCode(4);

        LobbyInfo info = new LobbyInfo("waiting", lobbyCode, hostId);
        PlayerSetup hostSetup = new PlayerSetup(100, playerName);
        // Add host to playersSetup
        info.playersSetup.put(hostId, hostSetup);

        // Set lobby info in DB
        newLobbyRef.child("info").setValue(info)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    Log.d(TAG_DATABASE, "Lobby created successfully. Duration: " + duration + " ms");
                    callback.onSuccess(lobbyId);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG_DATABASE, "Error creating lobby: " + e.getMessage());
                    callback.onError(NetworkError.DATABASE_ERROR, e.getMessage());
                }
            });
    }

    @Override
    public void joinLobby(String lobbyCode, String playerId, String playerName, LobbyCallback callback) {
        final long startTime = System.currentTimeMillis();
        Log.i(TAG_DATABASE, playerName + "join lobby " + lobbyCode);
        DatabaseReference lobbiesRef = dbRef.child("lobbies");

        // Find lobby by code
        lobbiesRef.orderByChild("info/code").equalTo(lobbyCode).get()
            .addOnCompleteListener(task -> {
                // Check if the task was successful
                if (!task.isSuccessful()) {
                    callback.onError(NetworkError.DATABASE_ERROR, "Network Error: " + task.getException().getMessage());
                    return;
                }

                // Get the result of the query
                com.google.firebase.database.DataSnapshot dataSnapshot = task.getResult();

                // Check if the lobby exists
                if (!dataSnapshot.exists()) {
                    callback.onError(NetworkError.LOBBY_NOT_FOUND, "No lobby with code: " + lobbyCode);
                    return;
                }

                // Iterate through the lobbies and find the first one with status "waiting"
                for (com.google.firebase.database.DataSnapshot lobbySnapshot : dataSnapshot.getChildren()) {
                    String lobbyId = lobbySnapshot.getKey();
                    String status = lobbySnapshot.child("info").child("status").getValue(String.class);

                    // Check if the game is "waiting"
                    if (!"waiting".equals(status)) {
                        callback.onError(NetworkError.LOBBY_ALREADY_STARTED, "The game already started");
                        return;
                    }
                    // Check if the lobby is full
                    long numPlayers = lobbySnapshot.child("info")
                        .child("players_setup").getChildrenCount();
                    if (numPlayers >= 4) {
                        callback.onError(NetworkError.LOBBY_FULL, "The lobby is full");
                        return;
                    }
                    // Check if name already exist
                    for (com.google.firebase.database.DataSnapshot playerSnapshot : lobbySnapshot.child("info").child("players_setup").getChildren()) {
                        String playerNameDB = playerSnapshot.child("name").getValue(String.class);
                        if (playerNameDB.equals(playerName)) {
                            callback.onError(NetworkError.NAME_ALREADY_EXIST, "Name already exist");
                            return;
                        }
                    }

                    DatabaseReference playerSetupRef = lobbiesRef.child(lobbyId)
                        .child("info/players_setup").child(playerId);
                    // If client disconnect remove node
                    playerSetupRef.onDisconnect().removeValue();

                    // Prepare player setup
                    PlayerSetup setup = new PlayerSetup(100 + Math.toIntExact(numPlayers), playerName);

                    // Write player setup to the database
                    playerSetupRef.setValue(setup)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                long endTime = System.currentTimeMillis();
                                long duration = endTime - startTime;
                                Log.d(TAG_DATABASE, playerName + " joined lobby " + lobbyId
                                    + " successfully. Duration: " + duration + " ms");
                                callback.onSuccess(lobbyId);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG_DATABASE, "Error joining lobby: " + lobbyId + ". " + e.getMessage());
                                callback.onError(NetworkError.DATABASE_ERROR,
                                    "Error writing to database: " + e.getMessage());
                            }
                        });
                }
            });

    }

    @Override
    public void leaveLobby(String lobbyId, String playerId) {
        Log.i(TAG_DATABASE, "Player" + playerId + " leave the lobby " + lobbyId);
        DatabaseReference lobbyRef = dbRef.child("lobbies").child(lobbyId);
        // Check who is the host
        lobbyRef.child("info/host_id").get().addOnCompleteListener(task -> {
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
                    // Remove only the player from the players_setup node
                    lobbyRef.child("info/players_setup").child(playerId).removeValue()
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
        DatabaseReference lobbiesRef = dbRef.child("lobbies");
        lobbiesRef.child(lobbyId).child("info").child("status").setValue("playing");
    }

    @Override
    public void endGame(String lobbyId) {
        Log.i(TAG_DATABASE, "End Game");
        DatabaseReference lobbiesRef = dbRef.child("lobbies");
        lobbiesRef.child(lobbyId).child("info").child("status").setValue("over");
    }

    @Override
    public void listenToLobbyInfo(String lobbyId, LobbyInfoCallback callback) {
        // Get a reference to the lobby info node
        lobbyInfoRef = dbRef.child("lobbies").child(lobbyId).child("info");

        // Create a listener to listen for changes in the lobby info node
        lobbyInfoListener = new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Firebase automatically convert JSON to Java Object
                    LobbyInfo info = snapshot.getValue(LobbyInfo.class);
                    if (info != null) {
                        callback.onInfoUpdated(info);
                    }
                } else {
                    // Node doesn't exist anymore! Host or server deleted it.
                    Log.w(TAG_DATABASE, "Lobby was destroyed by host or server.");
                    callback.onError(NetworkError.LOBBY_NOT_FOUND, "Lobby Closed");

                    // TODO Controller Must call databaseService.stopListening() to destroy listener.
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                callback.onError(NetworkError.DATABASE_ERROR, error.getMessage());
            }
        };

        // Attach the listener to the lobby info node
        lobbyInfoRef.addValueEventListener(lobbyInfoListener);
    }

    @Override
    public void pushGameState(String lobbyId, GameState state) {
        // Get a reference to the game state node
        gameStateRef = dbRef.child("lobbies").child(lobbyId)
            .child("game_state");
        // Write the game state data to the database
        gameStateRef.setValue(state);
        // TODO only host can push game state

        // For fast push (10/s) no need of listener, it will only slow the system
    }

    @Override
    public void listenToInputs(String lobbyId, InputsCallback callback) {
        // Get a reference to the player input node
        inputsRef = dbRef.child("lobbies").child(lobbyId)
            .child("inputs");

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
                callback.onError(NetworkError.DATABASE_ERROR, error.getMessage());
            }
        };

        // Attach the listener to the player input node
        inputsRef.addValueEventListener(inputsListener);
    }

    @Override
    public void pushPlayerInput(String lobbyId, String playerId, PlayerInput data) {
        // Get a reference to the player input node
        DatabaseReference playerInputRef = dbRef.child("lobbies").child(lobbyId)
            .child("inputs").child(playerId);
        // Write the player input data to the database
        playerInputRef.setValue(data);

        // For fast push (10/s) no need of listener, it will only slow the system
    }

    @Override
    public void listenToGameState(String lobbyId, GameStateCallback callback) {
        // Get a reference to the game state node
        gameStateRef = dbRef.child("lobbies").child(lobbyId).child("game_state");

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
                    callback.onError(NetworkError.LOBBY_NOT_FOUND, "Lobby Closed");

                    // TODO Controller Must call databaseService.stopListening() to destroy listener.
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                callback.onError(NetworkError.DATABASE_ERROR, error.getMessage());
            }
        };

        // Attach the listener to the game state node
        gameStateRef.addValueEventListener(gameStateListener);
    }

    @Override
    public void stopListening() {
        // Remove listener to avoid memory leak and improve performance
        if (lobbyInfoRef != null && lobbyInfoListener != null) {
            lobbyInfoRef.removeEventListener(lobbyInfoListener);
            lobbyInfoListener = null;
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
    private String generateRandomCode(int length) {
        String chars = "0123456789";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return code.toString();
    }
}
