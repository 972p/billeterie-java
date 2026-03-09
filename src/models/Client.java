package models;

public class Client {
    private int id_client;
    private String nom;
    private String email;
    private String telephone;
    private String adresse;
    private String motDePasse;
    private String role;
    private String photoProfil;

    public Client(int id, String nom, String email, String telephone, String adresse, String motDePasse, String role,
            String photoProfil) {
        this.id_client = id;
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.motDePasse = motDePasse;
        this.role = role;
        this.photoProfil = photoProfil;
    }

    public Client(int id, String nom, String email, String telephone, String adresse, String motDePasse, String role) {
        this(id, nom, email, telephone, adresse, motDePasse, role, null);
    }

    // Constructor used for registering a new client without knowing ID yet
    public Client(String nom, String email, String telephone, String adresse, String motDePasse, String role,
            String photoProfil) {
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.motDePasse = motDePasse;
        this.role = role;
        this.photoProfil = photoProfil;
    }

    public Client(String nom, String email, String telephone, String adresse, String motDePasse, String role) {
        this(nom, email, telephone, adresse, motDePasse, role, null);
    }

    public int getId() {
        return id_client;
    }

    public String getNom() {
        return nom;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public String getRole() {
        return role;
    }

    public String getPhotoProfil() {
        return photoProfil;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setPhotoProfil(String photoProfil) {
        this.photoProfil = photoProfil;
    }
}