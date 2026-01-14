package com.baccalaureatplus.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SessionDAO {

    /**
     * Créer une nouvelle session multijoueur
     * 
     * @param codeSession Code unique de la session (6 caractères)
     * @param nbJoueur    Nombre de joueurs attendus
     * @param hostId      ID du joueur qui crée la partie
     * @param lettre      Lettre aléatoire pour la partie
     * @return ID de la session créée, ou -1 en cas d'erreur
     */
    public int creerSession(String codeSession, int nbJoueur, int hostId, String lettre) {
        String sql = "INSERT INTO multiplayer (code_session, nb_joueur, date_creation, host_id, lettre) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, codeSession);
            stmt.setInt(2, nbJoueur);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(4, hostId);
            stmt.setString(5, lettre);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int sessionId = rs.getInt(1);
                    System.out.println("✅ Session créée avec ID: " + sessionId + " (Code: " + codeSession + ")");
                    return sessionId;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création de la session");
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Vérifier si un code de session existe
     * 
     * @param codeSession Code à vérifier
     * @return true si le code existe, false sinon
     */
    public boolean sessionExiste(String codeSession) {
        String sql = "SELECT COUNT(*) FROM multiplayer WHERE code_session = ?";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codeSession);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de la session");
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Récupérer les informations complètes d'une session par son code
     * 
     * @param codeSession Code de la session
     * @return Map avec [id, nb_joueurs, lettre, host_id] ou null si non trouvé
     */
    public Map<String, Object> getSessionByCode(String codeSession) {
        String sql = "SELECT id, nb_joueur, lettre, host_id FROM multiplayer WHERE code_session = ?";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codeSession);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> sessionInfo = new HashMap<>();
                sessionInfo.put("id", rs.getInt("id"));
                sessionInfo.put("nb_joueurs", rs.getInt("nb_joueur"));
                sessionInfo.put("lettre", rs.getString("lettre"));
                sessionInfo.put("host_id", rs.getInt("host_id"));

                System.out.println("✅ Session trouvée: ID=" + rs.getInt("id") + ", Lettre=" + rs.getString("lettre"));
                return sessionInfo;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de la session par code");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Récupérer les informations d'une session par son code
     * 
     * @param codeSession Code de la session
     * @return Array [id, nbJoueur, hostId] ou null si non trouvé
     */
    public int[] getInfosSession(String codeSession) {
        String sql = "SELECT id, nb_joueur, host_id FROM multiplayer WHERE code_session = ?";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codeSession);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new int[] {
                        rs.getInt("id"),
                        rs.getInt("nb_joueur"),
                        rs.getInt("host_id")
                };
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des infos de session");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Supprimer une session (après la fin de la partie)
     * 
     * @param codeSession Code de la session à supprimer
     * @return true si suppression réussie
     */
    public boolean supprimerSession(String codeSession) {
        String sql = "DELETE FROM multiplayer WHERE code_session = ?";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codeSession);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Session supprimée: " + codeSession);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de la session");
            e.printStackTrace();
        }

        return false;
    }
}
