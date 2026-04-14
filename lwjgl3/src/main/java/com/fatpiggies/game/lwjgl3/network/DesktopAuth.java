package com.fatpiggies.game.lwjgl3.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.fatpiggies.game.network.AuthService;

public class DesktopAuth implements AuthService {
    private final String authUrl;
    private String currentUserId = null;
    private String idToken = null;

    public DesktopAuth(String apiKey) {
        this.authUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + apiKey;
    }

    @Override
    public void signIn(AuthCallback callback) {
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl(authUrl);
        request.setHeader("Content-Type", "application/json");
        request.setContent("{\"returnSecureToken\":true}");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String responseString = httpResponse.getResultAsString();
                Gdx.app.log("DesktopAuth", "RISPOSTA FIREBASE AUTH: " + responseString);
                if (responseString == null || responseString.trim().isEmpty()) {
                    Gdx.app.error("DesktopAuth", "La risposta di Firebase è completamente vuota!");
                    callback.onFailure("Risposta vuota");
                    return;
                }

                try {
                    JsonValue json = new JsonReader().parse(responseString);
                    if (json != null && json.has("error")) {
                        // Nelle API Auth, l'errore è un oggetto, estraiamo il messaggio
                        String errorMsg = json.get("error").getString("message", "Errore Sconosciuto");
                        Gdx.app.error("DesktopAuth", "Google ha rifiutato l'accesso: " + errorMsg);
                        callback.onFailure(errorMsg);
                        return;
                    }

                    currentUserId = json.getString("localId");
                    idToken = json.getString("idToken");

                    Gdx.app.log("DesktopAuth", "Accesso REST riuscito! UID: " + currentUserId);
                    callback.onSuccess(currentUserId);
                } catch (Exception e) {
                    Gdx.app.error("DesktopAuth", "Errore nel parsing JSON", e);
                    callback.onFailure("Errore di parsing");
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("DesktopAuth", "Connessione fallita", t);
                callback.onFailure("Connessione fallita");
            }

            @Override
            public void cancelled() {
                callback.onFailure("Cancellato");
            }
        });
    }

    @Override
    public void signOut() {
        currentUserId = null;
    }

    @Override
    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getIdToken() {
        return idToken;
    }
}
