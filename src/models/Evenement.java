package models;
public class Evenement {
    private  final String titre;
    private final String description_courte;
    private final String description_longue;
    private final int duree;
    private final String langue;
    private final int age_min;


    public Evenement(String titre, String description_courte, String description_longue, int duree, String langue, int age_min) {
        this.titre = titre;
        this.description_courte = description_courte;
        this.description_longue = description_longue;
        this.duree = duree;
        this.langue = langue;
        this.age_min = age_min;
    }

    public String getTitre() {return titre;}
    public String getDescriptionCourte() {return description_courte;}
    public String getDescriptionLongue() {return description_longue;}
    public int getDuree() {return duree;}
    public String getLangue() {return langue;}
    public int getAgeMin() {return age_min;}
}
