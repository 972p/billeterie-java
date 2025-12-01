package models;

public class Billet {
    private int id_billet;
    private int id_seance;
    private int id_tarif;
    private int id_client;
    private int numero;
    private String statut;
    private int date_achat;

public Billet(int id_billet, int id_seance, int id_tarif, int id_client, int numero, String statut, int date_achat ) {
    this.id_billet = id_billet;
    this.id_seance = id_seance;
    this.id_tarif = id_tarif;
    this.id_client = id_client;
    this.numero = numero;
    this.statut = statut;
    this.date_achat = date_achat;
}

    public int getId_billet() {return id_billet;}
    public int getId_seance() {return id_seance;}
    public int getId_tarif() {return id_tarif;}
    public int getId_client() {return id_client;}
    public int getNumero() {return numero;}
    public String getStatut() {return statut;}
    public int getDate_achat() {return date_achat;}
}

