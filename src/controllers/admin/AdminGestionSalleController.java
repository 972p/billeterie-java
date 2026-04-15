package controllers.admin;

import DAO.LieuDAO;
import DAO.SalleDAO;
import models.Lieu;
import models.Salle;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.List;

public class AdminGestionSalleController {

    @FXML
    private ComboBox<Lieu> comboLieu;
    @FXML
    private TextField champNom;
    @FXML
    private TextField champRangees;
    @FXML
    private TextField champColonnes;
    @FXML
    private Label labelCapacite;
    @FXML
    private GridPane grillePreview;

    private SalleDAO salleDAO = new SalleDAO();
    private LieuDAO lieuDAO = new LieuDAO();

    @FXML
    public void initialize() {
        try {
            List<Lieu> lieux = lieuDAO.trouverTous();
            comboLieu.getItems().addAll(lieux);
        } catch (SQLException e) {
            showError("Impossible de charger les lieux : " + e.getMessage());
        }

        champRangees.textProperty().addListener((obs, oldVal, newVal) -> dessinerGrille());
        champColonnes.textProperty().addListener((obs, oldVal, newVal) -> dessinerGrille());

        grillePreview.setAlignment(Pos.CENTER);
    }

    @FXML
    private void handleSauvegarder() {
        Lieu lieuChoisi = comboLieu.getValue();
        if (lieuChoisi == null) {
            showError("Veuillez sélectionner un lieu.");
            return;
        }

        String nom = champNom.getText().trim();
        if (nom.isEmpty()) {
            showError("Veuillez entrer un nom pour la salle.");
            return;
        }

        int rangees, colonnes;
        try {
            rangees = Integer.parseInt(champRangees.getText());
            colonnes = Integer.parseInt(champColonnes.getText());
        } catch (NumberFormatException e) {
            showError("Veuillez entrer des nombres valides pour les dimensions.");
            return;
        }

        if (rangees <= 0 || colonnes <= 0) {
            showError("Les dimensions doivent être des entiers positifs.");
            return;
        }

        try {
            Salle nouvelleSalle = new Salle(lieuChoisi.getId_lieu(), nom, rangees, colonnes);
            salleDAO.ajouter(nouvelleSalle);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Salle créée avec succès !");
            alert.setContentText(
                    "Nom : " + nom + "\n" +
                            "Lieu : " + lieuChoisi.getNom() + "\n" +
                            "Dimensions : " + rangees + " rangées × " + colonnes + " colonnes\n" +
                            "Capacité totale : " + (rangees * colonnes) + " places");
            alert.showAndWait();
            reinitialiserFormulaire();

        } catch (SQLException e) {
            showError("Erreur base de données : " + e.getMessage());
        }
    }

    private void dessinerGrille() {
        grillePreview.getChildren().clear();
        try {
            int rangees = Integer.parseInt(champRangees.getText());
            int colonnes = Integer.parseInt(champColonnes.getText());

            if (rangees > 0 && colonnes > 0) {
                int capacite = rangees * colonnes;
                String forme = (rangees == colonnes) ? " (carrée)" : " (rectangulaire)";
                labelCapacite.setText("Capacité totale : " + capacite + " places" + forme);

                int numeroSiege = 1;
                for (int r = 0; r < rangees; r++) {
                    for (int c = 0; c < colonnes; c++) {
                        Button siege = new Button(String.valueOf(numeroSiege));
                        siege.setPrefSize(38, 38);
                        siege.setStyle(
                                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10; -fx-cursor: hand; -fx-background-radius: 4;");
                        grillePreview.add(siege, c, r);
                        numeroSiege++;
                    }
                }
            } else {
                labelCapacite.setText("Capacité totale : 0 places");
            }
        } catch (NumberFormatException e) {
            labelCapacite.setText("Capacité totale : 0 places");
        }
    }

    private void reinitialiserFormulaire() {
        champNom.clear();
        champRangees.clear();
        champColonnes.clear();
        comboLieu.setValue(null);
        grillePreview.getChildren().clear();
        labelCapacite.setText("Capacité totale : 0 places");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.show();
    }
}