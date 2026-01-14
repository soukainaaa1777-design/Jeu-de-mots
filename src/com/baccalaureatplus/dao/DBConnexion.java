package com.baccalaureatplus.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnexion {

    // Configuration de la base de données MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/baccalaureatplus";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Variable pour stocker la connexion unique (Singleton)
    private static Connection connexion = null;

    /**
     * Méthode pour obtenir une connexion à la base de données
     * 
     * @return Connection - connexion à la base de données
     */
    public static Connection getConnexion() {
        try {
            // Si la connexion n'existe pas ou est fermée, créer une nouvelle connexion
            if (connexion == null || connexion.isClosed()) {
                // Charger le driver MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Établir la connexion
                connexion = DriverManager.getConnection(URL, USER, PASSWORD);

                System.out.println("✅ Connexion à MySQL réussie !");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé !");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données !");
            System.err.println("Vérifiez que :");
            System.err.println("  - MySQL est démarré");
            System.err.println("  - La base 'baccalaureatplus' existe");
            System.err.println("  - Les identifiants sont corrects");
            e.printStackTrace();
        }

        return connexion;
    }

    /**
     * Méthode pour fermer la connexion
     */
    public static void fermerConnexion() {
        try {
            if (connexion != null && !connexion.isClosed()) {
                connexion.close();
                System.out.println("✅ Connexion fermée");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la fermeture de la connexion");
            e.printStackTrace();
        }
    }

    /**
     * Méthode pour tester la connexion
     */
    public static boolean testerConnexion() {
        Connection conn = getConnexion();
        return conn != null;
    }
}
