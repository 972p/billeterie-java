package models;

public class Seance {
    private int id_seance;
    private int id_evenement;
    private int id_lieu;
    private int id_salle;
    private String date_heure;

    public Seance(int id_seance, int id_evenement, int id_lieu, int id_salle, String date_heure) {
        this.id_seance = id_seance;
        this.id_evenement = id_evenement;
        this.id_lieu = id_lieu;
        this.id_salle = id_salle;
        this.date_heure = date_heure;
    }

    public int getId_seance() {
        return id_seance;
    }

    public int getId_evenement() {
        return id_evenement;
    }

    public int getId_lieu() {
        return id_lieu;
    }

    public int getId_salle() {
        return id_salle;
    }

    public String getDate_heure() {
        return date_heure;
    }
}
