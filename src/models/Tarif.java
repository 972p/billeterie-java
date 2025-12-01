package models;
public class Tarif {
    private int id_tarif;
    private int id_evenement;
    private String libelle;
    private int prix;



    public Tarif(int id_tarif, int id_evenement, String libelle, int prix) {
        this.id_tarif = id_tarif;
        this.id_evenement = id_evenement;
        this.libelle = libelle;
        this.prix = prix;
    }

    public int getId_tarif() {return id_tarif;}
    public int getId_evenement() {return id_evenement;}
    public String getLibelle() {return libelle;}
    public int getPrix() {return prix;}
}



