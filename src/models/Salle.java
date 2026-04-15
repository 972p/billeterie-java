    package models;

    public class Salle {
        private int id_salle;
        private int id_lieu;
        private String nom;
        private int nb_rangees;
        private int nb_colonnes;
        private int nb_places;

        public Salle(int id_salle, int id_lieu, String nom, int nb_rangees, int nb_colonnes, int nb_places) {
            this.id_salle = id_salle;
            this.id_lieu = id_lieu;
            this.nom = nom;
            this.nb_rangees = nb_rangees;
            this.nb_colonnes = nb_colonnes;
            this.nb_places = nb_places;
        }

        public Salle(int id_lieu, String nom, int nb_rangees, int nb_colonnes, int nb_places) {
            this.id_lieu = id_lieu;
            this.nom = nom;
            this.nb_rangees = nb_rangees;
            this.nb_colonnes = nb_colonnes;
            this.nb_places = nb_places;
        }

        public int getId_salle() {
            return id_salle;
        }

        public int getId_lieu() {
            return id_lieu;
        }

        public String getNom() {
            return nom;
        }

        public int getNb_rangees() {
            return nb_rangees;
        }

        public int getNb_colonnes() {
            return nb_colonnes;
        }

        public int getNb_places() {
            return nb_places;
        }

        public void setId_salle(int id_salle) {
            this.id_salle = id_salle;
        }

        public void setId_lieu(int id_lieu) {
            this.id_lieu = id_lieu;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public void setNb_rangees(int nb_rangees) {
            this.nb_rangees = nb_rangees;
        }

        public void setNb_colonnes(int nb_colonnes) {
            this.nb_colonnes = nb_colonnes;
        }

        public void setNb_places(int nb_places) {
            this.nb_places = nb_places;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Salle salle = (Salle) o;
            return id_salle == salle.id_salle;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(id_salle);
        }

        @Override
        public String toString() {
            return nom;
        }
    }
