package com.baccalaureatplus.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class SoundManager {

    private static MediaPlayer startPlayer;
    private static MediaPlayer successPlayer;
    private static MediaPlayer failPlayer;
    private static MediaPlayer clickPlayer;

    static {
        // ✅ Les ressources se trouvent dans src/resources, donc chemin relatif :
        // /sounds/...
        startPlayer = createPlayer("/sounds/start.wav");
        successPlayer = createPlayer("/sounds/success.wav");
        failPlayer = createPlayer("/sounds/fail.wav");
        clickPlayer = createPlayer("/sounds/click.wav");
    }

    private static MediaPlayer createPlayer(String path) {
        try {
            URL resource = SoundManager.class.getResource(path);

            if (resource == null) {
                System.err.println("❌ Impossible de trouver le fichier son : " + path);
                return null;
            }

            Media media = new Media(resource.toString());
            return new MediaPlayer(media);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du lecteur pour : " + path);
            e.printStackTrace();
            return null;
        }
    }

    public static void playStart() {
        try {
            if (startPlayer != null) {
                startPlayer.stop();
                startPlayer.play();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lecture son start");
        }
    }

    public static void playSuccess() {
        try {
            if (successPlayer != null) {
                successPlayer.stop();
                successPlayer.play();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lecture son success");
        }
    }

    public static void playFail() {
        try {
            if (failPlayer != null) {
                failPlayer.stop();
                failPlayer.play();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lecture son fail");
        }
    }

    public static void playClick() {
        try {
            if (clickPlayer != null) {
                clickPlayer.stop();
                clickPlayer.play();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lecture son click");
        }
    }
}