package com.baccalaureatplus.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieDAO {

    /**
     * Récupérer toutes les catégories
     */
    public List<String> getToutesLesCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT nom FROM categories ORDER BY id";

        try (Connection conn = DBConnexion.getConnexion();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(rs.getString("nom"));
            }

            System.out.println("✅ " + categories.size() + " catégories récupérées");

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des catégories");
            e.printStackTrace();
        }

        return categories;
    }

    /**
     * Récupérer l'ID d'une catégorie par son nom
     */
    public int getIdCategorie(String nomCategorie) {
        String sql = "SELECT id FROM categories WHERE nom = ?";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomCategorie);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de l'ID de la catégorie: " + nomCategorie);
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Ajouter une nouvelle catégorie
     */
    public boolean ajouterCategorie(String nomCategorie) {
        String sql = "INSERT INTO categories (nom) VALUES (?)";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomCategorie);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Catégorie ajoutée: " + nomCategorie);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de la catégorie: " + nomCategorie);
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Vérifier si une catégorie existe
     */
    public boolean categorieExiste(String nomCategorie) {
        String sql = "SELECT COUNT(*) FROM categories WHERE nom = ?";

        try (Connection conn = DBConnexion.getConnexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomCategorie);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de la catégorie");
            e.printStackTrace();
        }

        return false;
    }
}
