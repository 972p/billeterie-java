package controllers.client;

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

public class ClientController {

    private static ClientController instance;

    @FXML
    private BorderPane mainContainer;

    @FXML
    private Label lblWelcome;

    @FXML
    private Label lblSolde;

    public ClientController() {
        instance = this;
    }

    public static ClientController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        Client currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            lblWelcome.setText("Bienvenue, " + currentUser.getNom() + " !");
            refreshBalance();
        }
    }

    public void refreshBalance() {
        Client currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && lblSolde != null) {
            lblSolde.setText(String.format("Solde: %.2f €", currentUser.getSolde()));
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.clearSession();
        loadFullView("/views/auth/Login.fxml", "Connexion", event);
    }

    @FXML
    private void handleViewProfile() {
        loadCenterView("/views/client/ClientProfile.fxml");
    }

    @FXML
    private void handleViewBillets() {
        loadCenterView("/views/billet/ClientBillets.fxml");
    }

    @FXML
    private void handleBookTicket() {
        loadCenterView("/views/evenement/Evenement.fxml");
    }

    public void loadCenterView(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainContainer.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue : " + fxmlPath);
        }
    }

    public void setCenterView(Parent view) {
        if(mainContainer != null) {
            mainContainer.setCenter(view);
        }
    }

    private void loadFullView(String fxmlPath, String title, ActionEvent event) {
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
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
