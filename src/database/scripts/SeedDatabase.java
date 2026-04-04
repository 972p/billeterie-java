package database.scripts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import database.MySQLConnection;

/**
 * Seeds the database with high-quality events, seances, and demo data.
 * Run this class from the IDE to refresh the demo environment.
 */
public class SeedDatabase {

    static final String[][] EVENTS = {
        {"Les Misérables", "Le chef-d'œuvre musical de Victor Hugo.", "Une production époustouflante du célèbre roman de Victor Hugo, mettant en scène la France du XIXe siècle. Jean Valjean, poursuivi par l'inspecteur Javert, cherche rédemption dans un contexte de révolution.", "180", "Français", "8", "Théâtre"},
        {"Daft Punk Tribute", "Le meilleur hommage aux robots de la techno.", "Revivez les plus grands hits de Daft Punk avec un groupe de musiciens et un show laser époustouflant. Get Lucky, Harder Better Faster Stronger, Around the World — tout y est !", "120", "Instrumental", "0", "Concert"},
        {"Paris Saint-Germain vs. Marseille", "Le classique de la Ligue 1.", "Le plus grand derby du football français. Deux titans s'affrontent au Parc des Princes dans une atmosphère électrique et irremplaçable. Venez vibrer pour vos couleurs !", "110", "Français", "0", "Sport"},
        {"Festival Rock en Seine", "Le festival rock en plein air incontournable.", "Trois jours de concert en plein air dans le magnifique Domaine de Saint Cloud. Des dizaines d'artistes de renommée mondiale, de la nourriture locale et des expériences inoubliables.", "240", "Multi", "0", "Festival"},
        {"Hamlet - Shakespeare", "La tragédie immortelle de Shakespeare.", "Une mise en scène contemporaine de la grande tragédie du Prince du Danemark. Le doute, la vengeance, et l'amour s'entremêlent dans ce chef-d'oeuvre atemporel.", "160", "Français", "12", "Théâtre"},
        {"Coldplay World Tour", "Music of the Spheres en direct à Paris.", "Coldplay revient en France pour son tour mondial légendaire, avec un spectacle de lumières interactif et des ballons colorés. Un show qui fait pleurer de bonheur.", "150", "Anglais", "0", "Concert"},
        {"Roland Garros - Finale Hommes", "La grande finale de Roland Garros.", "Soyez témoins de l'histoire du tennis sur la terre battue parisienne. La finales hommes promet un affrontement entre les meilleurs tennismen de la planète.", "180", "Multi", "0", "Sport"},
        {"Avengers : Infinity War - Ciné-Concert", "La bande son jouée live.", "L'Orchestre Philharmonique de Paris interprète en direct la bande originale complète du film Avengers : Infinity War, projeté sur grand écran. Une expérience cinématographique et musicale unique.", "150", "Multi", "0", "Cinéma"},
        {"Nuit des Musées - Louvre", "La magie du Louvre la nuit.", "Une nuit exceptionnelle au sein du Musée du Louvre. Visitez les collections permanentes éclairées différemment et découvrez des installations artistiques contemporaines entre les chefs-d'oeuvre.", "300", "Français", "0", "Autre"},
        {"Cirque du Soleil - O", "Le spectacle aquatique de Las Vegas à Paris.", "Le Cirque du Soleil présente son spectacle aquatique légendaire 'O'. Acrobates, nageurs synchronisés, danseurs et plongeurs de haut vol se produisent dans et autour d'un bassin aquatique de 1,5 million de litres.", "120", "Multi", "0", "Autre"},
    };

    public static void main(String[] args) {
        try (Connection conn = MySQLConnection.connect()) {
            if (conn == null) { System.out.println("Connection failed."); return; }

            // 1. Clear
            conn.createStatement().executeUpdate("DELETE FROM Billet");
            conn.createStatement().executeUpdate("DELETE FROM Seance");
            conn.createStatement().executeUpdate("DELETE FROM Tarif");
            conn.createStatement().executeUpdate("DELETE FROM Evenement");
            System.out.println("Tables cleared.");

            // 2. Get first salle
            int idSalle = -1, idLieu = -1;
            ResultSet rs = conn.createStatement().executeQuery("SELECT id_salle, id_lieu FROM Salle LIMIT 1");
            if (rs.next()) { idSalle = rs.getInt("id_salle"); idLieu = rs.getInt("id_lieu"); }
            if (idSalle == -1) { System.out.println("No Salle found !"); return; }

            // 3. Insert events and seances
            PreparedStatement psEv = conn.prepareStatement(
                "INSERT INTO Evenement (titre, description_courte, description_longue, duree, langue, age_min, categorie, affiche) VALUES (?,?,?,?,?,?,?,?)",
                java.sql.Statement.RETURN_GENERATED_KEYS);
            PreparedStatement psSeance = conn.prepareStatement(
                "INSERT INTO Seance (id_evenement, id_lieu, id_salle, date_heure) VALUES (?,?,?,?)");
            PreparedStatement psTarif = conn.prepareStatement(
                "INSERT INTO Tarif (id_evenement, libelle, prix) VALUES (?,?,?)");

            String[] posters = {
                "ressource/posters/miserables.png",
                "ressource/posters/daftpunk.png",
                "ressource/posters/psgom.png",
                null, null, null, null, null, null, null
            };

            String[] dates = {
                "2026-04-12 19:30:00", "2026-04-15 20:00:00", "2026-04-18 21:00:00",
                "2026-04-20 16:00:00", "2026-04-22 20:00:00", "2026-04-25 20:30:00",
                "2026-04-27 14:00:00", "2026-04-29 20:00:00", "2026-05-02 19:00:00",
                "2026-05-05 19:00:00"
            };
            double[] prices = {65.0, 45.0, 85.0, 55.0, 50.0, 90.0, 120.0, 40.0, 15.0, 75.0};

            for (int i = 0; i < EVENTS.length; i++) {
                psEv.setString(1, EVENTS[i][0]);
                psEv.setString(2, EVENTS[i][1]);
                psEv.setString(3, EVENTS[i][2]);
                psEv.setInt(4, Integer.parseInt(EVENTS[i][3]));
                psEv.setString(5, EVENTS[i][4]);
                psEv.setInt(6, Integer.parseInt(EVENTS[i][5]));
                psEv.setString(7, EVENTS[i][6]);
                psEv.setString(8, posters[i]);
                psEv.executeUpdate();

                ResultSet gk = psEv.getGeneratedKeys();
                int newId = 0;
                if (gk.next()) newId = gk.getInt(1);

                psSeance.setInt(1, newId); psSeance.setInt(2, idLieu);
                psSeance.setInt(3, idSalle); psSeance.setString(4, dates[i]);
                psSeance.executeUpdate();

                psTarif.setInt(1, newId); psTarif.setString(2, "Standard");
                psTarif.setDouble(3, prices[i]); psTarif.executeUpdate();

                psTarif.setInt(1, newId); psTarif.setString(2, "Etudiant");
                psTarif.setDouble(3, prices[i] * 0.7); psTarif.executeUpdate();

                psTarif.setInt(1, newId); psTarif.setString(2, "Enfant");
                psTarif.setDouble(3, prices[i] * 0.5); psTarif.executeUpdate();
            }

            // 4. Update demo user balance
            conn.createStatement().executeUpdate("UPDATE Client SET solde = 500.0 WHERE email = 'user@example.com'");
            System.out.println("Done! Updated schema, inserted events, and credited demo user.");
        } catch (Exception e) { e.printStackTrace(); }
    }
}
