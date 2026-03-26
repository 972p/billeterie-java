package controllers.billet;

import DAO.BilletDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.BilletDisplay;
import models.Client;
import utils.PdfGenerator;
import utils.SessionManager;
import java.io.File;
import javafx.stage.Stage;
import models.BilletDisplay;
import models.Client;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class BilletController {

    @FXML
    private TableView<BilletDisplay> tableBillets;

    @FXML
    private TableColumn<BilletDisplay, String> colEvent;

    @FXML
    private TableColumn<BilletDisplay, String> colDate;

    @FXML
    private TableColumn<BilletDisplay, String> colTime;

    @FXML
    private TableColumn<BilletDisplay, String> colLieu;

    @FXML
    private TableColumn<BilletDisplay, String> colSalle;

    @FXML
    private TableColumn<BilletDisplay, String> colSiege;

    @FXML
    private TableColumn<BilletDisplay, Double> colPrix;

    @FXML
    public void initialize() {
        colEvent.setCellValueFactory(new PropertyValueFactory<>("nom_evenement"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_seance"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("heure_seance"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("nom_lieu"));
        colSalle.setCellValueFactory(new PropertyValueFactory<>("nom_salle"));
        colSiege.setCellValueFactory(new PropertyValueFactory<>("siegeComplet"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));

        loadBillets();
    }

    private void loadBillets() {
        Client currentUser = SessionManager.getCurrentUser();
        if (currentUser == null)
            return;

        try {
            BilletDAO billetDAO = new BilletDAO();
            List<BilletDisplay> billets = billetDAO.getBilletsDetailedByClient(currentUser.getId());
            ObservableList<BilletDisplay> observableBillets = FXCollections.observableArrayList(billets);
            tableBillets.setItems(observableBillets);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger vos billets.");
        }
    }

    @FXML
    private void handleDownloadPdf(ActionEvent event) {
        BilletDisplay selected = tableBillets.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un billet dans la liste.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le Billet PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Billet_" + selected.getNom_evenement().replaceAll("\\s+", "_") + ".pdf");
        
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                PdfGenerator.generateBilletPdf(selected, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Le billet a été enregistré avec succès.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la création du PDF.");
            }
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
