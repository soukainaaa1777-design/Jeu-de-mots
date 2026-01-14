package com.baccalaureatplus.network;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private GameServer server;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String codeSession;
    private String nomJoueur;

    public ClientHandler(GameServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                traiterMessage(message);
            }
        } catch (IOException e) {
            System.out.println("❌ Client déconnecté");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (codeSession != null && nomJoueur != null) {
                GameSession session = server.getOrCreateSession(codeSession);
                session.removeClient(this);
            }
            server.removeClient(socket);
        }
    }

    private void traiterMessage(String message) {
        String[] parts = message.split("\\|", -1);

        switch (parts[0]) {
            case "JOIN":
                if (parts.length >= 3) {
                    codeSession = parts[1];
                    nomJoueur = parts[2];

                    GameSession session = server.getOrCreateSession(codeSession);
                    if (session.addClient(this, nomJoueur)) {
                        send("JOINED|" + codeSession + "|" + nomJoueur);
                        server.broadcastToSession(codeSession,
                                "PLAYER_JOINED|" + nomJoueur + "|" + session.getClientCount());
                    } else {
                        send("ERROR|Session pleine");
                    }
                }
                break;

            case "START_GAME":
                if (codeSession != null && parts.length >= 3) {
                    String lettre = parts[1];
                    int nbJoueurs = Integer.parseInt(parts[2]);
                    GameSession s = server.getOrCreateSession(codeSession);
                    s.startGame(lettre, nbJoueurs);
                }
                break;

            case "SUBMIT_SCORE":
                if (codeSession != null && parts.length >= 3) {
                    try {
                        int score = Integer.parseInt(parts[2]);
                        GameSession s = server.getOrCreateSession(codeSession);
                        s.submitScore(parts[1], score);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case "END_GAME":
                if (codeSession != null) {
                    GameSession s = server.getOrCreateSession(codeSession);
                    s.endGame();
                }
                break;
        }
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
