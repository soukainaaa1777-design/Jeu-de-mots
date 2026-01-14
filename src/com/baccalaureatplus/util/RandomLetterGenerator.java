package com.baccalaureatplus.util;

import java.util.Random;

public class RandomLetterGenerator {
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String generate() {
        Random rand = new Random();
        return String.valueOf(LETTERS.charAt(rand.nextInt(LETTERS.length())));
    }
}
