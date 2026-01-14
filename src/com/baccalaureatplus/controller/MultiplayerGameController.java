package com.baccalaureatplus.controller;

import com.baccalaureatplus.network.GameClient;
import com.baccalaureatplus.service.MultiplayerService;
import com.baccalaureatplus.service.SyncService;
import com.baccalaureatplus.service.ValidationService;
import com.baccalaureatplus.util.SoundManager;
import com.baccalaureatplus.util.TextUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class MultiplayerGameController {

    private GameClient client;
    private Timeline timeline;
    private int tempsRestant = 120;
    private boolean isTimerRunning = false;

    @FXML
    private TextField nomField;
    @FXML
    private TextField nbJoueursField;
    @FXML
    private TextField codeSessionField;
    @FXML
    private Label statusLabel, lettreLabel, timerLabel, resultLabel;
    @FXML
    private Label codeSessionLabel;
    @FXML
    private HBox codeSessionBox;
    @FXML
    private Button copierCodeBtn;
    @FXML
    private Button rejoindreBtn;
    @FXML
    private Button creerPartieBtn;
    @FXML
    private Button demarrerBtn;

    @FXML
    private TextField prenomField, legumeField, fruitField, animalField, paysField, villeField, objetField;
    @FXML
    private Button validerBtn;
    @FXML
    private Button quitterBtn;

    // ================= SERVICE ET DONNÉES DE PARTIE =================

    private MultiplayerService multiplayerService = new MultiplayerService();
    private SyncService syncService = new SyncService();
    private int idJoueur = -1;
    private int tempsEcoule = 0;
    private String lettrePartie = "";
    private String codeSession = ""; // Code unique de la session
    private boolean estHote = false; // Indiquer si c'est l'hôte

    // Liste des joueurs (max 8)
    private List<String> listeJoueurs = new ArrayList<>();
    private int nombreJoueursMax = 8;
    private int nombreJoueursAttendu = 0;

    // Scores de chaque joueur
    private Map<String, Integer> scoresJoueurs = new HashMap<>();

    // 🔗 MAPPING DES NOMS DE JOUEURS À LEURS IDS (pour récupérer les scores de la
    // BD)
    private Map<String, Integer> joueurIDs = new HashMap<>();

    // ================= GESTION DES TOURS =================
    private List<String> listeJoueursOrdre = new ArrayList<>(); // Ordre des joueurs
    private int indexJoueurActuel = 0; // Index du joueur actuel
    private Map<String, Map<String, String>> motsJoueurs = new HashMap<>(); // Mots de chaque joueur
    private boolean enjeuTours = false; // Flag pour savoir si on est en mode tour par tour

    @FXML
    public void initialize() {
        cacherCategories();
        validerBtn.setDisable(true);
        statusLabel.setText("🎮 Mode Multijoueur");
    }

    public void configurerModeMultiplayer() {
        cacherCategories();
        statusLabel.setText("🎮 Mode Multijoueur");
    }

    /**
     * Initialiser une nouvelle partie multijoueur
     * Réinitialise toutes les variables et prépare l'interface
     */
    private void initialiserPartie(String lettreCommune, int nbJoueurs) {
        // Vérifier le nom du joueur
        if (nomField == null || nomField.getText().trim().isEmpty()) {
            statusLabel.setText("⚠️ Veuillez saisir votre nom !");
            return;
        }

        String nomJoueur = nomField.getText().trim();

        // Créer le joueur dans la base de données
        idJoueur = multiplayerService.creerJoueurMultijoueur(nomJoueur, nbJoueurs);

        if (idJoueur == -1) {
            statusLabel.setText("❌ Erreur de connexion à la base de données !");
            return;
        }

        System.out.println("✅ Joueur multijoueur créé: " + nomJoueur + " (ID: " + idJoueur + ")");

        // Initialiser la partie avec la lettre commune
        this.lettrePartie = lettreCommune;
        multiplayerService.initialiserPartie(lettreCommune);

        // Réinitialiser les variables
        tempsEcoule = 0;
        tempsRestant = 120;
        listeJoueurs.clear();
        scoresJoueurs.clear();
        joueurIDs.clear();
        listeJoueursOrdre.clear();
        motsJoueurs.clear();
        indexJoueurActuel = 0;
        enjeuTours = true;
        nombreJoueursAttendu = nbJoueurs;

        // Ajouter le joueur actuel à la liste
        listeJoueurs.add(nomJoueur);
        scoresJoueurs.put(nomJoueur, 0);
        joueurIDs.put(nomJoueur, idJoueur); // 🔗 MAPPER LE NOM À L'ID
        listeJoueursOrdre.add(nomJoueur);
        motsJoueurs.put(nomJoueur, new HashMap<>());

        // Afficher la lettre
        lettreLabel.setText("Lettre : " + lettrePartie);

        // Préparer l'interface
        nomField.setDisable(true);
        nbJoueursField.setDisable(true);
        afficherCategories();
        viderChamps();
        validerBtn.setDisable(false);

        // Démarrer le tour du joueur
        demarrerTourJoueur();

        System.out.println("🎮 Partie initialisée : " + nbJoueurs + " joueurs, lettre " + lettrePartie);
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
                valider();
            }
        }));

        timeline.setCycleCount(120);
        timeline.play();
    }

    // ====================== GESTION DES TOURS ======================

    /**
     * Démarrer le tour d'un joueur avec chronomètre de 120 secondes
     */
    private void demarrerTourJoueur() {
        if (indexJoueurActuel >= listeJoueursOrdre.size()) {
            // Tous les joueurs locaux ont joué
            // MAIS on doit vérifier si TOUS les joueurs attendus ont joué en BD

            // 📊 Vérifier combien de joueurs ont joué pour cette session en BD
            List<Map<String, Object>> scoresBD = syncService.getScoresByCodeSession(codeSession);
            int nombreJoueursQuiOntJoue = (scoresBD != null) ? scoresBD.size() : 0;

            System.out.println("📊 Joueurs qui ont joué : " + nombreJoueursQuiOntJoue + "/" + nombreJoueursAttendu);

            // Attendre que TOUS les joueurs attendus aient joué
            if (nombreJoueursQuiOntJoue >= nombreJoueursAttendu) {
                // ✅ TOUS les joueurs ont joué - afficher les résultats
                afficherResultats();
            } else {
                // ⏳ En attente des autres joueurs
                statusLabel.setText("⏳ En attente des autres joueurs... (" + nombreJoueursQuiOntJoue + "/"
                        + nombreJoueursAttendu + ")");
                System.out
                        .println("⏳ En attente de " + (nombreJoueursAttendu - nombreJoueursQuiOntJoue) + " joueur(s)");

                // Relancer une vérification après 2 secondes
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        Platform.runLater(this::demarrerTourJoueur);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            return;
        }

        String joueurActuel = listeJoueursOrdre.get(indexJoueurActuel);
        // 👥 AFFICHER LE NOMBRE RÉEL DE JOUEURS, PAS LE NOMBRE ATTENDU
        int joueurActuelNum = indexJoueurActuel + 1;
        int totalJoueurs = listeJoueursOrdre.size();
        statusLabel.setText(
                "🎮 Tour de " + joueurActuel + " (" + joueurActuelNum + "/" + totalJoueurs + ")");

        viderChamps();
        resetFieldStyles();
        validerBtn.setDisable(false);

        // Lancer un chronomètre de 120 secondes pour ce joueur
        lancerTimerJoueur();
    }

    /**
     * Lancer le chronomètre pour un joueur (120 secondes, comme le solo)
     */
    private void lancerTimerJoueur() {
        if (timeline != null)
            timeline.stop();

        tempsRestant = 120;
        tempsEcoule = 0;
        timerLabel.setStyle("-fx-text-fill: #F86DA3; -fx-font-weight: 900;");

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tempsRestant--;
            timerLabel.setText(tempsRestant + "s");

            if (tempsRestant <= 10) {
                timerLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
            if (tempsRestant <= 0) {
                timeline.stop();
                // ⏰ AFFICHER LE MESSAGE "TEMPS ÉCOULÉ"
                Platform.runLater(() -> {
                    statusLabel.setText("⏰ Temps écoulé ! Score : 0 pts");
                    timerLabel.setText("⏰ TEMPS ÉCOULÉ");
                    resultLabel.setText("📊 Score automatique : 0 pts");
                    valider();
                });
            }
        }));

        timeline.setCycleCount(120);
        timeline.play();
    }

    /**
     * Valider les mots du joueur actuel (avec la logique du solo)
     */
    @FXML
    public void valider() {
        if (!enjeuTours)
            return;

        if (timeline != null) {
            timeline.stop();
        }

        String joueurActuel = listeJoueursOrdre.get(indexJoueurActuel);
        setFieldsDisabled(true);
        validerBtn.setDisable(true);
        statusLabel.setText("Vérification des mots...");

        TextField[] fields = {
                prenomField, fruitField, legumeField,
                animalField, paysField, villeField, objetField
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
                // ⚠️ VÉRIFIER D'ABORD SI LE MOT COMMENCE PAR LA BONNE LETTRE
                char letterRequired = lettrePartie.charAt(0);
                char firstLetter = Character.toUpperCase(mot.charAt(0));

                if (firstLetter != letterRequired) {
                    // ❌ Le mot ne commence pas par la bonne lettre
                    System.out.println(
                            "❌ Mot refusé (mauvaise lettre): " + mot + " (doit commencer par " + letterRequired + ")");
                    animerResultat(f, false);
                    futures.add(CompletableFuture.completedFuture(false));
                } else {
                    // ✅ LA LETTRE EST BONNE → maintenant vérifier le cache
                    // 1️⃣ VÉRIFIER D'ABORD DANS LE CACHE (mots_valides)
                    boolean existeDansCache = syncService.motEstDansCache(mot, categorie);

                    if (existeDansCache) {
                        // ✅ Mot trouvé dans le cache → validation instantanée
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

                            // 3️⃣ Si validé par l'API → ajouter au cache
                            if (isValid) {
                                syncService.cacherMotValide(motFinal, categorieFinal);
                                System.out.println("💾 Mot ajouté au cache: " + motFinal + " (" + categorieFinal + ")");
                            }
                        });

                        futures.add(validationFuture);
                    }
                }
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    int score = futures.stream().mapToInt(f -> f.join() ? 10 : 0).sum();
                    scoresJoueurs.put(joueurActuel, score);

                    // 💾 ENREGISTRER LE SCORE DANS LA BASE DE DONNÉES AVEC LE CODE DE SESSION
                    // Utiliser l'ID du joueur courant stocké dans joueurIDs
                    int idJoueurCourant = joueurIDs.getOrDefault(joueurActuel, idJoueur);
                    syncService.enregistrerScoreMultijoueur(idJoueurCourant, score, lettrePartie.charAt(0), 0,
                            codeSession);

                    Platform.runLater(() -> {
                        // 📊 AFFICHER LE SCORE DU JOUEUR QUI VIENT DE TERMINER
                        statusLabel.setText("✅ " + joueurActuel + " a terminé ! Score : " + score + " pts");
                        resultLabel.setText("📊 " + joueurActuel + " : " + score + " pts");

                        // 🔒 DÉSACTIVER LE BOUTON "REJOINDRE" ET LE CHAMP DE CODE
                        // Pour empêcher les joueurs de rejoindre une nouvelle session une fois la
                        // partie lancée
                        if (codeSessionField != null) {
                            codeSessionField.setDisable(true);
                        }
                        if (rejoindreBtn != null) {
                            rejoindreBtn.setDisable(true);
                        }
                        if (creerPartieBtn != null) {
                            creerPartieBtn.setDisable(true);
                        }

                        // Passer au joueur suivant après 2 secondes
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000);
                                Platform.runLater(() -> {
                                    indexJoueurActuel++;
                                    setFieldsDisabled(false);
                                    demarrerTourJoueur();
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    });
                });
    }

    /**
     * Animation du résultat d'un mot (vert ou rouge)
     */
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

    /**
     * Afficher les résultats finaux avec classement
     */
    private void afficherResultats() {
        enjeuTours = false;
        cacherCategories();

        // 🎯 APPELER LE SERVICE POUR CALCULER LE GAGNANT
        // Le service récupère TOUS les scores de la session en base de données
        // et retourne le classement avec le gagnant
        Map<String, Object> resultatSession = syncService.calculerGagnantSession(codeSession);

        if (resultatSession == null) {
            resultLabel.setText("❌ Erreur : aucun score trouvé pour cette session");
            statusLabel.setText("⚠️ La session n'a pas de scores enregistrés");
            return;
        }

        // Récupérer les résultats calculés par le service
        String gagnant = (String) resultatSession.get("gagnant");
        int scoreGagnant = (int) resultatSession.get("scoreGagnant");
        List<Map<String, Object>> scores = (List<Map<String, Object>>) resultatSession.get("scores");

        // 🏆 CADRE DU HAUT - Message du gagnant avec fond rose clair et bordure rose
        // (comme l'image)
        String messageGagnant = "🏆 Partie terminée ! Gagnant : " + gagnant + " (" + scoreGagnant + " pts)";
        resultLabel.setText(messageGagnant);
        resultLabel.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 20px; -fx-background-color: #FFF0F5; -fx-border-color: #FFB6C1; -fx-border-width: 3; -fx-border-radius: 15; -fx-background-radius: 15;");

        // 📊 CADRE DU BAS - Scores des joueurs avec fond blanc (comme l'image)
        StringBuilder messageResultats = new StringBuilder();
        messageResultats.append("🏆 Partie terminée ! Gagnant : ").append(gagnant).append(" (").append(scoreGagnant)
                .append(" pts)\n\n");
        messageResultats.append("📊 Scores des joueurs :\n");

        // Ajouter les scores de tous les joueurs
        for (int i = 0; i < scores.size(); i++) {
            Map<String, Object> scoreData = scores.get(i);
            String prenom = (String) scoreData.get("prenom");
            String nom = (String) scoreData.get("nom");

            // Construire le nom complet : prénom + nom (utiliser celui qui n'est pas null)
            String nomComplet;
            if (prenom != null && !prenom.trim().isEmpty()) {
                nomComplet = prenom;
            } else if (nom != null && !nom.trim().isEmpty()) {
                nomComplet = nom;
            } else {
                nomComplet = "Joueur";
            }

            int score = ((Number) scoreData.get("score")).intValue();

            if (i == 0) {
                messageResultats.append("🥇 ").append(nomComplet).append(" : ").append(score).append(" pts\n");
            } else if (i == 1) {
                messageResultats.append("🥈 ").append(nomComplet).append(" : ").append(score).append(" pts\n");
            } else if (i == 2) {
                messageResultats.append("🥉 ").append(nomComplet).append(" : ").append(score).append(" pts\n");
            } else {
                messageResultats.append("   ").append(nomComplet).append(" : ").append(score).append(" pts\n");
            }
        }

        statusLabel.setText(messageResultats.toString());
        statusLabel.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 20px; -fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");

        System.out.println("✅ Session " + codeSession + " terminée. Gagnant : " + gagnant);
        System.out.println("📊 Nombre de joueurs : " + scores.size());
    }

    private void setFieldsDisabled(boolean disabled) {
        TextField[] fields = {
                prenomField, fruitField, legumeField,
                animalField, paysField, villeField, objetField
        };
        for (TextField f : fields)
            if (f != null)
                f.setDisable(disabled);
    }

    /**
     * Vider tous les champs de texte
     */
    private void viderChamps() {
        prenomField.clear();
        legumeField.clear();
        fruitField.clear();
        animalField.clear();
        paysField.clear();
        villeField.clear();
        objetField.clear();
    }

    /**
     * Réinitialiser les styles des champs
     */
    private void resetFieldStyles() {
        prenomField.setStyle("");
        legumeField.setStyle("");
        fruitField.setStyle("");
        animalField.setStyle("");
        paysField.setStyle("");
        villeField.setStyle("");
        objetField.setStyle("");
    }

    private void cacherCategories() {
        setCategoriesVisible(false);
    }

    private void afficherCategories() {
        setCategoriesVisible(true);
    }

    private void setCategoriesVisible(boolean visible) {
        prenomField.setDisable(!visible);
        legumeField.setDisable(!visible);
        fruitField.setDisable(!visible);
        animalField.setDisable(!visible);
        paysField.setDisable(!visible);
        villeField.setDisable(!visible);
        objetField.setDisable(!visible);
        if (validerBtn != null)
            validerBtn.setVisible(visible);
    }

    // ================= BOUTONS =================

    /**
     * Générer un code unique de session (6 caractères alphanumériques)
     */
    private String genererCodeSession() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    @FXML
    public void creerPartie() {
        if (nomField.getText().isEmpty() || nbJoueursField.getText().isEmpty()) {
            statusLabel.setText("⚠️ Remplissez tous les champs");
            return;
        }

        // Valider le nombre de joueurs
        int nbJoueurs;
        try {
            nbJoueurs = Integer.parseInt(nbJoueursField.getText());
            if (nbJoueurs < 2 || nbJoueurs > 8) {
                statusLabel.setText("⚠️ Nombre de joueurs : 2-8");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("⚠️ Nombre invalide");
            return;
        }

        // Créer le joueur hôte dans la base de données
        String nomHote = nomField.getText().trim();
        idJoueur = multiplayerService.creerJoueurMultijoueur(nomHote, nbJoueurs);

        if (idJoueur == -1) {
            statusLabel.setText("❌ Erreur de connexion à la base de données !");
            return;
        }

        // Générer le code de session
        codeSession = genererCodeSession();

        // Générer la lettre aléatoire
        String lettreAleatoire = genererLettreAleatoire();
        this.lettrePartie = lettreAleatoire;

        // Enregistrer la session dans la base de données
        SyncService syncService = new SyncService();
        int sessionId = syncService.creerSession(codeSession, nbJoueurs, idJoueur, lettreAleatoire);

        if (sessionId == -1) {
            statusLabel.setText("❌ Erreur lors de la création de la session !");
            return;
        }

        // Afficher le code
        if (codeSessionLabel != null && codeSessionBox != null) {
            codeSessionLabel.setText("🆔 CODE : " + codeSession);
            codeSessionBox.setVisible(true);
        }

        // Afficher le bouton Démarrer pour l'hôte
        if (demarrerBtn != null) {
            demarrerBtn.setVisible(true);
        }
        estHote = true;

        statusLabel.setText("✅ Partie créée ! Partagez le code : " + codeSession);
        System.out.println("🎮 Session enregistrée : ID=" + sessionId + ", Code=" + codeSession + ", Host=" + nomHote);

        // Désactiver les boutons de création
        nomField.setDisable(true);
        nbJoueursField.setDisable(true);

        // Désactiver la section "Rejoindre une partie existante"
        rejoindreBtn.setDisable(true);
        codeSessionField.setDisable(true);

        // 🔐 NE PAS initialiser la partie encore - attendre que le host confirme le
        // code
        // Initialiser seulement le minimum (joueur, lettre, session)
        this.lettrePartie = lettreAleatoire;
        this.nombreJoueursAttendu = nbJoueurs;

        // NE PAS ajouter le host à la liste - il sera ajouté quand il clique "Copier"
        // et que demarrerPartieHote() est appelée

        // Montrer la lettre mais pas les catégories
        lettreLabel.setText("Lettre : " + lettrePartie);
        statusLabel.setText("✅ Code généré ! Cliquez sur 'Copier' et attendez les autres joueurs...");

        // Les catégories resteront masquées jusqu'à ce que le host/joueur arrive prêt
    }

    /**
     * Bouton Démarrer - Appelé par l'hôte pour démarrer la partie
     */
    @FXML
    public void demarrerPartie() {
        if (!estHote) {
            statusLabel.setText("⚠️ Seul l'hôte peut démarrer la partie !");
            return;
        }
        demarrerPartieHote();
    }

    /**
     * Démarrer la partie pour le host (appelé quand il est prêt)
     */
    private void demarrerPartieHote() {
        // Désactiver le bouton Démarrer après le clic
        if (demarrerBtn != null) {
            demarrerBtn.setDisable(true);
        }

        // Désactiver le bouton "Créer Partie" quand la session démarre
        if (creerPartieBtn != null) {
            creerPartieBtn.setDisable(true);
        }

        // 👤 Ajouter le host à la liste des joueurs quand il démarre la partie
        String nomHote = nomField.getText().trim();
        if (!listeJoueursOrdre.contains(nomHote)) {
            listeJoueurs.add(nomHote);
            scoresJoueurs.put(nomHote, 0);
            joueurIDs.put(nomHote, idJoueur);
            listeJoueursOrdre.add(nomHote);
            motsJoueurs.put(nomHote, new HashMap<>());
            System.out.println("👤 Host ajouté à la liste : " + nomHote);
        }

        // Afficher les catégories et démarrer les tours
        afficherCategories();
        viderChamps();
        validerBtn.setDisable(false);
        enjeuTours = true;
        indexJoueurActuel = 0;

        // Démarrer le premier tour
        demarrerTourJoueur();

        System.out.println("🎮 Partie démarrée pour le host !");
    }

    @FXML
    public void rejoindrePartie() {
        if (nomField.getText().isEmpty()) {
            statusLabel.setText("⚠️ Entrez votre nom");
            return;
        }

        if (codeSessionField == null || codeSessionField.getText().isEmpty()) {
            statusLabel.setText("⚠️ Entrez le code de la session");
            return;
        }

        String codeARejoindre = codeSessionField.getText().toUpperCase().trim();

        // Vérifier le format du code (6 caractères)
        if (codeARejoindre.length() != 6) {
            statusLabel.setText("⚠️ Code invalide (6 caractères)");
            return;
        }

        statusLabel.setText("🔍 Connexion à la session " + codeARejoindre + "...");
        System.out.println("🔗 Tentative de rejoindre la session : " + codeARejoindre);

        // Chercher la session dans la base de données
        Map<String, Object> sessionInfo = syncService.getSessionByCode(codeARejoindre);

        if (sessionInfo == null || sessionInfo.isEmpty()) {
            statusLabel.setText("❌ Session introuvable. Vérifiez le code.");
            System.out.println("❌ Session non trouvée : " + codeARejoindre);
            return;
        }

        // Récupérer les infos de la session
        int sessionId = (int) sessionInfo.get("id");
        String lettreSession = (String) sessionInfo.get("lettre");
        int nbJoueursAttendu = (int) sessionInfo.get("nb_joueurs");

        System.out.println(
                "✅ Session trouvée ! ID=" + sessionId + ", Lettre=" + lettreSession + ", Joueurs=" + nbJoueursAttendu);

        // Créer le joueur dans la base de données
        String nomJoueur = nomField.getText().trim();
        idJoueur = multiplayerService.creerJoueurMultijoueur(nomJoueur, nbJoueursAttendu);

        if (idJoueur == -1) {
            statusLabel.setText("❌ Erreur de création du joueur !");
            return;
        }

        System.out.println("✅ Joueur créé : " + nomJoueur + " (ID=" + idJoueur + ")");

        // Initialiser la partie avec la lettre et le nombre de joueurs
        codeSession = codeARejoindre;
        this.lettrePartie = lettreSession;
        this.nombreJoueursAttendu = nbJoueursAttendu;

        // Ajouter le joueur à la liste
        listeJoueurs.add(nomJoueur);
        scoresJoueurs.put(nomJoueur, 0);
        joueurIDs.put(nomJoueur, idJoueur); // 🔗 MAPPER LE NOM À L'ID
        listeJoueursOrdre.add(nomJoueur);
        motsJoueurs.put(nomJoueur, new HashMap<>());

        // 🔄 CHARGER LES SCORES DES AUTRES JOUEURS QUI ONT DÉJÀ JOUÉ
        // Cela permet à ce joueur de voir les résultats finaux avec tous les autres
        // scores
        System.out.println("📊 Chargement des scores des autres joueurs...");
        for (String autreJoueur : listeJoueurs) {
            if (!autreJoueur.equals(nomJoueur)) {
                // Chercher le joueur dans la DB et récupérer son score
                // TODO: Si besoin, implémenter une méthode pour récupérer les scores par
                // session
            }
        }

        // Désactiver le champ de nom
        nomField.setDisable(true);
        codeSessionField.setDisable(true);

        // Afficher la lettre
        lettreLabel.setText("Lettre : " + lettrePartie);

        statusLabel.setText("✅ Connecté à la session !");

        // Désactiver le bouton "Créer Partie" quand on rejoint une partie existante
        creerPartieBtn.setDisable(true);
        nbJoueursField.setDisable(true);

        // Désactiver le bouton "Rejoindre" quand la session démarre
        rejoindreBtn.setDisable(true);

        // 🎮 DÉMARRER LA PARTIE QUAND QUELQU'UN REJOINT AVEC UN CODE VALIDE
        demarrerPartieHote();
    }

    /**
     * Copier le code de session dans le presse-papiers
     */
    @FXML
    public void copierCode() {
        if (codeSession != null && !codeSession.isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(codeSession);
            clipboard.setContent(content);

            statusLabel.setText("✅ Code copié : " + codeSession);
            System.out.println("📋 Code copié dans le presse-papiers : " + codeSession);

            // Animation du bouton
            if (copierCodeBtn != null) {
                String originalText = copierCodeBtn.getText();
                copierCodeBtn.setText("✅ Copié !");

                // Restaurer le texte après 2 secondes
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        Platform.runLater(() -> {
                            copierCodeBtn.setText(originalText);
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    /**
     * Générer une lettre aléatoire pour la partie
     */
    private String genererLettreAleatoire() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        return String.valueOf(alphabet.charAt(random.nextInt(alphabet.length())));
    }

    private void lancerConnexion(String commande) {
        client = new GameClient();
        String serverAddress = "localhost"; // Remplacez par l'adresse du serveur si besoin
        if (!client.connect(serverAddress)) {
            statusLabel.setText("❌ Serveur hors ligne !");
            return;
        }
        client.send(commande);
        ecouterServeur();
    }

    // ================= COMMUNICATION =================

    private void ecouterServeur() {
        new Thread(() -> {
            try {
                String msg;
                while ((msg = client.getIn().readLine()) != null) {
                    traiterMessage(msg);
                }
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("❌ Déconnexion du serveur"));
            }
        }).start();
    }

    private void traiterMessage(String msg) {
        String[] p = msg.split("\\|");

        Platform.runLater(() -> {
            switch (p[0]) {
                case "WAIT":
                    statusLabel.setText("⏳ En attente : " + p[1]);
                    break;

                case "START":
                    lettreLabel.setText("Lettre : " + p[1]);
                    afficherCategories();
                    demarrerTimer();
                    statusLabel.setText("🚀 C'est parti !");
                    break;

                case "RESULTS": // Changé de "RESULT" à "RESULTS" pour matcher le serveur
                    afficherPodium(p[1]);
                    break;
            }
        });
    }

    private void afficherPodium(String rawData) {
        // Format reçu : RESULTS|Nom1:Score1;Nom2:Score2;
        StringBuilder sb = new StringBuilder("🏆 CLASSEMENT\n");
        String[] scores = rawData.split(";");
        for (int i = 0; i < scores.length; i++) {
            sb.append(i + 1).append(". ").append(scores[i].replace(":", " -> ")).append(" pts\n");
        }
        resultLabel.setText(sb.toString());
        cacherCategories();
    }

    // ================= TIMER =================

    private void demarrerTimer() {
        if (isTimerRunning)
            return;
        isTimerRunning = true;
        tempsRestant = 60;

        new Thread(() -> {
            try {
                while (tempsRestant >= 0 && isTimerRunning) {
                    int t = tempsRestant--;
                    Platform.runLater(() -> timerLabel.setText("⏱ " + t + " s"));
                    if (t == 0)
                        Platform.runLater(this::valider);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                isTimerRunning = false;
            }
        }).start();
    }

    /**
     * Quitter le jeu et retourner au menu principal
     */
    @FXML
    private void quitterJeu() {
        try {
            // 🚪 CHARGER LE MENU PRINCIPAL
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/baccalaureatplus/app/menu.fxml"));

            // 📺 REMPLACER LA SCÈNE ACTUELLE PAR LE MENU
            Stage stage = (Stage) quitterBtn.getScene().getWindow();
            stage.getScene().setRoot(root);

            System.out.println("✅ Retour au menu principal");

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du retour au menu");
            e.printStackTrace();
        }
    }

}
