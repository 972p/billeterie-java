package controllers;

import database.MySQLConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Client;
import utils.SessionManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PrestataireDashboardController {

    @FXML private BorderPane mainContainer;
    @FXML private Label lblWelcome;

    private Client currentUser;

    @FXML
    public void initialize() {
        if (!SessionManager.isPrestataire()) {
            System.err.println("Acces refusé : utilisateur n'est pas prestataire.");
            return;
        }

        currentUser = SessionManager.getCurrentUser();
        lblWelcome.setText("Bienvenue, " + currentUser.getNom() + " !");

        Platform.runLater(this::handleViewMissions);
    }

    @FXML
    public void handleViewMissions(ActionEvent event) {
        handleViewMissions();
    }

    private void handleViewMissions() {
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setStyle("-fx-padding: 40;");

        Label title = new Label("Mes Missions Assignées");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 28; -fx-font-weight: bold;");

        ListView<String> missionsList = new ListView<>();
        missionsList.setPrefHeight(500);
        missionsList.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #2b2b3c; -fx-control-inner-background-alt: #2b2b3c; -fx-text-fill: white;");
        missionsList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-padding: 15; -fx-border-color: #bb86fc; -fx-border-width: 0 0 1 0; -fx-background-color: transparent;");
                }
            }
        });

        ObservableList<String> items = FXCollections.observableArrayList();

        String sql = "SELECT e.titre, e.date_heure, e.lieu_nom, s.nom as service_nom, s.description " + 
                     "FROM Evenement e " + 
                     "JOIN Evenement_Service es ON e.id_evenement = es.id_evenement " + 
                     "JOIN Service s ON es.id_service = s.id_service " + 
                     "WHERE s.id_prestataire = ?";

        // Since date_heure and lieu_nom might be in Seance, let's join Seance or just get what we have.
        // If Evenement doesn't contain date_heure, we must join Seance.
        sql = "SELECT e.titre, s.nom as service_nom, s.description, " +
              "IFNULL(se.date_heure, 'Non programmée') as date_heure, IFNULL(l.nom, 'Non précisé') as lieu_nom " +
              "FROM Evenement e " +
              "JOIN Evenement_Service es ON e.id_evenement = es.id_evenement " +
              "JOIN Service s ON es.id_service = s.id_service " +
              "LEFT JOIN Seance se ON e.id_evenement = se.id_evenement " +
              "LEFT JOIN Lieu l ON se.id_lieu = l.id_lieu " +
              "WHERE s.id_prestataire = ? " +
              "ORDER BY se.date_heure DESC";

        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, currentUser.getId()); // The ID matches Prestataire.id_prestataire!
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String titre = rs.getString("titre");
                    String sNom = rs.getString("service_nom");
                    String sDesc = rs.getString("description");
                    String date = rs.getString("date_heure");
                    String lieu = rs.getString("lieu_nom");

                    items.add("Évènement : " + titre + "\nDate limite / Représentation : " + date + "\nLieu : " + lieu + 
                              "\n\n👉 Mission attendue :\n" + sNom + " (" + sDesc + ")");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            items.add("Erreur lors de la récupération des missions.");
        }

        if (items.isEmpty()) {
            items.add("Aucune mission ne vous a été assignée pour le moment.");
        }

        missionsList.setItems(items);
        contentBox.getChildren().addAll(title, missionsList);

        mainContainer.setCenter(contentBox);
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        SessionManager.clearSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Connexion");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
