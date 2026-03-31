package controllers;

import DAO.PrestataireDAO;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import models.Prestataire;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AdminPrestatairesController {

    @FXML private TableView<Prestataire> prestataires_table;
    @FXML private TableColumn<Prestataire, String> colNom;
    @FXML private TableColumn<Prestataire, String> colSpecialite;
    @FXML private TableColumn<Prestataire, String> colContact;
    @FXML private TableColumn<Prestataire, String> colEmail;
    @FXML private TableColumn<Prestataire, Void> colActions;

    private PrestataireDAO prestataireDAO = new PrestataireDAO();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNom()));
        colSpecialite.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getSpecialite()));
        colContact.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getContact()));
        colEmail.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEmail()));

        setupActionColumn();
        chargerPrestataires();
    }

    private void chargerPrestataires() {
        try {
            List<Prestataire> list = prestataireDAO.trouverTous();
            ObservableList<Prestataire> data = FXCollections.observableArrayList(list);
            prestataires_table.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            montrerAlerte("Erreur", "Impossible de charger les prestataires.");
        }
    }

    private void setupActionColumn() {
        Callback<TableColumn<Prestataire, Void>, TableCell<Prestataire, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Prestataire, Void> call(final TableColumn<Prestataire, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Modifier");
                    private final Button btnDelete = new Button("Supprimer");
                    private final HBox pane = new HBox(10, btnEdit, btnDelete);

                    {
                        btnEdit.setStyle("-fx-background-color: #bb86fc; -fx-text-fill: #1e1e2e; -fx-cursor: hand;");
                        btnEdit.setOnAction(event -> handleModifier(getTableView().getItems().get(getIndex())));

                        btnDelete.setStyle("-fx-background-color: #ff4c4c; -fx-text-fill: white; -fx-cursor: hand;");
                        btnDelete.setOnAction(event -> handleSupprimer(getTableView().getItems().get(getIndex())));
                        pane.setStyle("-fx-alignment: CENTER;");
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    @FXML
    private void handleAjouter() {
        ouvrirDialogue(null);
    }

    private void handleModifier(Prestataire p) {
        ouvrirDialogue(p);
    }

    private void handleSupprimer(Prestataire p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le prestataire : " + p.getNom());
        alert.setContentText("Êtes-vous sûr ? Cette action supprimera également ses services.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    prestataireDAO.supprimer(p.getId());
                    chargerPrestataires();
                } catch (SQLException e) {
                    montrerAlerte("Erreur", "Erreur lors de la suppression.");
                }
            }
        });
    }

    private void ouvrirDialogue(Prestataire existing) {
        Dialog<Prestataire> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter un Prestataire" : "Modifier un Prestataire");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nom = new TextField(existing != null ? existing.getNom() : "");
        TextField spec = new TextField(existing != null ? existing.getSpecialite() : "");
        TextField contact = new TextField(existing != null ? existing.getContact() : "");
        TextField email = new TextField(existing != null ? existing.getEmail() : "");
        PasswordField pass = new PasswordField();
        pass.setPromptText(existing == null ? "Mot de passe" : "Laisser vide pour ne pas changer");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nom, 1, 0);
        grid.add(new Label("Spécialité:"), 0, 1);
        grid.add(spec, 1, 1);
        grid.add(new Label("Contact:"), 0, 2);
        grid.add(contact, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(email, 1, 3);
        grid.add(new Label("Mot de passe:"), 0, 4);
        grid.add(pass, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Prestataire(
                    existing != null ? existing.getId() : 0,
                    nom.getText(),
                    spec.getText(),
                    contact.getText(),
                    email.getText(),
                    pass.getText()
                );
            }
            return null;
        });

        Optional<Prestataire> result = dialog.showAndWait();
        result.ifPresent(p -> {
            try {
                if (existing == null) {
                    prestataireDAO.ajouter(p);
                } else {
                    prestataireDAO.modifier(p);
                }
                chargerPrestataires();
            } catch (SQLException e) {
                montrerAlerte("Erreur DB", "Erreur lors de l'enregistrement : " + e.getMessage());
            }
        });
    }

    private void montrerAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
