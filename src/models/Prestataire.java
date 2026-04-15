package models;

public class Prestataire {
    private final int id;
    private final String nom;
    private final String specialite;
    private final String contact;
    private final String email;
    private final String motDePasse;

    public Prestataire(int id, String nom, String specialite, String contact, String email, String motDePasse) {
        this.id = id;
        this.nom = nom;
        this.specialite = specialite;
        this.contact = contact;
        this.email = email;
        this.motDePasse = motDePasse;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getSpecialite() { return specialite; }
    public String getContact() { return contact; }
    public String getEmail() { return email; }
    public String getMotDePasse() { return motDePasse; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prestataire that = (Prestataire) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

    @Override
    public String toString() {
        return nom;
    }
}
