package com.baccalaureatplus.network;

import java.io.PrintWriter;

public class Player {
    String name;
    int score = -1;
    PrintWriter out;

    Player(String name, PrintWriter out) {
        this.name = name;
        this.out = out;
    }
}
