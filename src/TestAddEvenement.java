import models.Evenement;
import DAO.EvenementDAO;

public class TestAddEvenement {
    public static void main(String[] args) {
        try {
            Evenement d = new Evenement(0, "Test", "Test", "Test", 1, "FR", 1, "Autre");
            d.setAffiche(null);
            EvenementDAO dao = new EvenementDAO();
            dao.ajouter(d);
            System.out.println("Success!");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
