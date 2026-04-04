package controllers.auth;

import DAO.ClientDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Client;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.prefs.Preferences;

public class LoginController {

    private static final Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
    private static final String PREF_EMAIL = "saved_email";
    private static final String PREF_PASSWORD = "saved_password";
    private static final String PREF_REMEMBER = "remember_me";

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private CheckBox chkRemember;

    @FXML
    public void initialize() {
        // Auto-fill if saved
        if (prefs.getBoolean(PREF_REMEMBER, false)) {
            txtEmail.setText(prefs.get(PREF_EMAIL, ""));
            txtPassword.setText(prefs.get(PREF_PASSWORD, ""));
            chkRemember.setSelected(true);
        }
    }

    public void onLoginClick(ActionEvent event) {
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            ClientDAO clientDAO = new ClientDAO();
            Client user = clientDAO.authentifier(email, password);

            if (user != null) {
                SessionManager.setCurrentUser(user);

                // Save credentials if "rester connecté" is checked
                if (chkRemember.isSelected()) {
                    prefs.put(PREF_EMAIL, email);
                    prefs.put(PREF_PASSWORD, password);
                    prefs.putBoolean(PREF_REMEMBER, true);
                } else {
                    prefs.remove(PREF_EMAIL);
                    prefs.remove(PREF_PASSWORD);
                    prefs.putBoolean(PREF_REMEMBER, false);
                }

                if (SessionManager.isAdmin()) {
                    loadView("/views/admin/AdminDashboard.fxml", "Tableau de Bord Administrateur", event);
                } else {
                    loadView("/views/client/ClientDashboard.fxml", "Espace Client", event);
                }
            } else {
                DAO.PrestataireDAO prestataireDAO = new DAO.PrestataireDAO();
                models.Prestataire prestataire = prestataireDAO.authentifier(email, password);
                
                if (prestataire != null) {
                    Client prestataireUser = new Client(prestataire.getId(), prestataire.getNom(), email, prestataire.getContact(), "", prestataire.getMotDePasse(), "PRESTATAIRE");
                    SessionManager.setCurrentUser(prestataireUser);

                    if (chkRemember.isSelected()) {
                        prefs.put(PREF_EMAIL, email);
                        prefs.put(PREF_PASSWORD, password);
                        prefs.putBoolean(PREF_REMEMBER, true);
                    } else {
                        prefs.remove(PREF_EMAIL);
                        prefs.remove(PREF_PASSWORD);
                        prefs.putBoolean(PREF_REMEMBER, false);
                    }

                    loadView("/views/prestataire/PrestataireDashboard.fxml", "Espace Prestataire", event);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur de connexion", "Email ou mot de passe incorrect.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données", "Impossible de se connecter.");
        }
    }

    public void onSignupClick(ActionEvent event) {
        loadView("/views/auth/Signup.fxml", "Créer un compte", event);
    }

    private void loadView(String fxmlPath, String title, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page demandée.");
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

