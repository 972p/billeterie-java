package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.Client;
import utils.SessionManager;

import java.io.IOException;

public class ClientController {

    @FXML
    private Label lblWelcome;

    @FXML
    public void initialize() {
        Client currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            lblWelcome.setText("Bienvenue, " + currentUser.getNom() + " !");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.clearSession();
        loadView("/views/Login.fxml", "Connexion", event);
    }

    @FXML
    private void handleViewProfile(ActionEvent event) {
        loadView("/views/ClientProfile.fxml", "Mon Profil", event);
    }

    @FXML
    private void handleViewBillets(ActionEvent event) {
        loadView("/views/billet/ClientBillets.fxml", "Mes Billets", event);
    }

    @FXML
    private void handleBookTicket(ActionEvent event) {
        loadView("/views/evenement/Evenement.fxml", "Réserver un Billet", event);
    }

    private void loadView(String fxmlPath, String title, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);

            Scene scene = new Scene(root);
            String css = this.getClass().getResource("/views/style.css").toExternalForm();
            scene.getStylesheets().add(css);

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page demandée.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
