package models;
import java.util.Date;

public class Seance {
    private int id_seance;
    private int id_evenement;
    private String id_lieu;
    private Date date_heure;


    public Seance(int id_seance, int id_evenement, String id_lieu, Date date_heure) {
        this.id_seance = id_seance;
        this.id_evenement = id_evenement;
        this.id_lieu = id_lieu;
        this.date_heure = date_heure;
    }

    public int getId_seance() {return id_seance;}
    public int getId_evenement() {return id_evenement;}
    public String getId_lieu() {return id_lieu;}
    public Date getDate_heure() {return date_heure;}
}
