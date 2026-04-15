package models;

public class Lieu {
    private int id_lieu;
    private String nom;
    private String adresse;
    private String ville;
    private int capacite;

    public Lieu(int id_lieu, String nom, String adresse, String ville, int capacite) {
        this.id_lieu = id_lieu;
        this.nom = nom;
        this.adresse = adresse;
        this.ville = ville;
        this.capacite = capacite;
    }

    public int getId_lieu() {
        return id_lieu;
    }

    public String getNom() {
        return nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getVille() {
        return ville;
    }

    public int getCapacite() {
        return capacite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lieu lieu = (Lieu) o;
        return id_lieu == lieu.id_lieu;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id_lieu);
    }

    @Override
    public String toString() {
        return this.nom + " (" + this.ville + ")";
    }
}
