package com.baccalaureatplus.network;

import java.util.*;

public class Room {

    int maxPlayers;
    List<Player> players = new ArrayList<>();
    char letter;

    Room(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        this.letter = (char) ('A' + new Random().nextInt(26));
    }

    void addPlayer(Player p) {
        players.add(p);
    }

    boolean isFull() {
        return players.size() == maxPlayers;
    }

    void startGame() {
        broadcast("START|" + letter + "|60");
    }

    void broadcast(String msg) {
        for (Player p : players) {
            p.out.println(msg);
        }
    }

    boolean allScoresReceived() {
        return players.stream().allMatch(p -> p.score >= 0);
    }

    void sendResults() {
        players.sort((a, b) -> b.score - a.score);
        StringBuilder sb = new StringBuilder("RESULT");
        for (Player p : players) {
            sb.append("|").append(p.name).append(":").append(p.score);
        }
        broadcast(sb.toString());
    }
}
