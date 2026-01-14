package com.baccalaureatplus.service;

import com.baccalaureatplus.dao.*;
import java.util.List;

public class SyncService {
    private JoueurDAO joueurDAO;
    private ScoreDAO scoreDAO;
    private CategorieDAO categorieDAO;
    private MotValideDAO motValideDAO;
    private SessionDAO sessionDAO;

    public SyncService() {
        this.joueurDAO = new JoueurDAO();
        this.scoreDAO = new ScoreDAO();
        this.categorieDAO = new CategorieDAO();
        this.motValideDAO = new MotValideDAO();
        this.sessionDAO = new SessionDAO();
    }

    // ========== GESTION DES JOUEURS ==========

    /**
     * Créer un joueur en mode SOLO
     * 
     * @return L'ID du joueur créé
     */
    public int creerJoueurSolo(String prenom) {
        return joueurDAO.creerJoueurSolo(prenom);
    }

    /**
     * Créer un joueur en mode MULTIJOUEUR
     * 
     * @return L'ID du joueur créé
     */
    public int creerJoueurMultijoueur(String nomEquipe, int nombreJoueurs) {
        return joueurDAO.creerJoueurMulti(nomEquipe, nombreJoueurs);
    }

    /**
     * Récupérer un joueur par son ID
     */
    public String getJoueur(int id) {
        return joueurDAO.getJoueur(id);
    }

    /**
     * Récupérer tous les joueurs SOLO
     */
    public List<String> getTousLesJoueursSolo() {
        return joueurDAO.getTousLesJoueursSolo();
    }

    /**
     * Supprimer un joueur
     */
    public boolean supprimerJoueur(int id) {
        return joueurDAO.supprimerJoueur(id);
    }

    // ========== GESTION DES SCORES ==========

    /**
     * Enregistrer le score d'une partie
     */
    public int enregistrerScore(int idJoueur, int score, char lettre, int tempsSeconde) {
        return scoreDAO.enregistrerScore(idJoueur, score, lettre, tempsSeconde);
    }

    /**
     * Enregistrer un score en mode MULTIJOUEUR avec code de session
     * Permet de relier les scores à la session multijoueur
     */
    public int enregistrerScoreMultijoueur(int idJoueur, int score, char lettre, int tempsSeconde, String codeSession) {
        return scoreDAO.enregistrerScoreMultijoueur(idJoueur, score, lettre, tempsSeconde, codeSession);
    }

    /**
     * Récupérer l'historique des scores d'un joueur
     */
    public List<java.util.Map<String, Object>> getHistoriqueJoueur(int idJoueur) {
        return scoreDAO.getScoresJoueur(idJoueur);
    }

    /**
     * Récupérer le meilleur score d'un joueur
     */
    public int getMeilleurScore(int idJoueur) {
        return scoreDAO.getMeilleurScore(idJoueur);
    }

    /**
     * Récupérer le TOP des meilleurs scores (classement général)
     */
    public List<java.util.Map<String, Object>> getTopScores(int limite) {
        return scoreDAO.getTopScores(limite);
    }

    /**
     * Récupérer tous les scores d'une session multijoueur par code de session
     * Permet de charger les scores de TOUS les joueurs qui ont joué dans cette
     * session
     */
    public List<java.util.Map<String, Object>> getScoresByCodeSession(String codeSession) {
        return scoreDAO.getScoresByCodeSession(codeSession);
    }

    /**
     * Récupérer le nombre total de parties jouées
     */
    public int getNombreTotalParties() {
        return scoreDAO.getNombreTotalParties();
    }

    /**
     * Supprimer un score
     */
    public boolean supprimerScore(int id) {
        return scoreDAO.supprimerScore(id);
    }

    // ========== GESTION DES CATÉGORIES ==========

    /**
     * Récupérer toutes les catégories
     */
    public List<String> getToutesLesCategories() {
        return categorieDAO.getToutesLesCategories();
    }

    /**
     * Récupérer l'ID d'une catégorie par son nom
     */
    public int getIdCategorie(String nom) {
        return categorieDAO.getIdCategorie(nom);
    }

    /**
     * Ajouter une nouvelle catégorie
     */
    public boolean ajouterCategorie(String nom) {
        return categorieDAO.ajouterCategorie(nom);
    }

    /**
     * Vérifier si une catégorie existe
     */
    public boolean categorieExiste(String nom) {
        return categorieDAO.categorieExiste(nom);
    }

    // ========== GESTION DES MOTS VALIDÉS (CACHE) ==========

    /**
     * Ajouter un mot validé au cache
     */
    public boolean cacherMotValide(String mot, String categorie) {
        return motValideDAO.ajouterMotValide(mot, categorie);
    }

    /**
     * Vérifier si un mot existe dans le cache
     */
    public boolean motEstDansCache(String mot, String categorie) {
        return motValideDAO.motExiste(mot, categorie);
    }

    /**
     * Récupérer tous les mots d'une catégorie
     */
    public List<String> getMotsParCategorie(String categorie) {
        return motValideDAO.getMotsParCategorie(categorie);
    }

    /**
     * Supprimer un mot du cache
     */
    public boolean supprimerMotCache(String mot, String categorie) {
        return motValideDAO.supprimerMot(mot, categorie);
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Initialiser une nouvelle partie (créer joueur + préparer le jeu)
     * 
     * @return L'ID du joueur créé
     */
    public int initialiserPartie(String prenomJoueur) {
        return creerJoueurSolo(prenomJoueur);
    }

    /**
     * Finaliser une partie (enregistrer le score final)
     */
    public int finaliserPartie(int idJoueur, int score, char lettre, int tempsSeconde) {
        return enregistrerScore(idJoueur, score, lettre, tempsSeconde);
    }

    /**
     * Valider et cacher un mot (vérification + mise en cache)
     */
    public boolean validerEtCacherMot(String mot, String categorie) {
        // Ici vous pouvez ajouter une validation API si nécessaire
        // Pour l'instant, on met directement en cache
        return cacherMotValide(mot, categorie);
    }

    // ========== GESTION DES SESSIONS MULTIJOUEUR ==========

    /**
     * Créer une nouvelle session multijoueur
     * 
     * @param codeSession Code unique de la session
     * @param nbJoueur    Nombre de joueurs attendus
     * @param hostId      ID du joueur hôte
     * @param lettre      Lettre aléatoire pour la partie
     * @return ID de la session créée
     */
    public int creerSession(String codeSession, int nbJoueur, int hostId, String lettre) {
        return sessionDAO.creerSession(codeSession, nbJoueur, hostId, lettre);
    }

    /**
     * Récupérer les informations complètes d'une session par code
     * 
     * @param codeSession Code de la session
     * @return Map avec [id, nb_joueurs, lettre, host_id] ou null si pas trouvée
     */
    public java.util.Map<String, Object> getSessionByCode(String codeSession) {
        return sessionDAO.getSessionByCode(codeSession);
    }

    /**
     * Vérifier si un code de session existe
     * 
     * @param codeSession Code à vérifier
     * @return true si la session existe
     */
    public boolean sessionExiste(String codeSession) {
        return sessionDAO.sessionExiste(codeSession);
    }

    /**
     * Récupérer les informations d'une session
     * 
     * @param codeSession Code de la session
     * @return Array [id, nbJoueur, hostId] ou null
     */
    public int[] getInfosSession(String codeSession) {
        return sessionDAO.getInfosSession(codeSession);
    }

    /**
     * Supprimer une session
     * 
     * @param codeSession Code de la session
     * @return true si suppression réussie
     */
    public boolean supprimerSession(String codeSession) {
        return sessionDAO.supprimerSession(codeSession);
    }

    // ========== GESTION DE FIN DE PARTIE MULTIJOUEUR ==========

    /**
     * Calculer le gagnant d'une session multijoueur (LOGIQUE MÉTIER DANS LE
     * SERVICE)
     * Récupère TOUS les scores de la session et les compare pour déterminer le
     * gagnant
     * 
     * @param codeSession Code unique de la session
     * @return Map avec [gagnant, scoreGagnant, classement, scores] ou null si pas
     *         de scores
     */
    public java.util.Map<String, Object> calculerGagnantSession(String codeSession) {
        java.util.Map<String, Object> resultat = new java.util.HashMap<>();

        // 📊 COUCHE DAO : Récupérer TOUS les scores de la session
        List<java.util.Map<String, Object>> scores = scoreDAO.getScoresByCodeSession(codeSession);

        if (scores == null || scores.isEmpty()) {
            System.out.println("❌ Aucun score trouvé pour la session " + codeSession);
            return null;
        }

        // 🧠 LOGIQUE MÉTIER (SERVICE) : Comparer les scores et déterminer le gagnant
        // Les scores sont déjà triés DESC par le DAO, donc le premier est le gagnant
        java.util.Map<String, Object> gagnantData = scores.get(0);
        String gagnant = (String) gagnantData.get("nom");
        int scoreGagnant = (int) gagnantData.get("score");

        // Construire le classement avec médailles
        StringBuilder classement = new StringBuilder("🏆 RÉSULTATS FINAUX\n\n");
        for (int i = 0; i < scores.size(); i++) {
            java.util.Map<String, Object> scoreData = scores.get(i);
            String nom = (String) scoreData.get("nom");
            int score = (int) scoreData.get("score");
            String medal = i == 0 ? "🥇" : (i == 1 ? "🥈" : (i == 2 ? "🥉" : (i + 1) + "."));
            classement.append(medal).append(" ").append(nom).append(" - ").append(score).append(" pts\n");
        }

        // Ajouter l'annonce du gagnant
        classement.append("\n🎉 GAGNANT : ").append(gagnant).append(" (").append(scoreGagnant).append(" pts)!");

        // Remplir le résultat
        resultat.put("gagnant", gagnant);
        resultat.put("scoreGagnant", scoreGagnant);
        resultat.put("classement", classement.toString());
        resultat.put("scores", scores);

        System.out.println(
                "✅ Gagnant calculé pour la session " + codeSession + ": " + gagnant + " (" + scoreGagnant + " pts)");

        return resultat;
    }
}
