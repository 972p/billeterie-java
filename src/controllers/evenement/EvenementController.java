package controllers.evenement;

import DAO.EvenementDAO;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Evenement;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class EvenementController {

    // TableView + colonne titre (adaptée à ton modèle Evenement)
    @FXML private TableView<Evenement> event_table;
    @FXML private TableColumn<Evenement, String> colTitre;

    // Recherche
    @FXML private TextField event_field;

    // Pagination
    @FXML private Button prevPage;
    @FXML private Button nextPage;
    @FXML private Label labelPage;

    // Boutons actions
    @FXML private Button create_event;
    @FXML private Button show_event;

    // DAO
    private final EvenementDAO dao = new EvenementDAO();

    // Pagination
    private int page = 1;
    private final int LIMIT = 10;

    // Liste observable pour la TableView
    private final ObservableList<Evenement> eventsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Colonne titre -> utilise le getter getTitre() de Evenement
        colTitre.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getTitre()));

        // Chargement de la 1ère page
        chargerPage();

        // Recherche dynamique
        event_field.textProperty().addListener((obs, oldValue, newValue) ->
                filtrerListe(newValue)
        );

        // Bouton "Créer"
        create_event.setOnAction(e -> ouvrirFenetre("views/evenement_create.fxml"));

        // Bouton "Consulter"
        show_event.setOnAction(e -> consulterEvenementSelectionne());
    }

    // === Pagination ===
    private void chargerPage() {
        try {
            List<Evenement> events = dao.findPaginated(page, LIMIT);
            eventsData.setAll(events);
            event_table.setItems(eventsData);
            labelPage.setText("Page " + page);
        } catch (SQLException e) {
            e.printStackTrace();
            montrerAlerte("Erreur lors du chargement des évènements.");
        }
    }

    @FXML
    private void nextPage() {
        page++;
        chargerPage();
    }

    @FXML
    private void prevPage() {
        if (page > 1) {
            page--;
            chargerPage();
        }
    }

    // === Recherche ===
    private void filtrerListe(String filtre) {
        if (filtre == null || filtre.isEmpty()) {
            event_table.setItems(eventsData);
            return;
        }

        String lower = filtre.toLowerCase();
        ObservableList<Evenement> filtered = eventsData.filtered(
                e -> e.getTitre() != null &&
                        e.getTitre().toLowerCase().contains(lower)
        );
        event_table.setItems(filtered);
    }

    // === Navigation ===
    private void ouvrirFenetre(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + fxmlPath));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            montrerAlerte("Impossible d'ouvrir la fenêtre : " + fxmlPath);
        }
    }

    private void consulterEvenementSelectionne() {
        Evenement selected = event_table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            montrerAlerte("Veuillez sélectionner un évènement.");
            return;
        }

        // Ici tu pourras ouvrir un écran de détail en lui passant 'selected'
        System.out.println("Evenement sélectionné : " + selected.getTitre());
    }

    // === Utilitaires ===
    private void montrerAlerte(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
