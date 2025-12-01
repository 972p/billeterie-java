import DAO.EvenementDAO;
import models.Evenement;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            EvenementDAO evenementDao = new EvenementDAO();

            // Récupère tous les évènements
            List<Evenement> events = evenementDao.selectAll();

            for (Evenement event : events) {
                System.out.println("Titre: " + event.getTitre());
            }

            System.out.println("Exemple DAO initialisé avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
