package models;
public class Lieu {
    public String nom;
    public String adresse;
    public String ville;
    public int capacite;



    public Lieu(String nom, String adresse, String ville, int capacite) {
        this.nom = nom;
        this.adresse = adresse;
        this.ville = ville;
        this.capacite = capacite;
    }


    public String getNom() {return nom;}
    public String getAdresse() {return adresse;}
    public String getVille() {return ville;}
    public int getCapacite() {return capacite;}
}
