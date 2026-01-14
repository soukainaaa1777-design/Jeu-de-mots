package com.baccalaureatplus.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {

    private static final int PORT = 5555;
    private ServerSocket serverSocket;
    private Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    private Map<Socket, ClientHandler> clientHandlers = new ConcurrentHashMap<>();

    public GameServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            String ip = java.net.InetAddress.getLocalHost().getHostAddress();
            System.out.println("\n" +
                    "╔══════════════════════════════════════╗\n" +
                    "║  🎮 SERVEUR LANCÉ !                 ║\n" +
                    "╠══════════════════════════════════════╣\n" +
                    "║  📡 ENVOIE CETTE IP À TON AMI :     ║\n" +
                    "║  ➡️  " + ip + "                   ║\n" +
                    "║  🔌 Port : " + PORT + "                       ║\n" +
                    "╚══════════════════════════════════════╝\n");
        } catch (IOException e) {
            System.err.println("❌ Erreur : impossible de lancer le serveur");
            e.printStackTrace();
        }
    }

    public void start() {
        new Thread(() -> {
            try {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("✅ Nouveau client connecté : " + clientSocket.getInetAddress());

                    ClientHandler handler = new ClientHandler(this, clientSocket);
                    clientHandlers.put(clientSocket, handler);
                    new Thread(handler).start();
                }
            } catch (IOException e) {
                System.err.println("❌ Erreur serveur");
                e.printStackTrace();
            }
        }).start();
    }

    public GameSession getOrCreateSession(String codeSession) {
        return sessions.computeIfAbsent(codeSession, k -> new GameSession(codeSession));
    }

    public void removeClient(Socket socket) {
        clientHandlers.remove(socket);
    }

    public void broadcastToSession(String codeSession, String message) {
        GameSession session = sessions.get(codeSession);
        if (session != null) {
            session.broadcast(message);
        }
    }

    public Map<String, GameSession> getSessions() {
        return sessions;
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();

        System.out.println("✅ Serveur Socket TCP démarré - en attente de connexions...");

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}