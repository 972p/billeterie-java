package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import models.Client;
import utils.SessionManager;

import java.io.IOException;

public class AdminController {

    private static AdminController instance;

    @FXML private BorderPane mainContainer;
    @FXML private Label lblAdminName;

    public AdminController() {
        instance = this;
    }

    public static AdminController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        Client currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            lblAdminName.setText(currentUser.getNom() + " (Admin)");
        }
        // Load Evenements as the default view
        loadCenterView("/views/evenement/Evenement.fxml");
    }

    @FXML
    private void handleViewEvenements() {
        loadCenterView("/views/evenement/Evenement.fxml");
    }

    @FXML
    private void handleViewUsers() {
        loadCenterView("/views/AdminUsers.fxml");
    }

    @FXML
    private void handleViewPrestataires() {
        loadCenterView("/views/AdminPrestataires.fxml");
    }

    @FXML
    private void handleViewReservations() {
        loadCenterView("/views/AdminReservations.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.clearSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
            stage.setTitle("Connexion");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadCenterView(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainContainer.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible de charger la vue : " + fxmlPath);
            alert.show();
        }
    }

    public void setCenterView(Parent view) {
        if (mainContainer != null) mainContainer.setCenter(view);
    }
}
