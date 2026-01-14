package com.baccalaureatplus.network;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class GameSession {
    private String codeSession;
    private String lettre;
    private int maxJoueurs;
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private Map<String, Integer> scores = new ConcurrentHashMap<>();
    private boolean started = false;

    public GameSession(String codeSession) {
        this.codeSession = codeSession;
    }

    public synchronized boolean addClient(ClientHandler client, String nomJoueur) {
        if (clients.size() >= maxJoueurs && maxJoueurs > 0) {
            return false;
        }
        clients.add(client);
        scores.put(nomJoueur, 0);

        System.out.println("👤 Joueur ajouté : " + nomJoueur + " (Session: " + codeSession + ")");
        System.out.println("📊 Joueurs en ligne : " + clients.size() + "/" + maxJoueurs);

        return true;
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            try {
                client.send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startGame(String lettre, int nbJoueurs) {
        this.lettre = lettre;
        this.maxJoueurs = nbJoueurs;
        this.started = true;

        broadcast("START|" + lettre + "|" + nbJoueurs);
        System.out.println("🎮 Partie démarrée : " + codeSession + ", Lettre: " + lettre);
    }

    public void submitScore(String nomJoueur, int score) {
        scores.put(nomJoueur, score);
        broadcast("SCORE_UPDATE|" + nomJoueur + "|" + score);
    }

    public void endGame() {
        String gagnant = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("?");

        StringBuilder results = new StringBuilder("RESULTS|");
        scores.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(e -> results.append(e.getKey()).append(":").append(e.getValue()).append(";"));

        broadcast(results.toString());
        System.out.println("🏆 Gagnant de " + codeSession + " : " + gagnant);
    }

    public String getCodeSession() {
        return codeSession;
    }

    public String getLettre() {
        return lettre;
    }

    public int getClientCount() {
        return clients.size();
    }

    public boolean isStarted() {
        return started;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }
}
