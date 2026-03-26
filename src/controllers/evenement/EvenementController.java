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
import models.Lieu;
import models.Salle;
import models.Seance;
import DAO.LieuDAO;
import DAO.SalleDAO;
import DAO.SeanceDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EvenementController {

    // TableView + colonnes
    @FXML
    private TableView<Evenement> event_table;
    @FXML
    private TableColumn<Evenement, String> colTitre;
    @FXML
    private TableColumn<Evenement, String> colTemps;
    @FXML
    private TableColumn<Evenement, String> colDescriptionCourte;
    @FXML
    private TableColumn<Evenement, String> colCategorie;

    // Recherche
    @FXML
    private TextField event_field;
    @FXML
    private ComboBox<String> cbFilterCategorie;

    // Pagination
    @FXML
    private Button prevPage;
    @FXML
    private Button nextPage;
    @FXML
    private Label labelPage;

    // Boutons actions
    @FXML
    private Button reserve_event;
    @FXML
    private Button create_event;
    @FXML
    private Button show_event;
    @FXML
    private Button edit_event;
    @FXML
    private Button delete_event;
    @FXML
    private Button manage_users;
    @FXML
    private Button manage_reservations;
    @FXML
    private Button btn_espace_client;
    @FXML
    private Button btn_logout;
    @FXML
    private javafx.scene.layout.HBox topBar;

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
        colTitre.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitre()));

        colTemps.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDuree() + " min"));

        colDescriptionCourte
                .setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDescriptionCourte()));
        colCategorie.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getCategorie()));

        // Charger la 1ère page
        chargerPage();

        // Recherche dynamique
        cbFilterCategorie.setItems(FXCollections.observableArrayList("Toutes", "Concert", "Théâtre", "Sport", "Festival", "Cinéma", "Autre"));
        cbFilterCategorie.setValue("Toutes");
        event_field.textProperty().addListener((obs, oldValue, newValue) -> filtrerListe(newValue, cbFilterCategorie.getValue()));
        cbFilterCategorie.valueProperty().addListener((obs, oldVal, newVal) -> filtrerListe(event_field.getText(), newVal));

        // Boutons
        reserve_event.setOnAction(e -> ouvrirFenetreReservation());
        create_event.setOnAction(e -> ouvrirFenetreCreation());
        show_event.setOnAction(e -> consulterEvenementSelectionne());
        edit_event.setOnAction(e -> modifierEvenementSelectionne());
        delete_event.setOnAction(e -> supprimerEvenementSelectionne());

        if (utils.SessionManager.isAdmin()) {
            create_event.setVisible(true);
            edit_event.setVisible(true);
            delete_event.setVisible(true);

            manage_users.setVisible(true);
            manage_users.setOnAction(e -> ouvrirGestionUtilisateurs());

            manage_reservations.setVisible(true);
            manage_reservations.setOnAction(e -> ouvrirGestionReservations());

            if (btn_espace_client != null) {
                btn_espace_client.setVisible(false);
                btn_espace_client.setManaged(false);
            }
            // Hide the topBar (logout button inside Evenement.fxml) when inside AdminDashboard
            if (topBar != null && controllers.AdminController.getInstance() != null) {
                topBar.setVisible(false);
                topBar.setManaged(false);
            }
        } else {
            create_event.setVisible(false);
            edit_event.setVisible(false);
            delete_event.setVisible(false);

            manage_users.setVisible(false);
            manage_reservations.setVisible(false);
            
            if (btn_espace_client != null) {
                btn_espace_client.setVisible(true);
                btn_espace_client.setManaged(true);
                btn_espace_client.setOnAction(e -> ouvrirEspaceClient());
            }
            if (topBar != null) {
                topBar.setVisible(false);
                topBar.setManaged(false);
            }
        }
    }

    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        utils.SessionManager.clearSession();
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene()
                    .getWindow();
            stage.setTitle("Connexion");

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            String css = this.getClass().getResource("/views/style.css").toExternalForm();
            scene.getStylesheets().add(css);

            stage.setScene(scene);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            montrerAlerte("Impossible de charger la page de connexion.");
        }
    }

    @FXML
    private void ouvrirEspaceClient() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/views/ClientDashboard.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) btn_espace_client.getScene().getWindow();
            stage.setTitle("Espace Client");

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            String css = this.getClass().getResource("/views/style.css").toExternalForm();
            scene.getStylesheets().add(css);

            stage.setScene(scene);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            montrerAlerte("Impossible de charger la page de connexion.");
        }
    }

    // === Navigation Admin ===
    private void ouvrirGestionUtilisateurs() {
        if (controllers.AdminController.getInstance() != null) {
            controllers.AdminController.getInstance().loadCenterView("/views/AdminUsers.fxml");
        } else {
            try {
                javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/views/AdminUsers.fxml"));
                javafx.stage.Stage stage = (javafx.stage.Stage) manage_users.getScene().getWindow();
                stage.setTitle("Gestion des Utilisateurs");
                javafx.scene.Scene scene = new javafx.scene.Scene(root);
                scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
                stage.setScene(scene); stage.show();
            } catch (java.io.IOException e) { e.printStackTrace(); }
        }
    }

    private void ouvrirGestionReservations() {
        if (controllers.AdminController.getInstance() != null) {
            controllers.AdminController.getInstance().loadCenterView("/views/AdminReservations.fxml");
        } else {
            try {
                javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/views/AdminReservations.fxml"));
                javafx.stage.Stage stage = (javafx.stage.Stage) manage_reservations.getScene().getWindow();
                stage.setTitle("Gestion des Réservations");
                javafx.scene.Scene scene = new javafx.scene.Scene(root);
                scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
                stage.setScene(scene); stage.show();
            } catch (java.io.IOException e) { e.printStackTrace(); }
        }
    }

    // === Pagination ===
    private void chargerPage() {
        try {
            List<Evenement> events = dao.findPaginated(page, LIMIT);
            eventsData.setAll(events);
            event_table.setItems(eventsData);
            labelPage.setText("Page " + page);

            // Désactiver / activer les boutons
            prevPage.setDisable(page <= 1);
            nextPage.setDisable(events.size() < LIMIT); // pas de page suivante si moins que LIMIT
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
    private void filtrerListe(String filtreText, String filtreCat) {
        String lower = filtreText == null ? "" : filtreText.toLowerCase();
        boolean filterByCategory = filtreCat != null && !filtreCat.equals("Toutes");

        ObservableList<Evenement> filtered = eventsData.filtered(e -> {
            boolean matchesName = e.getTitre() != null && e.getTitre().toLowerCase().contains(lower);
            boolean matchesCat = !filterByCategory || (e.getCategorie() != null && e.getCategorie().equals(filtreCat));
            return matchesName && matchesCat;
        });
        event_table.setItems(filtered);
    }

    // === Création ===
    private void ouvrirFenetreCreation() {
        Dialog<Evenement> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        dialog.setTitle("Créer un évènement");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titreField = new TextField();
        TextField descCourteField = new TextField();
        TextField descLongueField = new TextField();
        TextField dureeField = new TextField();
        TextField langueField = new TextField();
        TextField ageMinField = new TextField();
        ComboBox<String> cbCategorie = new ComboBox<>(FXCollections.observableArrayList("Concert", "Théâtre", "Sport", "Festival", "Cinéma", "Autre"));
        cbCategorie.setValue("Autre");

        ComboBox<Lieu> cbLieu = new ComboBox<>();
        ComboBox<Salle> cbSalle = new ComboBox<>();
        cbSalle.setDisable(true); // Disable until Lieu is chosen
        TextField dateHeureField = new TextField();
        dateHeureField.setPromptText("YYYY-MM-DD HH:MM:SS");

        try {
            LieuDAO lieuDAO = new LieuDAO();
            List<Lieu> lieux = lieuDAO.trouverTous();
            cbLieu.setItems(FXCollections.observableArrayList(lieux));
            cbLieu.setConverter(new javafx.util.StringConverter<Lieu>() {
                @Override
                public String toString(Lieu lieu) {
                    return lieu != null ? lieu.getNom() : "";
                }

                @Override
                public Lieu fromString(String string) {
                    return null; // Not needed
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            montrerAlerte("Impossible de charger les lieux.");
        }

        cbLieu.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    SalleDAO salleDAO = new SalleDAO();
                    List<Salle> salles = salleDAO.trouverParLieu(newVal.getId_lieu());
                    cbSalle.setItems(FXCollections.observableArrayList(salles));
                    cbSalle.setConverter(new javafx.util.StringConverter<Salle>() {
                        @Override
                        public String toString(Salle salle) {
                            return salle != null ? salle.getNom() : "";
                        }

                        @Override
                        public Salle fromString(String string) {
                            return null; // Not needed
                        }
                    });
                    cbSalle.setDisable(false);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                cbSalle.setDisable(true);
            }
        });

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
        grid.addRow(6, new Label("Catégorie:"), cbCategorie);
        grid.addRow(7, new Label("Lieu:"), cbLieu);
        grid.addRow(8, new Label("Salle:"), cbSalle);
        grid.addRow(9, new Label("Date & Heure:"), dateHeureField);

        dialog.getDialogPane().setContent(grid);

        var okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(
                Bindings.or(
                        titreField.textProperty().isEmpty(),
                        dureeField.textProperty().isEmpty()));

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    int duree = Integer.parseInt(dureeField.getText());
                    int ageMin = ageMinField.getText().isEmpty()
                            ? 0
                            : Integer.parseInt(ageMinField.getText());

                    Evenement newEv = new Evenement(
                            0,
                            titreField.getText(),
                            descCourteField.getText(),
                            descLongueField.getText(),
                            duree,
                            langueField.getText(),
                            ageMin,
                            cbCategorie.getValue());

                    // Attach extra selections so we can process them after returning
                    return newEv;
                } catch (NumberFormatException e) {
                    montrerAlerte("Durée et âge minimum doivent être des nombres.");
                }
            }
            return null;
        });

        Optional<Evenement> result = dialog.showAndWait();

        result.ifPresent(ev -> {
            try {
                // 1. Create the Event
                dao.ajouter(ev);

                // We need the new event ID. EvenementDAO.ajouter must set the ID if we use
                // RETURN_GENERATED_KEYS.
                // For now, let's assume dao.ajouter sets it or we re-fetch it. If cbLieu and
                // cbSalle are set:
                if (cbLieu.getValue() != null && cbSalle.getValue() != null && !dateHeureField.getText().isEmpty()) {
                    // Re-fetch the newly created event to get its ID using title (hacky, but works
                    // if titles are unique or latest)
                    int generatedEventId = ev.getId();
                    if (generatedEventId == 0) {
                        try (java.sql.Connection conn = database.MySQLConnection.connect();
                                java.sql.PreparedStatement ps = conn.prepareStatement(
                                        "SELECT id_evenement FROM Evenement ORDER BY id_evenement DESC LIMIT 1")) {
                            java.sql.ResultSet rs = ps.executeQuery();
                            if (rs.next()) {
                                generatedEventId = rs.getInt(1);
                            }
                        }
                    }

                    // 2. Create the Seance
                    SeanceDAO seanceDAO = new SeanceDAO();
                    Seance newSeance = new Seance(0, generatedEventId, cbLieu.getValue().getId_lieu(),
                            cbSalle.getValue().getId_salle(), dateHeureField.getText());
                    seanceDAO.ajouter(newSeance);
                }

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

        String details = "Titre : " + selected.getTitre() + "\n" +
                "Description courte : " + selected.getDescriptionCourte() + "\n" +
                "Description longue : " + selected.getDescriptionLongue() + "\n" +
                "Durée : " + selected.getDuree() + " min\n" +
                "Langue : " + selected.getLangue() + "\n" +
                "Âge minimum : " + selected.getAgeMin() + " ans\n";

        try (java.sql.Connection conn = database.MySQLConnection.connect();
                java.sql.PreparedStatement ps = conn.prepareStatement(
                        "SELECT l.nom AS lieu_nom, sa.nom AS salle_nom, s.date_heure " +
                                "FROM Seance s " +
                                "JOIN Lieu l ON s.id_lieu = l.id_lieu " +
                                "JOIN Salle sa ON s.id_salle = sa.id_salle " +
                                "WHERE s.id_evenement = ? LIMIT 1")) {

            ps.setInt(1, selected.getId());
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                details += "\n-- Emplacement & Date --\n" +
                        "Lieu : " + rs.getString("lieu_nom") + "\n" +
                        "Salle : " + rs.getString("salle_nom") + "\n" +
                        "Date & Heure : " + rs.getString("date_heure") + "\n";
            } else {
                details += "\n-- Emplacement & Date --\n" +
                        "Aucune séance programmée.\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        alert.setTitle("Détail de l'évènement");
        alert.setHeaderText(selected.getTitre());
        alert.setContentText(details);
        alert.showAndWait();
    }

    // === Modification ===
    private void modifierEvenementSelectionne() {
        Evenement selected = event_table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            montrerAlerte("Veuillez sélectionner un évènement à modifier.");
            return;
        }

        Dialog<Evenement> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        dialog.setTitle("Modifier un évènement");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titreField = new TextField(selected.getTitre());
        TextField descCourteField = new TextField(selected.getDescriptionCourte());
        TextField descLongueField = new TextField(selected.getDescriptionLongue());
        TextField dureeField = new TextField(String.valueOf(selected.getDuree()));
        TextField langueField = new TextField(selected.getLangue());
        TextField ageMinField = new TextField(String.valueOf(selected.getAgeMin()));
        ComboBox<String> cbCategorie = new ComboBox<>(FXCollections.observableArrayList("Concert", "Théâtre", "Sport", "Festival", "Cinéma", "Autre"));
        cbCategorie.setValue(selected.getCategorie() != null ? selected.getCategorie() : "Autre");

        ComboBox<Lieu> cbLieu = new ComboBox<>();
        ComboBox<Salle> cbSalle = new ComboBox<>();
        cbSalle.setDisable(true);
        TextField dateHeureField = new TextField();
        dateHeureField.setPromptText("YYYY-MM-DD HH:MM:SS");

        // Reference variables to hold current Seance details
        final int[] currentSeanceId = { -1 };

        try {
            LieuDAO lieuDAO = new LieuDAO();
            List<Lieu> lieux = lieuDAO.trouverTous();
            cbLieu.setItems(FXCollections.observableArrayList(lieux));
            cbLieu.setConverter(new javafx.util.StringConverter<Lieu>() {
                @Override
                public String toString(Lieu lieu) {
                    return lieu != null ? lieu.getNom() : "";
                }

                @Override
                public Lieu fromString(String string) {
                    return null;
                }
            });

            // Try to find the existing Seance
            try (java.sql.Connection conn = database.MySQLConnection.connect();
                    java.sql.PreparedStatement ps = conn.prepareStatement(
                            "SELECT s.id_seance, l.id_lieu, sa.id_salle, s.date_heure " +
                                    "FROM Seance s " +
                                    "JOIN Lieu l ON s.id_lieu = l.id_lieu " +
                                    "JOIN Salle sa ON s.id_salle = sa.id_salle " +
                                    "WHERE s.id_evenement = ? LIMIT 1")) {
                ps.setInt(1, selected.getId());
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    currentSeanceId[0] = rs.getInt("id_seance");
                    int existingLieuId = rs.getInt("id_lieu");
                    int existingSalleId = rs.getInt("id_salle");
                    dateHeureField.setText(rs.getString("date_heure"));

                    // Pre-select Lieu
                    for (Lieu l : lieux) {
                        if (l.getId_lieu() == existingLieuId) {
                            cbLieu.getSelectionModel().select(l);
                            break;
                        }
                    }

                    // Manually trigger Salle fetch for the pre-selected Lieu
                    if (cbLieu.getValue() != null) {
                        SalleDAO salleDAO = new SalleDAO();
                        List<Salle> salles = salleDAO.trouverParLieu(cbLieu.getValue().getId_lieu());
                        cbSalle.setItems(FXCollections.observableArrayList(salles));
                        cbSalle.setConverter(new javafx.util.StringConverter<Salle>() {
                            @Override
                            public String toString(Salle salle) {
                                return salle != null ? salle.getNom() : "";
                            }

                            @Override
                            public Salle fromString(String string) {
                                return null;
                            }
                        });
                        cbSalle.setDisable(false);

                        // Pre-select Salle
                        for (Salle s : salles) {
                            if (s.getId_salle() == existingSalleId) {
                                cbSalle.getSelectionModel().select(s);
                                break;
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        cbLieu.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    SalleDAO salleDAO2 = new SalleDAO();
                    List<Salle> salles = salleDAO2.trouverParLieu(newVal.getId_lieu());
                    cbSalle.setItems(FXCollections.observableArrayList(salles));
                    cbSalle.setConverter(new javafx.util.StringConverter<Salle>() {
                        @Override
                        public String toString(Salle salle) {
                            return salle != null ? salle.getNom() : "";
                        }

                        @Override
                        public Salle fromString(String string) {
                            return null;
                        }
                    });
                    cbSalle.setDisable(false);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                cbSalle.setDisable(true);
            }
        });

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
        grid.addRow(6, new Label("Catégorie:"), cbCategorie);
        grid.addRow(7, new Label("Lieu:"), cbLieu);
        grid.addRow(8, new Label("Salle:"), cbSalle);
        grid.addRow(9, new Label("Date & Heure:"), dateHeureField);

        dialog.getDialogPane().setContent(grid);

        var okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(
                Bindings.or(
                        titreField.textProperty().isEmpty(),
                        dureeField.textProperty().isEmpty()));

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    int duree = Integer.parseInt(dureeField.getText());
                    int ageMin = ageMinField.getText().isEmpty()
                            ? 0
                            : Integer.parseInt(ageMinField.getText());

                    Evenement editedEv = new Evenement(
                            selected.getId(),
                            titreField.getText(),
                            descCourteField.getText(),
                            descLongueField.getText(),
                            duree,
                            langueField.getText(),
                            ageMin,
                            cbCategorie.getValue());

                    return editedEv;
                } catch (NumberFormatException e) {
                    montrerAlerte("Durée et âge minimum doivent être des nombres.");
                }
            }
            return null;
        });

        Optional<Evenement> result = dialog.showAndWait();

        result.ifPresent(ev -> {
            try {
                dao.modifier(ev);

                // Also update or create the Seance
                if (cbLieu.getValue() != null && cbSalle.getValue() != null && !dateHeureField.getText().isEmpty()) {
                    SeanceDAO seanceDAO = new SeanceDAO();
                    if (currentSeanceId[0] != -1) {
                        // Update existing
                        Seance updatedSeance = new Seance(currentSeanceId[0], ev.getId(),
                                cbLieu.getValue().getId_lieu(), cbSalle.getValue().getId_salle(),
                                dateHeureField.getText());
                        seanceDAO.modifier(updatedSeance);
                    } else {
                        // Create new if there wasn't one
                        Seance newSeance = new Seance(0, ev.getId(), cbLieu.getValue().getId_lieu(),
                                cbSalle.getValue().getId_salle(), dateHeureField.getText());
                        seanceDAO.ajouter(newSeance);
                    }
                }

                chargerPage();
            } catch (Exception e) {
                e.printStackTrace();
                montrerAlerte("Erreur lors de la modification de l'évènement.");
            }
        });
    }

    // === Réservation ===
    private void ouvrirFenetreReservation() {
        Evenement selected = event_table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            montrerAlerte("Veuillez sélectionner un évènement pour réserver.");
            return;
        }

        try {
            // Check if a seance exists for this event
            int idSeance = -1;
            int idSalle = -1; // Assuming we have to find out which room the event/seance takes place in

            try (java.sql.Connection conn = database.MySQLConnection.connect();
                    java.sql.PreparedStatement ps = conn.prepareStatement(
                            "SELECT s.id_seance, l.id_lieu, sa.id_salle " +
                                    "FROM Seance s " +
                                    "JOIN Lieu l ON s.id_lieu = l.id_lieu " +
                                    "JOIN Salle sa ON s.id_salle = sa.id_salle " +
                                    "WHERE s.id_evenement = ? LIMIT 1")) {

                ps.setInt(1, selected.getId());
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    idSeance = rs.getInt("id_seance");
                    idSalle = rs.getInt("id_salle");
                }
            }

            if (idSeance == -1 || idSalle == -1) {
                montrerAlerte(
                        "Erreur: Aucune séance ou salle n'est configurée pour cet évènement. Revenez plus tard !");
                return;
            }

            // Open the seat selection view
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/views/evenement/SeatSelection.fxml"));
            javafx.scene.Parent root = loader.load();

            SeatSelectionController controller = loader.getController();
            controller.initData(selected.getId(), idSeance, idSalle);

            if (!utils.SessionManager.isAdmin() && controllers.ClientController.getInstance() != null) {
                controllers.ClientController.getInstance().setCenterView(root);
            } else {
                javafx.stage.Stage stage = (javafx.stage.Stage) reserve_event.getScene().getWindow();
                stage.setTitle("Sélection des places");

                javafx.scene.Scene scene = new javafx.scene.Scene(root);
                String css = this.getClass().getResource("/views/style.css").toExternalForm();
                scene.getStylesheets().add(css);

                stage.setScene(scene);
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            montrerAlerte("Erreur lors de l'ouverture du plan de réservation.");
        }
    }

    // === Suppression ===
    private void supprimerEvenementSelectionne() {
        Evenement selected = event_table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            montrerAlerte("Veuillez sélectionner un évènement à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        confirm.setTitle("Supprimer");
        confirm.setHeaderText("Supprimer cet évènement ?");
        confirm.setContentText(selected.getTitre());
        Optional<ButtonType> res = confirm.showAndWait();

        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                dao.supprimer(selected.getId()); // suppression en base

                // Si la page actuelle n’a plus qu’un élément (celui qu’on supprime)
                // et qu’on n’est pas en page 1, on recule d’une page
                if (eventsData.size() == 1 && page > 1) {
                    page--;
                }

                chargerPage(); // on recharge la page depuis la base
            } catch (Exception e) {
                e.printStackTrace();
                montrerAlerte("Erreur lors de la suppression de l'évènement.");
            }
        }

    }

    // === Utilitaires ===
    private void montrerAlerte(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
