package controllers.admin;

import DAO.LieuDAO;
import DAO.SalleDAO;
import models.Lieu;
import models.Salle;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Tooltip;

import java.sql.SQLException;
import java.util.ArrayList;
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
    @FXML
    private StackPane stageIndicator;

    private SalleDAO salleDAO = new SalleDAO();
    private LieuDAO lieuDAO = new LieuDAO();
    
    // 1 = Active, 0 = Inactive
    private List<Integer> seatStatuses = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            List<Lieu> lieux = lieuDAO.trouverTous();
            comboLieu.getItems().addAll(lieux);
            
            comboLieu.setConverter(new javafx.util.StringConverter<Lieu>() {
                @Override public String toString(Lieu l) { return l != null ? l.getNom() : ""; }
                @Override public Lieu fromString(String s) { return null; }
            });
        } catch (SQLException e) {
            showError("Impossible de charger les lieux : " + e.getMessage());
        }

        champRangees.textProperty().addListener((obs, oldVal, newVal) -> initialiserStatutsEtDessiner());
        champColonnes.textProperty().addListener((obs, oldVal, newVal) -> initialiserStatutsEtDessiner());

        grillePreview.setAlignment(Pos.CENTER);
    }

    private void initialiserStatutsEtDessiner() {
        try {
            String rText = champRangees.getText();
            String cText = champColonnes.getText();
            if (rText.isEmpty() || cText.isEmpty()) return;

            int rangees = Integer.parseInt(rText);
            int colonnes = Integer.parseInt(cText);
            
            if (rangees > 0 && colonnes > 0 && rangees < 100 && colonnes < 100) {
                seatStatuses.clear();
                for (int i = 0; i < rangees * colonnes; i++) {
                    seatStatuses.add(1); // All active by default
                }
                dessinerGrille();
            }
        } catch (NumberFormatException e) {
            grillePreview.getChildren().clear();
            labelCapacite.setText("Capacité : 0 places");
        }
    }

    private void dessinerGrille() {
        grillePreview.getChildren().clear();
        try {
            int rangees = Integer.parseInt(champRangees.getText());
            int colonnes = Integer.parseInt(champColonnes.getText());

            if (rangees > 0 && colonnes > 0) {
                updateCapaciteLabel();

                for (int r = 0; r < rangees; r++) {
                    // Row Label
                    Label rowLabel = new Label(String.valueOf((char) ('A' + r)));
                    rowLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10; -fx-min-width: 20;");
                    grillePreview.add(rowLabel, 0, r + 1);

                    for (int c = 0; c < colonnes; c++) {
                        // Column Label (only once)
                        if (r == 0) {
                            Label colLabel = new Label(String.valueOf(c + 1));
                            colLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10; -fx-alignment: center; -fx-min-width: 30;");
                            grillePreview.add(colLabel, c + 1, 0);
                        }

                        int index = r * colonnes + c;
                        Button siege = new Button();
                        siege.setPrefSize(30, 30);
                        siege.setTooltip(new Tooltip("Rangée " + (char)('A'+r) + ", Siège " + (c+1)));
                        
                        updateSiegeStyle(siege, seatStatuses.get(index));

                        siege.setOnAction(e -> {
                            int currentStatus = seatStatuses.get(index);
                            int newStatus = (currentStatus == 1) ? 0 : 1;
                            seatStatuses.set(index, newStatus);
                            updateSiegeStyle(siege, newStatus);
                            updateCapaciteLabel();
                        });

                        grillePreview.add(siege, c + 1, r + 1);
                    }
                }
            }
        } catch (Exception e) {}
    }

    private void updateSiegeStyle(Button siege, int status) {
        if (status == 1) {
            siege.setStyle("-fx-background-color: linear-gradient(to bottom right, #03dac6, #018786); " +
                           "-fx-background-radius: 15; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 1);");
        } else {
            siege.setStyle("-fx-background-color: #3c3c4c; -fx-background-radius: 15; -fx-cursor: hand; -fx-opacity: 0.3;");
        }
    }

    private void updateCapaciteLabel() {
        long activeCount = seatStatuses.stream().filter(s -> s == 1).count();
        labelCapacite.setText("Capacité : " + activeCount + " places");
    }

    @FXML
    private void handleSauvegarder() {
        Lieu lieuChoisi = comboLieu.getValue();
        String nom = champNom.getText().trim();
        
        if (lieuChoisi == null || nom.isEmpty() || seatStatuses.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            int rangees = Integer.parseInt(champRangees.getText());
            int colonnes = Integer.parseInt(champColonnes.getText());
            int nbPlaces = (int) seatStatuses.stream().filter(s -> s == 1).count();
            
            Salle nouvelleSalle = new Salle(lieuChoisi.getId_lieu(), nom, rangees, colonnes, nbPlaces);
            salleDAO.ajouter(nouvelleSalle, seatStatuses);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Salle designer sauvegardée !");
            alert.setContentText("La salle '" + nom + "' a été créée avec " + nbPlaces + " places actives.");
            alert.showAndWait();
            
            reinitialiserFormulaire();
        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    private void reinitialiserFormulaire() {
        champNom.clear();
        champRangees.clear();
        champColonnes.clear();
        comboLieu.setValue(null);
        grillePreview.getChildren().clear();
        labelCapacite.setText("Capacité : 0 places");
        seatStatuses.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.show();
    }
}