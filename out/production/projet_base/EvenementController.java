package controllers.evenement;

import DAO.EvenementDAO;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import models.Evenement;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EvenementController {

    // TableView + colonnes
    @FXML private TableView<Evenement> event_table;
    @FXML private TableColumn<Evenement, String> colTitre;
    @FXML private TableColumn<Evenement, String> colTemps;
    @FXML private TableColumn<Evenement, String> colDescriptionCourte;

    // Recherche
    @FXML private TextField event_field;

    // Pagination
    @FXML private Button prevPage;
    @FXML private Button nextPage;
    @FXML private Label labelPage;

    // Boutons actions
    @FXML private Button create_event;
    @FXML private Button show_event;
    @FXML private Button delete_event;

    // DAO
    private final EvenementDAO dao = new EvenementDAO();

    // Pagination
    private int page = 1;
    private final int LIMIT = 10;

    // Liste observable pour la TableView
    private final ObservableList<Evenement> eventsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Colonnes
        colTitre.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getTitre()));

        colTemps.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getDuree() + " min"));

        colDescriptionCourte.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getDescriptionCourte()));

        // Charger la 1ère page
        chargerPage();

        // Recherche dynamique
        event_field.textProperty().addListener((obs, oldValue, newValue) ->
                filtrerListe(newValue)
        );

        // Boutons
        create_event.setOnAction(e -> ouvrirFenetreCreation());
        show_event.setOnAction(e -> consulterEvenementSelectionne());
        delete_event.setOnAction(e -> supprimerEvenementSelectionne());
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

    // === Création ===
    private void ouvrirFenetreCreation() {
        Dialog<Evenement> dialog = new Dialog<>();
        dialog.setTitle("Créer un évènement");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titreField = new TextField();
        TextField descCourteField = new TextField();
        TextField descLongueField = new TextField();
        TextField dureeField = new TextField();
        TextField langueField = new TextField();
        TextField ageMinField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.addRow(0, new Label("Titre:"), titreField);
        grid.addRow(1, new Label("Description courte:"), descCourteField);
        grid.addRow(2, new Label("Description longue:"), descLongueField);
        grid.addRow(3, new Label("Durée (min):"), dureeField);
        grid.addRow(4, new Label("Langue:"), langueField);
        grid.addRow(5, new Label("Âge minimum:"), ageMinField);

        dialog.getDialogPane().setContent(grid);

        var okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(
                Bindings.or(
                        titreField.textProperty().isEmpty(),
                        dureeField.textProperty().isEmpty()
                )
        );

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    int duree = Integer.parseInt(dureeField.getText());
                    int ageMin = ageMinField.getText().isEmpty()
                            ? 0
                            : Integer.parseInt(ageMinField.getText());

                    // id = 0, la base génère l'AUTO_INCREMENT
                    return new Evenement(
                            0,
                            titreField.getText(),
                            descCourteField.getText(),
                            descLongueField.getText(),
                            duree,
                            langueField.getText(),
                            ageMin
                    );
                } catch (NumberFormatException e) {
                    montrerAlerte("Durée et âge minimum doivent être des nombres.");
                }
            }
            return null;
        });

        Optional<Evenement> result = dialog.showAndWait();

        result.ifPresent(ev -> {
            try {
                dao.ajouter(ev);
                // Recharge la page pour récupérer les ids réels
                chargerPage();
            } catch (Exception e) {
                e.printStackTrace();
                montrerAlerte("Erreur lors de la création de l'évènement.");
            }
        });
    }

    // === Consultation ===
    private void consulterEvenementSelectionne() {
        Evenement selected = event_table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            montrerAlerte("Veuillez sélectionner un évènement.");
            return;
        }

        String details =
                "Titre : " + selected.getTitre() + "\n" +
                        "Description courte : " + selected.getDescriptionCourte() + "\n" +
                        "Description longue : " + selected.getDescriptionLongue() + "\n" +
                        "Durée : " + selected.getDuree() + " min\n" +
                        "Langue : " + selected.getLangue() + "\n" +
                        "Âge minimum : " + selected.getAgeMin() + " ans";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détail de l'évènement");
        alert.setHeaderText(selected.getTitre());
        alert.setContentText(details);
        alert.showAndWait();
    }

    // === Suppression ===
    private void supprimerEvenementSelectionne() {
        Evenement selected = event_table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            montrerAlerte("Veuillez sélectionner un évènement à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer");
        confirm.setHeaderText("Supprimer cet évènement ?");
        confirm.setContentText(selected.getTitre());
        Optional<ButtonType> res = confirm.showAndWait();

        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                dao.supprimer(selected.getId());   // suppression en base
                eventsData.remove(selected);       // suppression dans la liste
            } catch (Exception e) {
                e.printStackTrace();
                montrerAlerte("Erreur lors de la suppression de l'évènement.");
            }
        }
    }

    // === Utilitaires ===
    private void montrerAlerte(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
