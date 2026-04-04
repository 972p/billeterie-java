package controllers.auth;

import DAO.ClientDAO;
import models.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class SignupController {

    @FXML
    private TextField nomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField adresseField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;

    private ClientDAO clientDAO = new ClientDAO();

    @FXML
    public void handleSignup(ActionEvent event) {
        errorLabel.setVisible(false);

        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String adresse = adresseField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (nom.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Veuillez remplir tous les champs obligatoires (*).");
            return;
        }

        // Regex Validation
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (!email.matches(emailRegex)) {
            showError("Format d'email invalide.");
            return;
        }

        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";
        if (!password.matches(passwordRegex)) {
            showError("Le mot de passe doit contenir au moins 8 caractères, dont une lettre et un chiffre.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            // public Client(int id, String nom, String email, String telephone, String
            // adresse, String motDePasse, String role)
            Client newClient = new Client(0, nom, email, telephone, adresse, password, "USER");
            clientDAO.ajouter(newClient);

            // Send Welcome Email
            utils.EmailSender.sendWelcomeEmail(email, nom);

            // Redirection vers le login après succès
            goToLogin(event);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la création du compte. Cet email est peut-être déjà utilisé.");
        }
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/auth/Login.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Connexion");

            Scene scene = new Scene(root, 400, 400);
            String css = this.getClass().getResource("/views/style.css").toExternalForm();
            scene.getStylesheets().add(css);

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de la navigation vers la page de connexion.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
