package com.baccalaureatplus.util;

import javax.sound.sampled.*;

public class ToneGenerator {

    /**
     * Joue un son sinusoïdal à la fréquence et durée données.
     * @param freqHz fréquence en Hz
     * @param durationSec durée en secondes
     */
    public static void playTone(double freqHz, double durationSec) {
        try {
            float SAMPLE_RATE = 44100;
            byte[] buf = new byte[1];
            AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();

            for (int i = 0; i < durationSec * SAMPLE_RATE; i++) {
                double angle = i / (SAMPLE_RATE / freqHz) * 2.0 * Math.PI;
                buf[0] = (byte)(Math.sin(angle) * 127.0);
                sdl.write(buf, 0, 1);
            }

            sdl.drain();
            sdl.stop();
            sdl.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
