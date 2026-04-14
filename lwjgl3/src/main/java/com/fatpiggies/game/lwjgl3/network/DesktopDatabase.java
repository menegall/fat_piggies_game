package com.fatpiggies.game.lwjgl3.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.Timer;
import com.fatpiggies.game.model.utils.GameConstants;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.network.NetworkError;
import com.fatpiggies.game.network.dto.GameState;
import com.fatpiggies.game.network.dto.LobbyInfo;
import com.fatpiggies.game.network.dto.PlayerInput;
import com.fatpiggies.game.network.dto.PlayerSetup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DesktopDatabase implements DatabaseService {

    private static final String TAG = "DesktopDatabase";

    private final String dbUrl;
    private final Json json;

    private Timer.Task lobbyStatusTask;
    private Timer.Task playersSetupTask;
    private Timer.Task inputsTask;
    private Timer.Task gameStateTask;
    private Timer.Task finalRankTask;
    private final DesktopAuth auth;

    public DesktopDatabase(String dbUrl, DesktopAuth auth) {
        this.dbUrl = dbUrl.endsWith("/") ? dbUrl : dbUrl + "/";
        this.json = new Json();
        this.json.setOutputType(JsonWriter.OutputType.json);
        this.auth = auth;
        this.json.setTypeName(null);
        this.json.setQuoteLongValues(false);
        System.out.println(TAG + ": Initialized DesktopDatabase");
    }

    // ==========================================================
    // SCRITTURE E CREAZIONE
    // ==========================================================

    @Override
    public void createLobby(String hostId, String playerName, String playerColor, LobbyCallback callback) {
        if (hostId == null || hostId.isEmpty()) {
            Gdx.app.error(TAG, "createLobby failed: hostId is null or empty");
            callback.onError(NetworkError.LOGIN_REQUIRED);
            return;
        }

        Gdx.app.log(TAG, "Creating new lobby for host: " + playerName);

        // 1. Generiamo noi l'ID della lobby localmente (esattamente come fa .push() su Android)
        String lobbyId = "-L" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 15);
        String lobbyCode = generateRandomCode();

        LobbyInfo info = new LobbyInfo("waiting", lobbyCode, hostId);
        PlayerSetup hostSetup = new PlayerSetup(playerName, playerColor);
        info.playersSetup.put(hostId, hostSetup);

        Map<String, LobbyInfo> payload = new HashMap<>();
        payload.put("info", info);

        // 2. Usiamo PUT direttamente sul percorso dell'ID specifico, aggirando il blocco della cartella madre
        sendRequest(Net.HttpMethods.PUT, "lobbies/" + lobbyId + ".json", json.toJson(payload), new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String response = httpResponse.getResultAsString();
                Gdx.app.log(TAG, "RISPOSTA FIREBASE (createLobby): " + response);

                try {
                    JsonValue result = new JsonReader().parse(response);

                    // Controllo anti-crash in caso di altri errori di permessi
                    if (result != null && result.has("error")) {
                        Gdx.app.error(TAG, "Firebase si è arrabbiato: " + result.getString("error"));
                        callback.onError(NetworkError.DATABASE_ERROR);
                        return;
                    }

                    Gdx.app.log(TAG, "Lobby created successfully. ID: " + lobbyId);
                    callback.onSuccess(lobbyId);
                } catch (Exception e) {
                    Gdx.app.error(TAG, "Error parsing createLobby JSON response", e);
                    callback.onError(NetworkError.DATABASE_ERROR);
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error(TAG, "HTTP request failed", t);
                callback.onError(NetworkError.DATABASE_ERROR);
            }

            @Override
            public void cancelled() {
                callback.onError(NetworkError.DATABASE_ERROR);
            }
        });
    }

    @Override
    public void joinLobby(String lobbyCode, String playerId, String playerName, String playerColor, LobbyCallback callback) {
        if (playerId == null || playerId.isEmpty()) {
            Gdx.app.error(TAG, "joinLobby failed: playerId is null or empty");
            callback.onError(NetworkError.LOGIN_REQUIRED);
            return;
        }

        Gdx.app.log(TAG, "Attempting to join lobby with code: " + lobbyCode);

        // Cerchiamo la lobby con il codice specifico
        String urlParams = "lobbies.json?orderBy=\"info/code\"&equalTo=\"" + lobbyCode + "\"";

        sendRequest(Net.HttpMethods.GET, urlParams, null, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String response = httpResponse.getResultAsString();

                if (response == null || response.equals("null") || response.equals("{}")) {
                    Gdx.app.log(TAG, "joinLobby failed: Lobby not found for code " + lobbyCode);
                    callback.onError(NetworkError.LOBBY_NOT_FOUND);
                    return;
                }

                try {
                    JsonValue lobbies = new JsonReader().parse(response);
                    JsonValue firstLobby = lobbies.child(); // Prende il primo risultato
                    if (firstLobby == null) {
                        Gdx.app.log(TAG, "joinLobby failed: Lobby node is empty");
                        callback.onError(NetworkError.LOBBY_NOT_FOUND);
                        return;
                    }

                    String lobbyId = firstLobby.name;
                    JsonValue info = firstLobby.get("info");

                    // Controlli di validazione (Uguali ad Android)
                    if (!info.getString("status").equals("waiting")) {
                        Gdx.app.log(TAG, "joinLobby failed: Lobby already started");
                        callback.onError(NetworkError.LOBBY_ALREADY_STARTED);
                        return;
                    }

                    JsonValue playersNode = info.get("playersSetup");
                    if (playersNode != null) {
                        if (playersNode.size >= GameConstants.MAX_PLAYERS) {
                            Gdx.app.log(TAG, "joinLobby failed: Lobby is full");
                            callback.onError(NetworkError.LOBBY_FULL);
                            return;
                        }
                        for (JsonValue p : playersNode) {
                            if (p.getString("name").equals(playerName)) {
                                Gdx.app.log(TAG, "joinLobby failed: Name already exists (" + playerName + ")");
                                callback.onError(NetworkError.NAME_ALREADY_EXIST);
                                return;
                            }
                            if (p.has("color") && p.getString("color").equals(playerColor)) {
                                Gdx.app.log(TAG, "joinLobby failed: Color already taken (" + playerColor + ")");
                                callback.onError(NetworkError.COLOR_ALREADY_TAKEN);
                                return;
                            }
                        }
                    }

                    // Aggiungiamo il giocatore tramite PUT
                    Gdx.app.log(TAG, "Validation passed. Putting player " + playerName + " into lobby " + lobbyId);
                    PlayerSetup setup = new PlayerSetup(playerName, playerColor);
                    String path = "lobbies/" + lobbyId + "/info/playersSetup/" + playerId + ".json";

                    sendRequest(Net.HttpMethods.PUT, path, json.toJson(setup), new Net.HttpResponseListener() {
                        @Override
                        public void handleHttpResponse(Net.HttpResponse httpResponse) {
                            Gdx.app.log(TAG, "Successfully joined lobby: " + lobbyId);
                            callback.onSuccess(lobbyId);
                        }

                        @Override
                        public void failed(Throwable t) {
                            Gdx.app.error(TAG, "joinLobby PUT request failed", t);
                            callback.onError(NetworkError.DATABASE_ERROR);
                        }

                        @Override
                        public void cancelled() {
                            Gdx.app.error(TAG, "joinLobby PUT request cancelled");
                            callback.onError(NetworkError.DATABASE_ERROR);
                        }
                    });

                } catch (Exception e) {
                    Gdx.app.error(TAG, "Error parsing joinLobby JSON response", e);
                    callback.onError(NetworkError.DATABASE_ERROR);
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error(TAG, "joinLobby GET request failed", t);
                callback.onError(NetworkError.DATABASE_ERROR);
            }

            @Override
            public void cancelled() {
                Gdx.app.error(TAG, "joinLobby GET request cancelled");
                callback.onError(NetworkError.DATABASE_ERROR);
            }
        });
    }

    @Override
    public void leaveLobby(String lobbyId, String playerId) {
        Gdx.app.log(TAG, "Player " + playerId + " leaving lobby " + lobbyId);
        stopListening();

        // Scopriamo se chi esce è l'host
        sendRequest(Net.HttpMethods.GET, "lobbies/" + lobbyId + "/info/hostId.json", null, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String hostId = httpResponse.getResultAsString().replace("\"", "");
                if (hostId.equals(playerId)) {
                    // L'host esce: Elimina l'intera lobby
                    Gdx.app.log(TAG, "Host left. Deleting the entire lobby.");
                    sendRequest(Net.HttpMethods.DELETE, "lobbies/" + lobbyId + ".json", null, null);
                } else {
                    // Client esce: Elimina solo sé stesso
                    Gdx.app.log(TAG, "Client left. Removing player node.");
                    sendRequest(Net.HttpMethods.DELETE, "lobbies/" + lobbyId + "/info/playersSetup/" + playerId + ".json", null, null);
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error(TAG, "leaveLobby GET hostId failed", t);
            }

            @Override
            public void cancelled() {
            }
        });
    }

    @Override
    public void startGame(String lobbyId) {
        Gdx.app.log(TAG, "Starting game for lobby: " + lobbyId);
        sendRequest(Net.HttpMethods.PUT, "lobbies/" + lobbyId + "/info/status.json", "\"playing\"", null);
    }

    @Override
    public void endGame(String lobbyId) {
        Gdx.app.log(TAG, "Ending game for lobby: " + lobbyId);
        sendRequest(Net.HttpMethods.PUT, "lobbies/" + lobbyId + "/info/status.json", "\"over\"", null);
    }

    @Override
    public void resetLobbyToWaiting(String lobbyId) {
        Gdx.app.log(TAG, "Resetting lobby to waiting state: " + lobbyId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("info/status", "waiting");
        updates.put("inputs", null);
        updates.put("game_state", null);

        // In REST API, PATCH si usa per aggiornare più campi contemporaneamente senza sovrascrivere il resto
        sendRequest("PATCH", "lobbies/" + lobbyId + ".json", json.toJson(updates), null);
    }

    @Override
    public void pushGameState(String lobbyId, GameState state) {
        sendRequest(Net.HttpMethods.PUT, "lobbies/" + lobbyId + "/game_state.json", json.toJson(state), null);
    }

    @Override
    public void pushPlayerInput(String lobbyId, String playerId, PlayerInput data) {
        sendRequest(Net.HttpMethods.PUT, "lobbies/" + lobbyId + "/inputs/" + playerId + ".json", json.toJson(data), null);
    }

    @Override
    public void pushFinalRank(String lobbyId, List<String> rankedPlayerIds) {
        Gdx.app.log(TAG, "Pushing final rank for lobby: " + lobbyId);
        sendRequest(Net.HttpMethods.PUT, "lobbies/" + lobbyId + "/final_rank.json", json.toJson(rankedPlayerIds), null);
    }


    // ==========================================================
    // LETTURE E LISTENER (POLLING)
    // ==========================================================

    @Override
    public void getLobbyCodeOnce(String lobbyId, CodeCallback callback) {
        sendRequest(Net.HttpMethods.GET, "lobbies/" + lobbyId + "/info/code.json", null, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String code = httpResponse.getResultAsString().replace("\"", "");
                if (code.equals("null")) {
                    Gdx.app.error(TAG, "getLobbyCodeOnce failed: code is null");
                    callback.onError(NetworkError.LOBBY_NOT_FOUND);
                } else {
                    callback.onCodeRetrieved(code);
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error(TAG, "getLobbyCodeOnce GET request failed", t);
                callback.onError(NetworkError.DATABASE_ERROR);
            }

            @Override
            public void cancelled() {
                callback.onError(NetworkError.DATABASE_ERROR);
            }
        });
    }

    @Override
    public void listenToLobbyStatus(String lobbyId, LobbyStatusCallback callback) {
        Gdx.app.log(TAG, "Starting polling for LobbyStatus...");
        lobbyStatusTask = schedulePolling("lobbies/" + lobbyId + "/info/status.json", 1.0f, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                String status = response.getResultAsString().replace("\"", "");
                if (status.equals("null")) {
                    callback.onError(NetworkError.LOBBY_NOT_FOUND);
                } else {
                    callback.onStatusUpdated(status);
                }
            }

            @Override
            public void failed(Throwable t) {
            }

            @Override
            public void cancelled() {
            }
        });
    }

    @Override
    public void listenToPlayersSetup(String lobbyId, PlayersSetupCallback callback) {
        Gdx.app.log(TAG, "Starting polling for PlayersSetup...");
        playersSetupTask = schedulePolling("lobbies/" + lobbyId + "/info/playersSetup.json", 1.0f, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                String resStr = response.getResultAsString();
                if (resStr == null || resStr.equals("null")) {
                    callback.onError(NetworkError.LOBBY_NOT_FOUND);
                    return;
                }
                try {
                    JsonValue root = new JsonReader().parse(resStr);
                    Map<String, PlayerSetup> setupMap = new HashMap<>();

                    // PARSING MANUALE: Navighiamo i figli del JSON uno per uno
                    for (JsonValue child : root) {
                        // Estraiamo i campi in sicurezza
                        String name = child.has("name") ? child.getString("name") : "Sconosciuto";

                        // Controlliamo sia "colorStr" che "color" a seconda di come l'hai chiamato in PlayerSetup
                        String colorStr = "RED"; // Valore di default
                        if (child.has("colorStr")) colorStr = child.getString("colorStr");
                        else if (child.has("color")) colorStr = child.getString("color");

                        // Ricreiamo l'oggetto a mano
                        PlayerSetup p = new PlayerSetup(name, colorStr);
                        setupMap.put(child.name, p); // child.name contiene l'UID del giocatore (es. "WToMXUw...")
                    }

                    callback.onPlayersSetupUpdated(setupMap);
                } catch (Exception e) {
                    Gdx.app.error(TAG, "Error parsing PlayersSetup JSON", e);
                    callback.onError(NetworkError.DATABASE_ERROR);
                }
            }

            @Override
            public void failed(Throwable t) {
            }

            @Override
            public void cancelled() {
            }
        });
    }

    @Override
    public void listenToInputs(String lobbyId, InputsCallback callback) {
        if (inputsTask != null) inputsTask.cancel();

        Gdx.app.log(TAG, "Starting robust polling for Inputs...");
        inputsTask = schedulePolling("lobbies/" + lobbyId + "/inputs.json", 0.1f, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                final String resStr = response.getResultAsString();

                if (resStr == null || resStr.equals("null") || resStr.trim().isEmpty()) return;

                try {
                    JsonValue root = new JsonReader().parse(resStr);
                    final Map<String, PlayerInput> inputsMap = new HashMap<>();

                    for (JsonValue child : root) {
                        PlayerInput input = new PlayerInput();
                        // Firebase REST spesso trasforma i float in double o int.
                        // Usiamo .asFloat() per forzare la conversione corretta.
                        if (child.has("jx")) input.jx = child.get("jx").asFloat();
                        if (child.has("jy")) input.jy = child.get("jy").asFloat();

                        inputsMap.put(child.name, input);
                    }

                    // Riportiamo l'esecuzione sul thread principale di LibGDX
                    Gdx.app.postRunnable(() -> callback.onInputsReceived(inputsMap));

                } catch (Exception e) {
                    Gdx.app.error(TAG, "INPUT PARSING ERROR: " + e.getMessage());
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error(TAG, "Input Polling failed", t);
            }

            @Override
            public void cancelled() {
            }
        });
    }

    @Override
    public void listenToGameState(String lobbyId, GameStateCallback callback) {
        if (gameStateTask != null) gameStateTask.cancel();

        Gdx.app.log(TAG, "Starting robust polling for GameState...");
        gameStateTask = schedulePolling("lobbies/" + lobbyId + "/game_state.json", 0.1f, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                final String resStr = response.getResultAsString();

                if (resStr == null || resStr.equals("null") || resStr.trim().isEmpty()) return;

                try {
                    // Usiamo un'istanza pulita di Json per evitare conflitti con impostazioni globali
                    Json pureJson = new Json();
                    pureJson.setIgnoreUnknownFields(true);

                    final GameState state = pureJson.fromJson(GameState.class, resStr);

                    if (state != null) {
                        Gdx.app.postRunnable(() -> callback.onDataReceived(state));
                    }
                } catch (Exception e) {
                    Gdx.app.error(TAG, "GAMESTATE PARSING ERROR: " + e.getMessage());
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error(TAG, "GameState Polling failed", t);
            }

            @Override
            public void cancelled() {
            }
        });
    }

    @Override
    public void getFinalRank(String lobbyId, FinalRankCallback callback) {
        Gdx.app.log(TAG, "Starting polling for FinalRank...");
        // Usa un task che si cancella da solo appena trova i dati
        finalRankTask = schedulePolling("lobbies/" + lobbyId + "/final_rank.json", 1.0f, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                String resStr = response.getResultAsString();
                if (!resStr.equals("null")) {
                    try {
                        List<String> rank = json.fromJson(ArrayList.class, String.class, resStr);
                        Gdx.app.log(TAG, "Final rank retrieved successfully");
                        callback.onRankRetrieved(rank);
                        if (finalRankTask != null) finalRankTask.cancel();
                    } catch (Exception e) {
                        Gdx.app.error(TAG, "Error parsing FinalRank JSON", e);
                        callback.onError(NetworkError.DATABASE_ERROR);
                    }
                }
            }

            @Override
            public void failed(Throwable t) {
            }

            @Override
            public void cancelled() {
            }
        });
    }

    @Override
    public void stopListening() {
        Gdx.app.log(TAG, "Stopping all active polling tasks.");
        if (lobbyStatusTask != null) lobbyStatusTask.cancel();
        if (playersSetupTask != null) playersSetupTask.cancel();
        if (inputsTask != null) inputsTask.cancel();
        if (gameStateTask != null) gameStateTask.cancel();
        if (finalRankTask != null) finalRankTask.cancel();
    }


    // ==========================================================
    // METODI UTILITY PRIVATI
    // ==========================================================

    private void sendRequest(String method, String endpoint, String content, Net.HttpResponseListener listener) {
        Net.HttpRequest request = new Net.HttpRequest(method);
        String url = dbUrl + endpoint;
        String token = auth.getIdToken();

        if (token != null)
            url += (url.contains("?") ? "&auth=" : "?auth=") + token;

        request.setUrl(url);

        if (content != null) {
            request.setHeader("Content-Type", "application/json");
            request.setContent(content);
        }

        if (listener == null) {
            listener = new Net.HttpResponseListener() {
                @Override
                public void handleHttpResponse(Net.HttpResponse httpResponse) {
                }

                @Override
                public void failed(Throwable t) {
                    Gdx.app.error(TAG, "Silent request failed: " + endpoint, t);
                }

                @Override
                public void cancelled() {
                }
            };
        }

        Gdx.net.sendHttpRequest(request, listener);
    }

    private Timer.Task schedulePolling(final String endpoint, float intervalSeconds, final Net.HttpResponseListener listener) {
        Timer.Task task = new Timer.Task() {
            @Override
            public void run() {
                sendRequest(Net.HttpMethods.GET, endpoint, null, listener);
            }
        };
        Timer.schedule(task, 0, intervalSeconds);
        return task;
    }

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
}
