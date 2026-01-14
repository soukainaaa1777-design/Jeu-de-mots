package com.baccalaureatplus.service;

import com.baccalaureatplus.model.AIWordCategoryVerifierFR;
import java.util.concurrent.CompletableFuture;

public class ValidationService {

    /**
     * Valider un mot avec vérification de la lettre ET de la catégorie via API
     * 
     * @param mot       Le mot à valider
     * @param lettre    La lettre requise
     * @param categorie La catégorie attendue (prénom, fruit, légume, animal, pays,
     *                  ville, objet)
     * @return CompletableFuture<Boolean> - true si le mot est valide
     */
    public static CompletableFuture<Boolean> validerMotAvecCategorie(final String mot, final String lettre,
            final String categorie) {
        return CompletableFuture.supplyAsync(() -> {
            // 1. Vérifier que le mot n'est pas vide
            if (mot == null || mot.trim().isEmpty()) {
                System.out.println("❌ Mot vide");
                return false;
            }

            final String motTrimmed = mot.trim();

            // 2. Vérifier que le mot commence par la bonne lettre
            if (!motTrimmed.toUpperCase().startsWith(lettre.toUpperCase())) {
                System.out.println("❌ " + motTrimmed + " ne commence pas par " + lettre);
                return false;
            }

            System.out.println(
                    "🔍 Validation de: " + motTrimmed + " (catégorie: " + categorie + ", lettre: " + lettre + ")");

            // 3. Vérifier via l'API que le mot appartient à la catégorie
            try {
                boolean resultat = AIWordCategoryVerifierFR.verifierAvecIA(motTrimmed, categorie).get();
                if (resultat) {
                    System.out.println("✅ " + motTrimmed + " validé pour " + categorie);
                } else {
                    System.out.println("❌ " + motTrimmed + " rejeté pour " + categorie);
                }
                return resultat;
            } catch (Exception e) {
                System.err.println("❌ Erreur API pour " + motTrimmed + ": " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Ancienne méthode - uniquement vérification de la lettre et existence du mot
     * 
     * @deprecated Utiliser validerMotAvecCategorie à la place
     */
    @Deprecated
    public static boolean estValide(String mot, String lettre) {
        if (mot == null || mot.isBlank())
            return false;
        if (lettre == null || lettre.isBlank())
            return false;

        mot = mot.trim();
        return mot.toUpperCase().startsWith(lettre.toUpperCase());
    }
}
