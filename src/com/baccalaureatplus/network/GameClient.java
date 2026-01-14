package com.baccalaureatplus.network;

import java.io.*;
import java.net.*;

public class GameClient {

    private static final int PORT = 5555;
    private String serverIP;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected = false;

    public boolean connect(String ip) {
        this.serverIP = ip;
        try {
            socket = new Socket(serverIP, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            System.out.println("✅ Connecté au serveur " + serverIP + ":" + PORT);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Impossible de se connecter au serveur : " + e.getMessage());
            return false;
        }
    }

    public void send(String msg) {
        if (connected && out != null) {
            out.println(msg);
            System.out.println("📤 Envoyé : " + msg);
        }
    }

    public BufferedReader getIn() {
        return in;
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                connected = false;
                System.out.println("✅ Déconnecté du serveur");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
