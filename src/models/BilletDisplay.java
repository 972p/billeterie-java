package models;

public class BilletDisplay {
    private int id_billet;
    private String nom_evenement;
    private String date_seance;
    private String heure_seance;
    private String nom_salle;
    private String nom_lieu;
    private String rangee_siege;
    private String numero_siege;
    private double prix;
    private String statut;
    private String date_achat;

    public BilletDisplay(int id_billet, String nom_evenement, String date_seance, String heure_seance,
            String nom_salle, String nom_lieu, String rangee_siege, String numero_siege,
            double prix, String statut, String date_achat) {
        this.id_billet = id_billet;
        this.nom_evenement = nom_evenement;
        this.date_seance = date_seance;
        this.heure_seance = heure_seance;
        this.nom_salle = nom_salle;
        this.nom_lieu = nom_lieu;
        this.rangee_siege = rangee_siege;
        this.numero_siege = numero_siege;
        this.prix = prix;
        this.statut = statut;
        this.date_achat = date_achat;
    }

    // Getters for TableView properties
    public int getId_billet() {
        return id_billet;
    }

    public String getNom_evenement() {
        return nom_evenement;
    }

    public String getDate_seance() {
        return date_seance;
    }

    public String getHeure_seance() {
        return heure_seance;
    }

    public String getNom_salle() {
        return nom_salle;
    }

    public String getNom_lieu() {
        return nom_lieu;
    }

    public String getRangee_siege() {
        return rangee_siege;
    }

    public String getNumero_siege() {
        return numero_siege;
    }

    public double getPrix() {
        return prix;
    }

    public String getStatut() {
        return statut;
    }

    public String getDate_achat() {
        return date_achat;
    }

    // Derived getters for easier display
    public String getSiegeComplet() {
        if (rangee_siege != null && numero_siege != null) {
            return "Rangée " + rangee_siege + " - Siège " + numero_siege;
        }
        return "Général";
    }
}
