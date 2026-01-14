package com.baccalaureatplus.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JoueurDAO {

    /**
     * Créer un nouveau joueur en mode SOLO
     */
    public int creerJoueurSolo(String prenom) {
        String sql = "INSERT INTO joueurs (mode_jeu, prenom, nombre_joueurs) VALUES ('SOLO', ?, 1)";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, prenom);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    System.out.println("✅ Joueur SOLO créé: " + prenom + " (ID: " + id + ")");
                    return id;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création du joueur SOLO");
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Créer un nouveau joueur en mode MULTIJOUEUR
     */
    public int creerJoueurMulti(String nom, int nombreJoueurs) {
        String sql = "INSERT INTO joueurs (mode_jeu, nom, nombre_joueurs) VALUES ('MULTIJOUEUR', ?, ?)";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nom);
            stmt.setInt(2, nombreJoueurs);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    System.out.println("✅ Joueur MULTI créé: " + nom + " (ID: " + id + ")");
                    return id;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création du joueur MULTI");
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Récupérer un joueur par son ID
     */
    public String getJoueur(int id) {
        String sql = "SELECT mode_jeu, prenom, nom FROM joueurs WHERE id = ?";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String mode = rs.getString("mode_jeu");
                if (mode.equals("SOLO")) {
                    return rs.getString("prenom");
                } else {
                    return rs.getString("nom");
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération du joueur");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Récupérer tous les joueurs SOLO
     */
    public List<String> getTousLesJoueursSolo() {
        List<String> joueurs = new ArrayList<>();
        String sql = "SELECT prenom FROM joueurs WHERE mode_jeu = 'SOLO' ORDER BY date_creation DESC";

        try (Connection conn = DBConnexion.getConnexion();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                joueurs.add(rs.getString("prenom"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des joueurs SOLO");
            e.printStackTrace();
        }

        return joueurs;
    }

    /**
     * Supprimer un joueur
     */
    public boolean supprimerJoueur(int id) {
        String sql = "DELETE FROM joueurs WHERE id = ?";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Joueur supprimé (ID: " + id + ")");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du joueur");
            e.printStackTrace();
        }

        return false;
    }
}
