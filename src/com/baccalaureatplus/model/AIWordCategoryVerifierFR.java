package com.baccalaureatplus.model;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AIWordCategoryVerifierFR {

    private static final String GEONAMES_USER = "amirazahr";

    public static CompletableFuture<Boolean> verifierAvecIA(String mot, String categorie) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (mot == null || mot.trim().isEmpty())
                    return false;

                String motNettoye = mot.trim().toLowerCase();
                String cat = categorie.toLowerCase();

                // 1. Vérification PRIORITAIRE en local (Kiwi, Kale, Kaki, etc.)
                if (verifierEnLocal(motNettoye, cat))
                    return true;

                // 2. Préparation pour les APIs
                String motEncode = URLEncoder.encode(motNettoye, StandardCharsets.UTF_8.toString());

                switch (cat) {
                    case "prénom":
                        return verifierPrenom(motEncode);
                    case "pays":
                        return verifierPays(motNettoye);
                    case "ville":
                        return verifierVille(motEncode);
                    case "animal":
                    case "fruit":
                    case "légume":
                    case "objet":
                        return verifierCNRTL(motEncode, cat, motNettoye);
                    default:
                        return false;
                }
            } catch (Exception e) {
                return false;
            }
        });
    }

    // LISTE DE SECOURS pour les mots qui échouent souvent en ligne (K, N, etc.)
    private static boolean verifierEnLocal(String mot, String cat) {
        switch (cat) {
            case "fruit":
                return Arrays.asList("kiwi", "kaki", "kumquat", "nectarine", "litchi", "mangue", "papaye")
                        .contains(mot);
            case "légume":
                return Arrays.asList("kale", "kohlrabi", "daikon", "manioc", "topinambour", "yam").contains(mot);
            case "animal":
                return Arrays.asList("koala", "koudou", "kiwi", "narval", "dauphin").contains(mot);
            default:
                return false;
        }
    }

    private static boolean verifierVille(String mot) throws Exception {
        // Rejeter les mots trop courts
        if (mot.trim().length() < 3)
            return false;

        // Chercher les villes avec featureClass P (villes et villages)
        String urlStr = "http://api.geonames.org/searchJSON?q=" + URLEncoder.encode(mot, "UTF-8")
                + "&featureClass=P&featureCode=PPL&maxRows=1&username=" + GEONAMES_USER;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(4000);
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String json = br.readLine();
            if (json == null)
                return false;

            // Vérifier strictement : totalResultsCount > 0 ET population > 0
            boolean resultatValide = json.contains("\"totalResultsCount\":")
                    && !json.contains("\"totalResultsCount\":0");
            boolean haPopulation = json.contains("\"population\":") && !json.contains("\"population\":0");

            return resultatValide && haPopulation;
        }
    }

    private static boolean verifierPays(String mot) throws Exception {
        List<String> paysValides = Arrays.asList(
                "france", "algerie", "maroc", "tunisie", "canada", "belgique", "suisse",
                "espagne", "italie", "allemagne", "bresil", "japon", "chine", "egypte", "senegal", "cameroun",
                "norvege", "danemark", "kenya", "suede", "finlande", "portugal", "grece", "turquie", "pakistan",
                "inde", "thaïlande", "vietnam", "laos", "birmanie", "cambodge", "malaisie", "indonesie", "philippines",
                "singapour", "hongkong", "taiwan", "coree du sud", "coree", "mongolie", "kazakhstan", "ouzbekistan",
                "turkmenistan", "tadjikistan", "kirghizistan", "afghanistan", "iran", "irak", "syrie", "liban",
                "israel", "palestine", "jordanie", "arabie saoudite", "yemen", "oman", "emirats", "qatar", "bahrein",
                "koweit", "argentine", "chili", "perou", "colombie", "venezuela", "ecuador", "paraguay", "uruguay",
                "bolivie", "guyane", "suriname", "afrique du sud", "botswana", "namibie", "zimbabwe", "zambie",
                "malawi", "tanzanie", "ouganda", "rwanda", "burundi", "congo", "gabon", "cameroun", "benin",
                "togo", "ghana", "côte d'ivoire", "mali", "burkina", "niger", "tchad", "soudan", "somalie",
                "ethiopie", "erythree", "djibouti", "mauritanie", "senegal", "gambie", "guinee", "liberia",
                "sierra leone", "madagascar", "maurice", "seychelles", "comores", "nouvelle zelande", "australie",
                "papouasie", "fidji", "samoa", "tonga", "vanuatu", "kiribati", "nauru", "palaos", "marshall");

        String motNettoye = mot.trim().toLowerCase();
        if (paysValides.contains(motNettoye))
            return true;

        // Vérifier aussi avec variantes communes
        if (motNettoye.contains("coree") || motNettoye.contains("corée"))
            return motNettoye.contains("nord") || motNettoye.contains("sud");

        // API geonames en dernier recours avec vérification stricte
        String urlStr = "http://api.geonames.org/searchJSON?q=" + URLEncoder.encode(mot, "UTF-8")
                + "&featureCode=PCLI&maxRows=1&username=" + GEONAMES_USER;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(4000);
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String json = br.readLine();
            if (json == null)
                return false;
            // Vérifier que c'est bien un résultat valide (totalResultsCount > 0 et
            // population > 0)
            return json.contains("\"totalResultsCount\":") && !json.contains("\"totalResultsCount\":0")
                    && json.contains("\"population\":");
        }
    }

    private static boolean verifierPrenom(String mot) throws Exception {
        URL url = new URL("https://api.agify.io/?name=" + mot);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String json = br.readLine();
            return json != null && !json.contains("\"count\":0");
        }
    }

    private static boolean verifierCNRTL(String motEncode, String cat, String motBrut) throws Exception {
        // Bloque les articles ou prépositions
        if (motBrut.length() < 3 && !motBrut.equals("dé") && !motBrut.equals("if") && !motBrut.equals("ail"))
            return false;

        URL url = new URL("https://www.cnrtl.fr/definition/" + motEncode);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        if (conn.getResponseCode() != 200)
            return false;

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line.toLowerCase());
        }
        String res = sb.toString();

        switch (cat) {
            case "fruit":
                return res.contains("fruit") || res.contains("bot.") || res.contains("comestible") ||
                        res.contains("drupe") || res.contains("charnu") || res.contains("pépin") ||
                        res.contains("noyau") || res.contains("variété de") || res.contains("sucré")
                        || res.contains("baie");

            case "légume":
                return res.contains("légume") || res.contains("potag") || res.contains("herbacée") ||
                        res.contains("racine") || res.contains("bulbe") || res.contains("comestible") ||
                        res.contains("plante") || res.contains("chou");

            case "animal":
                return res.contains("zool.") || res.contains("mammifère") || res.contains("oiseau") ||
                        res.contains("animal") || res.contains("poisson") || res.contains("vertébré") ||
                        res.contains("cétacé") || res.contains("reptile") || res.contains("mollusque");

            case "objet":
                if (motBrut.equals("de"))
                    return false;
                return res.contains("instrument") || res.contains("ustensile") || res.contains("meuble") ||
                        res.contains("appareil") || res.contains("objet") || res.contains("linge") ||
                        res.contains("outil") || res.contains("récipient") || res.contains("tissu")
                        || res.contains("vêtement");

            default:
                return false;
        }
    }
}