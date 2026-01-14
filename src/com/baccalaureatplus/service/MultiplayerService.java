package com.baccalaureatplus.service;

import com.baccalaureatplus.util.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service de gestion de la logique métier du mode multijoueur
 * Gère la lettre commune, le temps, la validation des mots et le calcul des
 * scores
 */
public class MultiplayerService {

    private SyncService syncService;
    // private ValidationService validationService;

    private String lettreCommune;
  
    private int tempsEcoule = 0;
    private int idJoueur = -1;

    // Constantes
    private static final int MAX_JOUEURS = 8;

    // Catégories dans l'ordre
    private static final String[] CATEGORIES = {
            "Prénom", "Légume", "Fruit", "Animal", "Pays", "Ville", "Objet"
    };

    public MultiplayerService() {
        this.syncService = new SyncService();
        // this.validationService = new ValidationService();
    }

    // ========== GESTION DES JOUEURS ==========

    /**
     * Créer un joueur multijoueur dans la base de données
     */
    public int creerJoueurMultijoueur(String nomJoueur, int nombreJoueurs) {
        // Validation du nombre de joueurs
        if (nombreJoueurs < 2 || nombreJoueurs > MAX_JOUEURS) {
            System.err
                    .println("❌ Nombre de joueurs invalide: " + nombreJoueurs + " (min: 2, max: " + MAX_JOUEURS + ")");
            return -1;
        }

        this.idJoueur = syncService.creerJoueurMultijoueur(nomJoueur, nombreJoueurs);
        System.out.println("✅ Joueur multijoueur créé: " + nomJoueur + " (ID: " + idJoueur + ")");
        return this.idJoueur;
    }

    /**
     * Obtenir l'ID du joueur actuel
     */
    public int getIdJoueur() {
        return this.idJoueur;
    }

    /**
     * Obtenir le nombre maximum de joueurs autorisés
     */
    public static int getMaxJoueurs() {
        return MAX_JOUEURS;
    }

    // ========== GESTION DE LA PARTIE ==========

    /**
     * Définir la lettre commune pour la partie
     */
    public void setLettreCommune(String lettre) {
        this.lettreCommune = lettre.toUpperCase();
        System.out.println("🎲 Lettre de la partie: " + this.lettreCommune);
    }

    /**
     * Obtenir la lettre commune
     */
    public String getLettreCommune() {
        return this.lettreCommune;
    }

    /**
     * Définir le temps écoulé pendant la partie
     */
    public void setTempsEcoule(int secondes) {
        this.tempsEcoule = secondes;
    }

    /**
     * Obtenir le temps écoulé
     */
    public int getTempsEcoule() {
        return this.tempsEcoule;
    }

    /**
     * Initialiser une nouvelle partie
     */
    public void initialiserPartie(String lettre) {
        this.lettreCommune = lettre.toUpperCase();
        this.tempsEcoule = 0;
        System.out.println("🎮 Partie multijoueur initialisée avec la lettre: " + lettre);
    }

    // ========== VALIDATION DES MOTS ==========

    /**
     * Valider et calculer le score d'un joueur
     * 
     * @param motsRaw Chaîne avec les 7 mots séparés par ";"
     * @return CompletableFuture avec le score final
     */
    public CompletableFuture<ResultatPartie> validerMots(String motsRaw) {
        return CompletableFuture.supplyAsync(() -> {
            String[] mots = motsRaw.split(";");

            if (mots.length != 7) {
                System.err.println("❌ Nombre de mots incorrect: " + mots.length);
                return new ResultatPartie(0, new ArrayList<>());
            }

            List<CompletableFuture<Boolean>> validations = new ArrayList<>();
            List<String> motsNormalises = new ArrayList<>();

            // Normaliser les mots
            for (String mot : mots) {
                motsNormalises.add(TextUtils.normaliser(mot));
            }

            // Valider chaque mot
            for (int i = 0; i < motsNormalises.size(); i++) {
                String mot = motsNormalises.get(i);
                String categorie = CATEGORIES[i];

                if (mot.isEmpty()) {
                    validations.add(CompletableFuture.completedFuture(false));
                } else {
                    // Vérifier d'abord dans le cache
                    boolean dansCache = syncService.motEstDansCache(mot, categorie);

                    if (dansCache) {
                        System.out.println("✅ Cache: " + mot + " (" + categorie + ")");
                        validations.add(CompletableFuture.completedFuture(true));
                    } else {
                        // Appel API si pas dans le cache
                        System.out.println("🌐 API: " + mot + " (" + categorie + ")");
                        CompletableFuture<Boolean> validation = ValidationService.validerMotAvecCategorie(
                                mot, lettreCommune, categorie);

                        // Mettre en cache si valide
                        final String motFinal = mot;
                        final String categorieFinal = categorie;
                        validation.thenAccept(isValid -> {
                            if (isValid) {
                                syncService.cacherMotValide(motFinal, categorieFinal);
                            }
                        });

                        validations.add(validation);
                    }
                }
            }

            // Attendre toutes les validations
            CompletableFuture.allOf(validations.toArray(new CompletableFuture[0])).join();

            // Calculer le score
            int score = 0;
            List<Boolean> resultats = new ArrayList<>();

            for (CompletableFuture<Boolean> validation : validations) {
                try {
                    boolean valide = validation.get();
                    resultats.add(valide);
                    if (valide) {
                        score += 10;
                    }
                } catch (Exception e) {
                    resultats.add(false);
                }
            }

            System.out.println("🎯 Score calculé: " + score + "/70");
            return new ResultatPartie(score, resultats);
        });
    }

    /**
     * Valider les mots de plusieurs joueurs
     * 
     * @param joueursData Map avec nom du joueur -> mots bruts
     * @return Map avec nom du joueur -> score
     */
    public CompletableFuture<Map<String, Integer>> validerTousLesJoueurs(Map<String, String> joueursData) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Integer> scores = new HashMap<>();

            // Valider les mots de chaque joueur
            List<CompletableFuture<Void>> validationsFutures = new ArrayList<>();

            for (Map.Entry<String, String> entry : joueursData.entrySet()) {
                String nomJoueur = entry.getKey();
                String motsRaw = entry.getValue();

                CompletableFuture<Void> future = validerMots(motsRaw).thenAccept(resultat -> {
                    synchronized (scores) {
                        scores.put(nomJoueur, resultat.getScore());
                    }
                });

                validationsFutures.add(future);
            }

            // Attendre toutes les validations
            CompletableFuture.allOf(validationsFutures.toArray(new CompletableFuture[0])).join();

            return scores;
        });
    }

    // ========== ENREGISTREMENT DU SCORE ==========

    /**
     * Enregistrer le score final dans la base de données
     */
    public int enregistrerScore(int score) {
        if (idJoueur == -1) {
            System.err.println("❌ Aucun joueur actif");
            return -1;
        }

        if (lettreCommune == null || lettreCommune.isEmpty()) {
            System.err.println("❌ Aucune lettre définie");
            return -1;
        }

        int scoreId = syncService.enregistrerScore(
                idJoueur,
                score,
                lettreCommune.charAt(0),
                tempsEcoule);

        if (scoreId != -1) {
            System.out.println("✅ Score multijoueur enregistré: " + score + " pts (ID: " + scoreId + ")");
        }

        return scoreId;
    }

    // ========== CLASSE INTERNE POUR LES RÉSULTATS ==========

    /**
     * Classe pour encapsuler le résultat d'une validation
     */
    public static class ResultatPartie {
        private int score;
        private List<Boolean> resultats;

        public ResultatPartie(int score, List<Boolean> resultats) {
            this.score = score;
            this.resultats = resultats;
        }

        public int getScore() {
            return score;
        }

        public List<Boolean> getResultats() {
            return resultats;
        }
    }
}
