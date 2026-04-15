package models;

public class Place {
    private int id_place;
    private int id_salle;
    private int rangee;
    private int numero;
    private int statut; // 1 = Active, 0 = Inactive

    public Place(int id_place, int id_salle, int rangee, int numero, int statut) {
        this.id_place = id_place;
        this.id_salle = id_salle;
        this.rangee = rangee;
        this.numero = numero;
        this.statut = statut;
    }

    public Place(int id_place, int id_salle, int rangee, int numero) {
        this(id_place, id_salle, rangee, numero, 1);
    }

    public Place(int id_salle, int rangee, int numero, int statut) {
        this.id_salle = id_salle;
        this.rangee = rangee;
        this.numero = numero;
        this.statut = statut;
    }

    public Place(int id_salle, int rangee, int numero) {
        this(0, id_salle, rangee, numero, 1);
    }

    public int getId_place() {
        return id_place;
    }

    public int getId_salle() {
        return id_salle;
    }

    public int getRangee() {
        return rangee;
    }

    public int getNumero() {
        return numero;
    }

    public void setId_place(int id_place) {
        this.id_place = id_place;
    }

    public void setId_salle(int id_salle) {
        this.id_salle = id_salle;
    }

    public void setRangee(int rangee) {
        this.rangee = rangee;
    }

    public int getStatut() {
        return statut;
    }

    public void setStatut(int statut) {
        this.statut = statut;
    }
}
