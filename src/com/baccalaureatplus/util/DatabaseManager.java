package com.baccalaureatplus.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseManager {
    // Configuration MySQL - MODIFIEZ CES VALEURS selon votre configuration
    private static final String URL = "jdbc:mysql://localhost:3306/baccalaureat_plus";
    private static final String USER = "root"; // Changez par votre nom d'utilisateur MySQL
    private static final String PASSWORD = ""; // Changez par votre mot de passe MySQL

    public static Connection getConnection() throws Exception {
        // Charger le driver MySQL (optionnel pour JDBC 4.0+)
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void setupDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Table pour les catégories personnalisables
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nom VARCHAR(255) UNIQUE NOT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // Table pour le cache des mots validés
            stmt.execute("CREATE TABLE IF NOT EXISTS mots_valides (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "mot VARCHAR(255) NOT NULL, " +
                    "id_categorie INT, " +
                    "FOREIGN KEY (id_categorie) REFERENCES categories(id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // Insertion par défaut si vide
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO categories (nom) VALUES ('Prénom'), ('Animal'), ('Ville')");
            }
        } catch (Exception e) {
            System.err.println("Erreur de connexion à la base de données MySQL:");
            System.err.println("Vérifiez que :");
            System.err.println("1. MySQL est démarré");
            System.err.println("2. La base de données 'baccalaureat_plus' existe");
            System.err.println("3. Les identifiants sont corrects");
            e.printStackTrace();
        }
    }
}