package com.baccalaureatplus.controller;

import com.baccalaureatplus.service.SyncService;
import com.baccalaureatplus.service.ValidationService;
import com.baccalaureatplus.util.SoundManager;
import com.baccalaureatplus.util.TextUtils;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class SoloGameController {

    @FXML
    private Label lettreLabel, timerLabel, scoreLabel;
    @FXML
    private Button validerBtn, startBtn, quitterBtn;
    @FXML
    private VBox gameContainer;
    @FXML
    private TextField nomJoueurField;

    @FXML
    private TextField fieldPrenom, fieldLegume, fieldFruit,
            fieldAnimal, fieldPays, fieldVille, fieldObjet;

    private Timeline timeline;
    private int tempsRestant = 120;
    private String lettrePartie = "";

    // Service et données de partie
    private SyncService syncService = new SyncService();
    private int idJoueur = -1;
    private int tempsEcoule = 0;

    @FXML
    public void initialize() {
        setFieldsDisabled(true);
        validerBtn.setVisible(false);
        validerBtn.setManaged(false);
        if (gameContainer != null)
            gameContainer.setOpacity(0.4);
    }

    // ====================== LANCER UNE NOUVELLE PARTIE ======================
    @FXML
    private void lancerPartie() {
        // Vérifier si le nom du joueur est saisi
        if (nomJoueurField == null || nomJoueurField.getText().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Nom requis");
            alert.setHeaderText(null);
            alert.setContentText("⚠️ Veuillez saisir votre prénom avant de commencer !");
            alert.showAndWait();
            return;
        }

        SoundManager.playStart(); // 🎵 Son de début

        // Créer le joueur dans la base de données via le service
        String prenomJoueur = nomJoueurField.getText().trim();
        idJoueur = syncService.creerJoueurSolo(prenomJoueur);

        if (idJoueur == -1) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de connexion");
            alert.setHeaderText(null);
            alert.setContentText(
                    "❌ Impossible de créer le joueur dans la base de données.\nVérifiez que MySQL est démarré !");
            alert.showAndWait();
            return;
        }

        System.out.println("✅ Joueur créé avec ID: " + idJoueur + " (" + prenomJoueur + ")");

        startBtn.setVisible(false);
        startBtn.setManaged(false);
        nomJoueurField.setDisable(true);

        validerBtn.setVisible(true);
        validerBtn.setManaged(true);
        validerBtn.setDisable(false);

        gameContainer.setOpacity(1.0);
        setFieldsDisabled(false);

        resetFieldStyles();
        genererNouvelleManche();
    }

    private void genererNouvelleManche() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        lettrePartie = String.valueOf(alphabet.charAt(new Random().nextInt(alphabet.length())));
        lettreLabel.setText(lettrePartie);
        clearFields();
        lancerChrono();
    }

    // ====================== CHRONOMÈTRE ======================
    private void lancerChrono() {
        if (timeline != null)
            timeline.stop();

        tempsRestant = 120;
        tempsEcoule = 0;
        timerLabel.setStyle("-fx-text-fill: #F86DA3; -fx-font-weight: 900;");

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tempsRestant--;
            tempsEcoule++;
            timerLabel.setText(tempsRestant + "s");

            if (tempsRestant <= 10) {
                timerLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
            if (tempsRestant <= 0) {
                timeline.stop();
                validerPartie();
            }
        }));

        timeline.setCycleCount(120);
        timeline.play();
    }

    // ====================== VALIDER ======================
    @FXML
    private void validerPartie() {
        if (timeline != null)
            timeline.stop();
        setFieldsDisabled(true);
        validerBtn.setDisable(true);
        scoreLabel.setText("Vérification...");

        TextField[] fields = {
                fieldPrenom, fieldFruit, fieldLegume,
                fieldAnimal, fieldPays, fieldVille, fieldObjet
        };

        String[] categories = {
                "Prénom", "Fruit", "Légume",
                "Animal", "Pays", "Ville", "Objet"
        };

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < fields.length; i++) {
            TextField f = fields[i];
            String categorie = categories[i];
            String mot = (f != null) ? TextUtils.normaliser(f.getText()) : "";

            if (mot.isEmpty()) {
                // Champ vide = invalide
                animerResultat(f, false);
                futures.add(CompletableFuture.completedFuture(false));
            } else {
                // 1️⃣ VÉRIFIER D'ABORD DANS LE CACHE (mots_valides)
                boolean existeDansCache = syncService.motEstDansCache(mot, categorie);

                if (existeDansCache) {
                    // ✅ Mot trouvé dans le cache → validation instantanée, pas d'appel API
                    System.out.println("✅ Mot trouvé dans le cache: " + mot + " (" + categorie + ")");
                    Platform.runLater(() -> animerResultat(f, true));
                    futures.add(CompletableFuture.completedFuture(true));
                } else {
                    // 2️⃣ PAS DANS LE CACHE → Appel API pour validation
                    System.out.println("🌐 Appel API pour: " + mot + " (" + categorie + ")");
                    CompletableFuture<Boolean> validationFuture = ValidationService.validerMotAvecCategorie(mot,
                            lettrePartie, categorie);

                    final String motFinal = mot;
                    final String categorieFinal = categorie;

                    validationFuture.thenAccept(isValid -> {
                        Platform.runLater(() -> animerResultat(f, isValid));

                        // 3️⃣ Si validé par l'API → ajouter au cache pour la prochaine fois
                        if (isValid) {
                            syncService.cacherMotValide(motFinal, categorieFinal);
                            System.out.println("💾 Mot ajouté au cache: " + motFinal + " (" + categorieFinal + ")");
                        }
                    });

                    futures.add(validationFuture);
                }
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    int score = futures.stream().mapToInt(f -> f.join() ? 10 : 0).sum();

                    // Enregistrer le score dans la base de données via le service
                    if (idJoueur != -1) {
                        int scoreId = syncService.enregistrerScore(idJoueur, score, lettrePartie.charAt(0),
                                tempsEcoule);
                        if (scoreId != -1) {
                            System.out.println("✅ Score enregistré avec ID: " + scoreId + " (" + score + " pts)");
                        } else {
                            System.err.println("❌ Erreur lors de l'enregistrement du score");
                        }
                    }

                    Platform.runLater(() -> {
                        scoreLabel.setText("SCORE : " + score);
                        afficherResultatFinal(score);
                    });
                });
    }

    private void animerResultat(TextField f, boolean valide) {
        if (f == null)
            return;

        if (valide) {
            f.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2px;");
            SoundManager.playSuccess();
        } else {
            f.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            SoundManager.playFail();
            TranslateTransition shake = new TranslateTransition(Duration.millis(50), f);
            shake.setByX(8);
            shake.setCycleCount(4);
            shake.setAutoReverse(true);
            shake.play();
        }
    }

    // ====================== AFFICHER LE SCORE FINAL ======================
    private void afficherResultatFinal(int score) {
        Stage popup = new Stage(StageStyle.UNDECORATED);
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-border-radius: 15;");

        Label label = new Label("SCORE FINAL : " + score);
        label.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        Button replay = new Button("REJOUER");
        replay.setStyle(
                "-fx-background-radius: 15; -fx-background-color: #6c63ff; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
        replay.setOnAction(e -> {
            SoundManager.playClick();
            popup.close();
            resetFieldStyles();
            lancerPartie();
        });

        root.getChildren().addAll(label, replay);
        popup.setScene(new Scene(root));
        popup.show();
    }

    // ====================== RETOUR AU MENU ======================
    @FXML
    private void retourMenu(ActionEvent event) {
        try {
            if (timeline != null)
                timeline.stop();
            SoundManager.playClick();

            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/baccalaureatplus/app/menu.fxml"));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur : impossible de revenir au menu.");
            alert.show();
            e.printStackTrace();
        }
    }

    // ====================== UTILS ======================
    private void setFieldsDisabled(boolean disabled) {
        TextField[] fields = {
                fieldPrenom, fieldFruit, fieldLegume,
                fieldAnimal, fieldPays, fieldVille, fieldObjet
        };
        for (TextField f : fields)
            if (f != null)
                f.setDisable(disabled);
    }

    private void resetFieldStyles() {
        TextField[] fields = {
                fieldPrenom, fieldFruit, fieldLegume,
                fieldAnimal, fieldPays, fieldVille, fieldObjet
        };
        for (TextField f : fields) {
            if (f != null)
                f.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-font-size: 16;" +
                                "-fx-padding: 0;" +
                                "-fx-border-color: transparent;");
        }
    }

    private void clearFields() {
        TextField[] fields = {
                fieldPrenom, fieldFruit, fieldLegume,
                fieldAnimal, fieldPays, fieldVille, fieldObjet
        };
        for (TextField f : fields)
            if (f != null)
                f.clear();
    }
}
