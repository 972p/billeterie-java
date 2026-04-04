package controllers.admin;

import DAO.ClientDAO;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import models.Client;
import java.io.IOException;
import java.util.List;

public class AdminUsersController {

    @FXML
    private TableView<Client> users_table;
    @FXML
    private TableColumn<Client, String> colNom;
    @FXML
    private TableColumn<Client, String> colEmail;
    @FXML
    private TableColumn<Client, String> colRole;
    @FXML
    private TableColumn<Client, Void> colAction;


    private ClientDAO clientDAO = new ClientDAO();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNom()));
        colEmail.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEmail()));
        colRole.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getRole()));

        setupActionColumn();

        chargerUtilisateurs();
    }

    private void chargerUtilisateurs() {
        try {
            // Re-using findPaginated with a large limit or we could add a `findAll()` to
            // ClientDAO
            List<Client> clients = clientDAO.findPaginated(1, 1000);
            ObservableList<Client> data = FXCollections.observableArrayList(clients);
            users_table.setItems(data);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
            alert.setContentText("Impossible de charger les utilisateurs.");
            alert.show();
        }
    }

    private void setupActionColumn() {
        Callback<TableColumn<Client, Void>, TableCell<Client, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Client, Void> call(final TableColumn<Client, Void> param) {
                final TableCell<Client, Void> cell = new TableCell<>() {
                    private final Button btnDelete = new Button("Désactiver");

                    {
                        btnDelete.setStyle("-fx-background-color: #ff4c4c; -fx-text-fill: white; -fx-cursor: hand;");
                        btnDelete.setOnAction(event -> {
                            Client client = getTableView().getItems().get(getIndex());
                            supprimerUtilisateur(client);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Client client = getTableView().getItems().get(getIndex());
                            // Dont let admin delete themselves
                            if ("ADMIN".equals(client.getRole())) {
                                btnDelete.setDisable(true);
                            } else {
                                btnDelete.setDisable(false);
                            }
                            HBox managebtn = new HBox(btnDelete);
                            managebtn.setStyle("-fx-alignment: center");
                            setGraphic(managebtn);
                        }
                    }
                };
                return cell;
            }
        };
        colAction.setCellFactory(cellFactory);
    }

    private void supprimerUtilisateur(Client client) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'utilisateur : " + client.getEmail());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce compte ? Cette action est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    clientDAO.supprimer(client.getId());
                    chargerUtilisateurs(); // Refresh the table
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
                    errorAlert.setContentText("Erreur lors de la suppression de l'utilisateur.");
                    errorAlert.show();
                }
            }
        });
    }

}
