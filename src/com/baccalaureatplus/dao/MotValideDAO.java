package com.baccalaureatplus.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MotValideDAO {

    /**
     * Ajouter un mot validé dans le cache
     */
    public boolean ajouterMotValide(String mot, String categorie) {
        // D'abord récupérer l'ID de la catégorie
        CategorieDAO categorieDAO = new CategorieDAO();
        int idCategorie = categorieDAO.getIdCategorie(categorie);

        if (idCategorie == -1) {
            System.err.println("❌ Catégorie non trouvée: " + categorie);
            return false;
        }

        String sql = "INSERT INTO mots_valides (mot, id_categorie) VALUES (?, ?)";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mot);
            stmt.setInt(2, idCategorie);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Mot validé ajouté: " + mot + " (" + categorie + ")");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout du mot validé");
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Vérifier si un mot existe déjà dans le cache
     */
    public boolean motExiste(String mot, String categorie) {
        CategorieDAO categorieDAO = new CategorieDAO();
        int idCategorie = categorieDAO.getIdCategorie(categorie);

        if (idCategorie == -1) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM mots_valides WHERE mot = ? AND id_categorie = ?";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mot);
            stmt.setInt(2, idCategorie);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification du mot");
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Récupérer tous les mots validés pour une catégorie
     */
    public List<String> getMotsParCategorie(String categorie) {
        List<String> mots = new ArrayList<>();
        CategorieDAO categorieDAO = new CategorieDAO();
        int idCategorie = categorieDAO.getIdCategorie(categorie);

        if (idCategorie == -1) {
            return mots;
        }

        String sql = "SELECT mot FROM mots_valides WHERE id_categorie = ? ORDER BY mot";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCategorie);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                mots.add(rs.getString("mot"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des mots");
            e.printStackTrace();
        }

        return mots;
    }

    /**
     * Supprimer un mot du cache
     */
    public boolean supprimerMot(String mot, String categorie) {
        CategorieDAO categorieDAO = new CategorieDAO();
        int idCategorie = categorieDAO.getIdCategorie(categorie);

        if (idCategorie == -1) {
            return false;
        }

        String sql = "DELETE FROM mots_valides WHERE mot = ? AND id_categorie = ?";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mot);
            stmt.setInt(2, idCategorie);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Mot supprimé: " + mot);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du mot");
            e.printStackTrace();
        }

        return false;
    }
}
