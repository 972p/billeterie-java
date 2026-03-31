package models;

public class Service {
    private final int id;
    private final int idPrestataire;
    private final String nom;
    private final String description;

    public Service(int id, int idPrestataire, String nom, String description) {
        this.id = id;
        this.idPrestataire = idPrestataire;
        this.nom = nom;
        this.description = description;
    }

    public int getId() { return id; }
    public int getIdPrestataire() { return idPrestataire; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
}
