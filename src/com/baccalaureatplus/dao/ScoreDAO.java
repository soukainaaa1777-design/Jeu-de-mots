package com.baccalaureatplus.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreDAO {

    /**
     * Enregistrer un nouveau score
     */
    public int enregistrerScore(int idJoueur, int score, char lettre, int tempsSeconde) {
        String sql = "INSERT INTO scores (id_joueur, score, lettre, temps_seconde) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, idJoueur);
            stmt.setInt(2, score);
            stmt.setString(3, String.valueOf(lettre));
            stmt.setInt(4, tempsSeconde);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    System.out.println("✅ Score enregistré: " + score + " pts (ID: " + id + ")");
                    return id;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'enregistrement du score");
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Enregistrer un score en mode MULTIJOUEUR avec code de session
     * Permet de relier les scores à la session multijoueur
     */
    public int enregistrerScoreMultijoueur(int idJoueur, int score, char lettre, int tempsSeconde, String codeSession) {
        String sql = "INSERT INTO scores (id_joueur, score, lettre, temps_seconde, code_session) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, idJoueur);
            stmt.setInt(2, score);
            stmt.setString(3, String.valueOf(lettre));
            stmt.setInt(4, tempsSeconde);
            stmt.setString(5, codeSession);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    System.out.println("✅ Score multijoueur enregistré: " + score + " pts (Session: " + codeSession
                            + ", ID: " + id + ")");
                    return id;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'enregistrement du score multijoueur");
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Récupérer tous les scores d'un joueur
     */
    public List<Map<String, Object>> getScoresJoueur(int idJoueur) {
        List<Map<String, Object>> scores = new ArrayList<>();
        String sql = "SELECT score, lettre, temps_seconde, date_partie FROM scores WHERE id_joueur = ? ORDER BY date_partie DESC";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idJoueur);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> scoreData = new HashMap<>();
                scoreData.put("score", rs.getInt("score"));
                scoreData.put("lettre", rs.getString("lettre"));
                scoreData.put("temps", rs.getInt("temps_seconde"));
                scoreData.put("date", rs.getTimestamp("date_partie"));
                scores.add(scoreData);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des scores du joueur");
            e.printStackTrace();
        }

        return scores;
    }

    /**
     * Récupérer le meilleur score d'un joueur
     */
    public int getMeilleurScore(int idJoueur) {
        String sql = "SELECT MAX(score) as meilleur FROM scores WHERE id_joueur = ?";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idJoueur);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("meilleur");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération du meilleur score");
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Récupérer le classement général (Top 10)
     */
    public List<Map<String, Object>> getTopScores(int limite) {
        List<Map<String, Object>> classement = new ArrayList<>();
        String sql = "SELECT j.prenom, j.nom, j.mode_jeu, s.score, s.lettre, s.temps_seconde, s.date_partie " +
                "FROM scores s " +
                "JOIN joueurs j ON s.id_joueur = j.id " +
                "ORDER BY s.score DESC, s.temps_seconde ASC " +
                "LIMIT ?";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limite);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                String mode = rs.getString("mode_jeu");
                String nom = mode.equals("SOLO") ? rs.getString("prenom") : rs.getString("nom");

                entry.put("nom", nom);
                entry.put("score", rs.getInt("score"));
                entry.put("lettre", rs.getString("lettre"));
                entry.put("temps", rs.getInt("temps_seconde"));
                entry.put("date", rs.getTimestamp("date_partie"));
                classement.add(entry);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération du classement");
            e.printStackTrace();
        }

        return classement;
    }

    /**
     * Récupérer le nombre total de parties jouées
     */
    public int getNombreTotalParties() {
        String sql = "SELECT COUNT(*) as total FROM scores";

        try (Connection conn = DBConnexion.getConnexion();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des parties");
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Supprimer un score
     */
    public boolean supprimerScore(int id) {
        String sql = "DELETE FROM scores WHERE id = ?";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Score supprimé (ID: " + id + ")");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du score");
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Récupérer tous les scores d'une session multijoueur par code de session
     * Permet de charger les scores de TOUS les joueurs qui ont joué dans cette
     * session
     */
    public List<Map<String, Object>> getScoresByCodeSession(String codeSession) {
        List<Map<String, Object>> scores = new ArrayList<>();
        String sql = "SELECT j.nom, j.prenom, s.score, s.lettre, s.temps_seconde, s.date_partie " +
                "FROM scores s " +
                "JOIN joueurs j ON s.id_joueur = j.id " +
                "WHERE s.code_session = ? " +
                "ORDER BY s.score DESC, s.temps_seconde ASC";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codeSession);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> scoreData = new HashMap<>();
                String prenom = rs.getString("prenom");
                String nom = rs.getString("nom");

                scoreData.put("prenom", prenom);
                scoreData.put("nom", nom);
                scoreData.put("score", rs.getInt("score"));
                scoreData.put("lettre", rs.getString("lettre"));
                scoreData.put("temps", rs.getInt("temps_seconde"));
                scoreData.put("date", rs.getTimestamp("date_partie"));
                scores.add(scoreData);
            }

            System.out.println("✅ Scores chargés pour la session " + codeSession + ": " + scores.size() + " joueurs");

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des scores par session");
            e.printStackTrace();
        }

        return scores;
    }

}
