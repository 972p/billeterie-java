package controllers.prestataire;

import database.MySQLConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Client;
import utils.SessionManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PrestataireDashboardController {

    @FXML private BorderPane mainContainer;
    @FXML private Label lblWelcome;

    private Client currentUser;

    @FXML
    public void initialize() {
        if (!SessionManager.isPrestataire()) {
            System.err.println("Acces refusé : utilisateur n'est pas prestataire.");
            return;
        }

        currentUser = SessionManager.getCurrentUser();
        lblWelcome.setText("Bienvenue, " + currentUser.getNom() + " !");

        Platform.runLater(this::handleViewMissions);
    }

    @FXML
    public void handleViewMissions(ActionEvent event) {
        handleViewMissions();
    }

    public static class MissionDisplay {
        public int id_evenement;
        public int id_service;
        public String titre;
        public String sNom;
        public String sDesc;
        public String date;
        public String lieu;
        public String etat;

        public MissionDisplay(int e, int s, String t, String sn, String sd, String d, String l, String et) {
            id_evenement = e; id_service = s; titre = t; sNom = sn; sDesc = sd; date = d; lieu = l; etat = et;
        }
    }

    private void handleViewMissions() {
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setStyle("-fx-padding: 40;");

        Label title = new Label("Mes Missions Assignées");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 28; -fx-font-weight: bold;");

        ListView<MissionDisplay> missionsList = new ListView<>();
        missionsList.setPrefHeight(500);
        missionsList.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #2b2b3c; -fx-control-inner-background-alt: #2b2b3c;");
        
        Label placeholder = new Label("Aucune mission ne vous a été assignée pour le moment.");
        placeholder.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
        missionsList.setPlaceholder(placeholder);
        
        missionsList.setCellFactory(lv -> new ListCell<MissionDisplay>() {
            @Override
            protected void updateItem(MissionDisplay item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    VBox box = new VBox(10);
                    box.setStyle("-fx-padding: 15; -fx-border-color: #bb86fc; -fx-border-width: 0 0 1 0; -fx-background-color: transparent;");
                    
                    Label text = new Label("Évènement : " + item.titre + "\nDate limite / Représentation : " + item.date + "\nLieu : " + item.lieu + 
                              "\n\n👉 Mission attendue :\n" + item.sNom + " (" + item.sDesc + ")");
                    text.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
                    
                    Label etatLabel = new Label("Statut : " + item.etat);
                    if ("ACCEPTE".equals(item.etat)) {
                        etatLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-font-size: 14;");
                    } else if ("REFUSE".equals(item.etat)) {
                        etatLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-font-size: 14;");
                    } else {
                        etatLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold; -fx-font-size: 14;");
                    }

                    javafx.scene.layout.HBox actions = new javafx.scene.layout.HBox(10);
                    if ("EN_ATTENTE".equals(item.etat) || "EN_ATTENTE".equals(item.etat.toUpperCase())) {
                        javafx.scene.control.Button btnAccept = new javafx.scene.control.Button("Accepter");
                        btnAccept.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand;");
                        btnAccept.setOnAction(e -> updateMissionState(item.id_evenement, item.id_service, "ACCEPTE"));

                        javafx.scene.control.Button btnRefuse = new javafx.scene.control.Button("Refuser");
                        btnRefuse.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");
                        btnRefuse.setOnAction(e -> updateMissionState(item.id_evenement, item.id_service, "REFUSE"));

                        actions.getChildren().addAll(btnAccept, btnRefuse);
                    }
                    
                    box.getChildren().addAll(text, etatLabel, actions);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        ObservableList<MissionDisplay> items = FXCollections.observableArrayList();

        String sql = "SELECT e.id_evenement, s.id_service, e.titre, s.nom as service_nom, s.description, " +
              "IFNULL(es.etat, 'EN_ATTENTE') as etat, " +
              "IFNULL(se.date_heure, 'Non programmée') as date_heure, IFNULL(l.nom, 'Non précisé') as lieu_nom " +
              "FROM Evenement e " +
              "JOIN Evenement_Service es ON e.id_evenement = es.id_evenement " +
              "JOIN Service s ON es.id_service = s.id_service " +
              "LEFT JOIN Seance se ON e.id_evenement = se.id_evenement " +
              "LEFT JOIN Lieu l ON se.id_lieu = l.id_lieu " +
              "WHERE s.id_prestataire = ? " +
              "ORDER BY se.date_heure DESC";

        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, currentUser.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idE = rs.getInt("id_evenement");
                    int idS = rs.getInt("id_service");
                    String titre = rs.getString("titre");
                    String sNom = rs.getString("service_nom");
                    String sDesc = rs.getString("description");
                    String date = rs.getString("date_heure");
                    String lieu = rs.getString("lieu_nom");
                    String etat = rs.getString("etat");
                    
                    items.add(new MissionDisplay(idE, idS, titre, sNom, sDesc, date, lieu, etat));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        missionsList.setItems(items);
        contentBox.getChildren().addAll(title, missionsList);

        mainContainer.setCenter(contentBox);
    }
    
    private void updateMissionState(int idEvt, int idSvc, String newEtat) {
        try {
            DAO.EvenementServiceDAO dao = new DAO.EvenementServiceDAO();
            dao.mettreAJourEtat(idEvt, idSvc, newEtat);
            
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("La mission a été marquée comme : " + newEtat);
            alert.showAndWait();
            
            handleViewMissions(); // Refresh the list
        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Alert err = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            err.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
            err.setTitle("Erreur");
            err.setHeaderText("Erreur lors de la mise à jour");
            err.setContentText(e.getMessage());
            err.showAndWait();
        }
    }

    @FXML
    public void handleManageServices(ActionEvent event) {
        handleManageServices();
    }

    private void handleManageServices() {
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setStyle("-fx-padding: 40;");

        Label title = new Label("Gérer Mes Services");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 28; -fx-font-weight: bold;");

        // Formulaire d'ajout
        VBox formBox = new VBox(10);
        formBox.setStyle("-fx-background-color: #2b2b3c; -fx-padding: 20; -fx-background-radius: 5;");
        Label formTitle = new Label("Ajouter un nouveau service");
        formTitle.setStyle("-fx-text-fill: #bb86fc; -fx-font-size: 18; -fx-font-weight: bold;");
        
        javafx.scene.control.TextField nomField = new javafx.scene.control.TextField();
        nomField.setPromptText("Nom du service (ex: DJ, Traiteur...)");
        nomField.setStyle("-fx-background-color: #3b3b4f; -fx-text-fill: white;");
        
        javafx.scene.control.TextArea descField = new javafx.scene.control.TextArea();
        descField.setPromptText("Description courte...");
        descField.setPrefRowCount(3);
        descField.setStyle("-fx-control-inner-background: #3b3b4f; -fx-text-fill: white;");
        
        javafx.scene.control.Button btnAdd = new javafx.scene.control.Button("Ajouter");
        btnAdd.setStyle("-fx-background-color: #bb86fc; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> {
            if (nomField.getText().trim().isEmpty() || descField.getText().trim().isEmpty()) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
                alert.setTitle("Attention");
                alert.setHeaderText(null);
                alert.setContentText("Veuillez remplir tous les champs.");
                alert.showAndWait();
                return;
            }
            try {
                DAO.ServiceDAO dao = new DAO.ServiceDAO();
                models.Service newS = new models.Service(0, currentUser.getId(), nomField.getText().trim(), descField.getText().trim());
                dao.ajouter(newS);
                handleManageServices(); // Refresh
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        formBox.getChildren().addAll(formTitle, nomField, descField, btnAdd);

        // Liste des services
        Label listTitle = new Label("Mes services actuels");
        listTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");
        
        ListView<models.Service> servicesList = new ListView<>();
        servicesList.setPrefHeight(300);
        servicesList.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #2b2b3c; -fx-control-inner-background-alt: #2b2b3c;");
        
        Label placeholder = new Label("Vous n'avez ajouté aucun service.");
        placeholder.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14;");
        servicesList.setPlaceholder(placeholder);
        
        servicesList.setCellFactory(lv -> new ListCell<models.Service>() {
            @Override
            protected void updateItem(models.Service item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    VBox box = new VBox(5);
                    box.setStyle("-fx-padding: 10; -fx-border-color: #bb86fc; -fx-border-width: 0 0 1 0; -fx-background-color: transparent;");
                    
                    Label nomLabel = new Label(item.getNom());
                    nomLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");
                    
                    Label descLabel = new Label(item.getDescription());
                    descLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14;");
                    descLabel.setWrapText(true);
                    
                    javafx.scene.control.Button btnDelete = new javafx.scene.control.Button("Supprimer");
                    btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");
                    btnDelete.setOnAction(e -> {
                        try {
                            new DAO.ServiceDAO().supprimer(item.getId());
                            handleManageServices(); // Refresh
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            javafx.scene.control.Alert err = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                            err.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
                            err.setTitle("Erreur");
                            err.setHeaderText("Suppression impossible");
                            err.setContentText("Ce service est probablement assigné à un évènement existant.\n" + ex.getMessage());
                            err.showAndWait();
                        }
                    });
                    
                    box.getChildren().addAll(nomLabel, descLabel, btnDelete);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        try {
            java.util.List<models.Service> myList = new DAO.ServiceDAO().trouverParPrestataire(currentUser.getId());
            servicesList.getItems().addAll(myList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        contentBox.getChildren().addAll(title, formBox, listTitle, servicesList);
        mainContainer.setCenter(contentBox);
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        SessionManager.clearSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/auth/Login.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Connexion");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
