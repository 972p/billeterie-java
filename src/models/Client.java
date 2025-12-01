package models;
public class Client {
    private int id_client;
    private String nom;
    private String email;
    private String telephone;
    private String adresse;


    public Client(int id, String nom, String email, String telephone, String adresse) {
        this.id_client = id;
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
    }


    public int getId() {return id_client;}
    public String getNom() {return nom;}
    public String getEmail() {return email;}
    public String getTelephone() {return telephone;}
    public String getAdresse() {return adresse;}

}