package models;

public class Place {
    private int id_place;
    private int id_salle;
    private int rangee;
    private int numero;

    public Place(int id_place, int id_salle, int rangee, int numero) {
        this.id_place = id_place;
        this.id_salle = id_salle;
        this.rangee = rangee;
        this.numero = numero;
    }

    public Place(int id_salle, int rangee, int numero) {
        this.id_salle = id_salle;
        this.rangee = rangee;
        this.numero = numero;
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

    public void setNumero(int numero) {
        this.numero = numero;
    }
}
