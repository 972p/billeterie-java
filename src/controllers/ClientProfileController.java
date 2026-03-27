package controllers;

import DAO.ClientDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Client;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class ClientProfileController {

    @FXML
    private ImageView profileImageView;

    @FXML
    private Label lblNom;

    @FXML
    private Label lblEmail;

    @FXML
    private Label lblTelephone;

    @FXML
    private Label lblAdresse;

    @FXML
    private Label lblSolde;

    private ClientDAO clientDAO = new ClientDAO();

    @FXML
    public void initialize() {
        chargerInfosActuelles();
    }

    private void chargerInfosActuelles() {
        Client currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            lblNom.setText(currentUser.getNom());
            lblEmail.setText(currentUser.getEmail());
            lblTelephone.setText(currentUser.getTelephone());
            lblAdresse.setText(currentUser.getAdresse());
            lblSolde.setText(String.format("%.2f €", currentUser.getSolde()));

            // Charger l'image si elle existe
            String photoPath = currentUser.getPhotoProfil();
            if (photoPath != null && !photoPath.isEmpty()) {
                try {
                    File file = new File(photoPath);
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString());
                        profileImageView.setImage(image);
                    }
                } catch (Exception e) {
                    System.out.println("Impossible de charger la photo : " + photoPath);
                }
            } else {
                // Mettre une image par défaut si aucune photo n'existe
                try {
                    String defaultIcon = getClass().getResource("/views/default_avatar.png").toExternalForm();
                    profileImageView.setImage(new Image(defaultIcon));
                } catch (Exception e) {
                    System.out.println("Image par défaut introuvable, laisser vide.");
                }
            }
        }
    }

    @FXML
    private void handleChangerPhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // S'assurer que le dossier "ressource/uploads/" existe
                File uploadDir = new File("ressource/uploads");
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // Copier l'image sélectionnée vers notre dossier
                File destFile = new File(
                        uploadDir.getAbsolutePath() + "/" + System.currentTimeMillis() + "_" + selectedFile.getName());
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Mettre à jour l'utilisateur en mémoire
                Client currentUser = SessionManager.getCurrentUser();
                currentUser.setPhotoProfil(destFile.getAbsolutePath());

                // Mettre à jour en BDD
                clientDAO.modifier(currentUser);

                // Recharger l'affichage
                chargerInfosActuelles();

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Photo de profil mise à jour !");
            } catch (IOException | SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder l'image.");
            }
        }
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/ClientDashboard.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Espace Client");

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
        alert.showAndWait();
    }
}
