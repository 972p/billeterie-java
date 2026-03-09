package models;

public class Billet {
    private int id_billet;
    private int id_seance;
    private int id_tarif;
    private int id_client;
    private int id_place;
    private String statut;
    private String date_achat;

    public Billet(int id_billet, int id_seance, int id_tarif, int id_client, int id_place, String statut,
            String date_achat) {
        this.id_billet = id_billet;
        this.id_seance = id_seance;
        this.id_tarif = id_tarif;
        this.id_client = id_client;
        this.id_place = id_place;
        this.statut = statut;
        this.date_achat = date_achat;
    }

    public int getId_billet() {
        return id_billet;
    }

    public int getId_seance() {
        return id_seance;
    }

    public int getId_tarif() {
        return id_tarif;
    }

    public int getId_client() {
        return id_client;
    }

    public int getId_place() {
        return id_place;
    }

    public String getStatut() {
        return statut;
    }

    public String getDate_achat() {
        return date_achat;
    }
}
