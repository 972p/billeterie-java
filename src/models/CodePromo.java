package models;

public class CodePromo {
    private int id_code_promo;
    private String code;
    private String type_reduction; // POURCENTAGE ou FIXE
    private double valeur_reduction;
    private String statut; // ACTIF ou INACTIF

    public CodePromo(int id_code_promo, String code, String type_reduction, double valeur_reduction, String statut) {
        this.id_code_promo = id_code_promo;
        this.code = code;
        this.type_reduction = type_reduction;
        this.valeur_reduction = valeur_reduction;
        this.statut = statut;
    }

    public int getId_code_promo() { return id_code_promo; }
    public void setId_code_promo(int id_code_promo) { this.id_code_promo = id_code_promo; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getType_reduction() { return type_reduction; }
    public void setType_reduction(String type_reduction) { this.type_reduction = type_reduction; }

    public double getValeur_reduction() { return valeur_reduction; }
    public void setValeur_reduction(double valeur_reduction) { this.valeur_reduction = valeur_reduction; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
