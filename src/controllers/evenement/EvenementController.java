package controllers.evenement;

import DAO.EvenementDAO;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Evenement;
import models.Lieu;
import models.Salle;
import DAO.LieuDAO;
import DAO.SalleDAO;
import DAO.SeanceDAO;
import DAO.PrestataireDAO;
import DAO.ServiceDAO;
import DAO.EvenementServiceDAO;
import models.Prestataire;
import models.Service;
import models.Seance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EvenementController {

    // TableView + colonnes
    @FXML
    private javafx.scene.layout.FlowPane event_container;

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
        // Charger la 1ère page
        chargerPage();

        // Recherche dynamique
        cbFilterCategorie.setItems(FXCollections.observableArrayList("Toutes", "Concert", "Théâtre", "Sport",
                "Festival", "Cinéma", "Autre"));
        cbFilterCategorie.setValue("Toutes");
        event_field.textProperty()
                .addListener((obs, oldValue, newValue) -> filtrerListe(newValue, cbFilterCategorie.getValue()));
        cbFilterCategorie.valueProperty()
                .addListener((obs, oldVal, newVal) -> filtrerListe(event_field.getText(), newVal));

        // Boutons - these will now show alerts as they need a card selection
        reserve_event.setOnAction(e -> montrerAlerte("Veuillez cliquer sur 'Réserver' sur une carte."));
        show_event.setOnAction(e -> montrerAlerte("Veuillez cliquer sur une carte pour voir les détails."));
        edit_event.setOnAction(e -> montrerAlerte("Veuillez cliquer sur 'Modifier' sur une carte."));
        delete_event.setOnAction(e -> montrerAlerte("Veuillez cliquer sur 'Supprimer' sur une carte."));

        if (utils.SessionManager.isAdmin()) {
            create_event.setVisible(true);
            create_event.setOnAction(e -> ouvrirFenetreCreation());

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
            // Hide the topBar (logout button inside Evenement.fxml) when inside
            // AdminDashboard
            if (topBar != null && controllers.admin.AdminController.getInstance() != null) {
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
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/views/auth/Login.fxml"));
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
            javafx.scene.Parent root = javafx.fxml.FXMLLoader
                    .load(getClass().getResource("/views/client/ClientDashboard.fxml"));
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
        if (controllers.admin.AdminController.getInstance() != null) {
            controllers.admin.AdminController.getInstance().loadCenterView("/views/admin/AdminUsers.fxml");
        } else {
            try {
                javafx.scene.Parent root = javafx.fxml.FXMLLoader
                        .load(getClass().getResource("/views/admin/AdminUsers.fxml"));
                javafx.stage.Stage stage = (javafx.stage.Stage) manage_users.getScene().getWindow();
                stage.setTitle("Gestion des Utilisateurs");
                javafx.scene.Scene scene = new javafx.scene.Scene(root);
                scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
                stage.setScene(scene);
                stage.show();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void ouvrirGestionReservations() {
        if (controllers.admin.AdminController.getInstance() != null) {
            controllers.admin.AdminController.getInstance().loadCenterView("/views/admin/AdminReservations.fxml");
        } else {
            try {
                javafx.scene.Parent root = javafx.fxml.FXMLLoader
                        .load(getClass().getResource("/views/admin/AdminReservations.fxml"));
                javafx.stage.Stage stage = (javafx.stage.Stage) manage_reservations.getScene().getWindow();
                stage.setTitle("Gestion des Réservations");
                javafx.scene.Scene scene = new javafx.scene.Scene(root);
                scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
                stage.setScene(scene);
                stage.show();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    // === Pagination ===
    private void chargerPage() {
        try {
            List<Evenement> events = dao.findPaginated(page, LIMIT);
            eventsData.setAll(events);
            refreshEventCards(eventsData);
            labelPage.setText("Page " + page);

            // Désactiver / activer les boutons
            prevPage.setDisable(page <= 1);
            nextPage.setDisable(events.size() < LIMIT); // pas de page suivante si moins que LIMIT
        } catch (SQLException e) {
            e.printStackTrace();
            montrerAlerte("Erreur lors du chargement des évènements.");
        }
    }

    private void refreshEventCards(List<Evenement> events) {
        if (event_container == null)
            return;
        event_container.getChildren().clear();
        for (Evenement ev : events) {
            event_container.getChildren().add(createEventCard(ev));
        }
    }

    private javafx.scene.Node createEventCard(Evenement ev) {
        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox();
        card.getStyleClass().add("event-card");
        card.setPrefWidth(250);
        card.setSpacing(0);

        // Poster
        javafx.scene.image.ImageView poster = new javafx.scene.image.ImageView();
        poster.setFitWidth(250);
        poster.setFitHeight(300);
        poster.setPreserveRatio(false);
        poster.getStyleClass().add("event-card-poster");

        // Load image
        String path = ev.getAffiche();
        if (path != null && !path.isEmpty()) {
            try {
                // Try absolute or relative
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    poster.setImage(new javafx.scene.image.Image(file.toURI().toString()));
                } else {
                    // Try classpath
                    poster.setImage(new javafx.scene.image.Image(getClass().getResource("/" + path).toExternalForm()));
                }
            } catch (Exception e) {
                // Fallback icon or empty
            }
        }

        javafx.scene.layout.VBox info = new javafx.scene.layout.VBox();
        info.getStyleClass().add("event-card-info");

        Label cat = new Label(ev.getCategorie());
        cat.getStyleClass().add("event-card-category");

        Label title = new Label(ev.getTitre());
        title.getStyleClass().add("event-card-title");
        title.setWrapText(true);

        Label desc = new Label(ev.getDescriptionCourte());
        desc.getStyleClass().add("event-card-description");
        desc.setWrapText(true);
        desc.setMinHeight(40);

        Button btnReserve = new Button("Réserver");
        btnReserve.setMaxWidth(Double.MAX_VALUE);
        btnReserve.setOnAction(e -> ouvrirFenetreReservation(ev));

        info.getChildren().addAll(cat, title, desc, btnReserve);

        if (utils.SessionManager.isAdmin()) {
            javafx.scene.layout.HBox adminActions = new javafx.scene.layout.HBox(10);
            adminActions.setStyle("-fx-padding: 10 0 0 0; -fx-alignment: CENTER;");

            Button btnEdit = new Button("Modifier");
            btnEdit.getStyleClass().add("button-secondary");
            btnEdit.setOnAction(e -> modifierEvenementSelectionne(ev));

            Button btnDelete = new Button("Supprimer");
            btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            btnDelete.setOnAction(e -> supprimerEvenementSelectionne(ev));

            adminActions.getChildren().addAll(btnEdit, btnDelete);
            info.getChildren().add(adminActions);
        }

        card.getChildren().addAll(poster, info);

        // Clicking the card (anywhere but buttons) shows details
        card.setOnMouseClicked(e -> {
            if (!(e.getTarget() instanceof Button)) {
                consulterEvenementSelectionne(ev);
            }
        });

        return card;
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

    // === Image Upload Helper ===
    private String handleChoisirImage(Stage owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une affiche");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(owner);
        if (selectedFile != null) {
            try {
                // Ensure directory exists
                File dir = new File("ressource/posters");
                if (!dir.exists())
                    dir.mkdirs();

                // Unique filename
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                String newName = UUID.randomUUID().toString() + extension;
                Path targetPath = Paths.get("ressource/posters", newName);

                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                return "ressource/posters/" + newName;
            } catch (IOException e) {
                e.printStackTrace();
                montrerAlerte("Erreur lors de la copie de l'image.");
            }
        }
        return null;
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
        refreshEventCards(filtered);
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
        ComboBox<String> cbCategorie = new ComboBox<>(
                FXCollections.observableArrayList("Concert", "Théâtre", "Sport", "Festival", "Cinéma", "Autre"));
        cbCategorie.setValue("Autre");

        ComboBox<Lieu> cbLieu = new ComboBox<>();
        ComboBox<Salle> cbSalle = new ComboBox<>();
        cbSalle.setDisable(true); // Disable until Lieu is chosen
        TextField dateHeureField = new TextField();
        dateHeureField.setPromptText("YYYY-MM-DD HH:MM:SS");

        try {
            LieuDAO lieuDAO = new LieuDAO();
            cbLieu.setItems(FXCollections.observableArrayList(lieuDAO.trouverTous()));
            cbLieu.setConverter(new javafx.util.StringConverter<Lieu>() {
                @Override public String toString(Lieu l) { return l != null ? l.getNom() : ""; }
                @Override public Lieu fromString(String s) { return null; }
            });
            cbSalle.setConverter(new javafx.util.StringConverter<Salle>() {
                @Override public String toString(Salle s) { return s != null ? s.getNom() : ""; }
                @Override public Salle fromString(String s) { return null; }
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
                    cbSalle.setDisable(false);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                cbSalle.setDisable(true);
            }
        });

        ComboBox<Prestataire> cbPrestataire = new ComboBox<>();
        VBox servicesBox = new VBox(5);
        try {
            PrestataireDAO prestataireDAO = new PrestataireDAO();
            List<Prestataire> prestataires = prestataireDAO.trouverTous();
            prestataires.add(0, new Prestataire(0, "Aucun prestataire", "", "", "", ""));
            cbPrestataire.setItems(FXCollections.observableArrayList(prestataires));
            cbPrestataire.setConverter(new javafx.util.StringConverter<Prestataire>() {
                @Override
                public String toString(Prestataire p) {
                    return p != null ? p.getNom() : "";
                }

                @Override
                public Prestataire fromString(String string) {
                    return null;
                }
            });
            cbPrestataire.getSelectionModel().select(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        cbPrestataire.valueProperty().addListener((obs, oldVal, newVal) -> {
            servicesBox.getChildren().clear();
            if (newVal != null && newVal.getId() > 0) {
                try {
                    ServiceDAO serviceDAO = new ServiceDAO();
                    List<Service> services = serviceDAO.trouverParPrestataire(newVal.getId());
                    for (Service svc : services) {
                        CheckBox chk = new CheckBox(svc.getNom() + " (" + svc.getDescription() + ")");
                        chk.setUserData(svc.getId());
                        servicesBox.getChildren().add(chk);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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
        grid.addRow(10, new Label("Prestataire:"), cbPrestataire);
        grid.addRow(11, new Label("Services liés:"), servicesBox);

        Button btnChoisirAffiche = new Button("Choisir une affiche");
        Label affichePathLabel = new Label("Aucune affiche sélectionnée");
        final String[] selectedImagePath = { null };
        btnChoisirAffiche.setOnAction(e -> {
            String path = handleChoisirImage((Stage) dialog.getDialogPane().getScene().getWindow());
            if (path != null) {
                selectedImagePath[0] = path;
                affichePathLabel.setText(new File(path).getName());
            }
        });
        grid.addRow(12, new Label("Affiche:"), new HBox(10, btnChoisirAffiche, affichePathLabel));

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
                    newEv.setAffiche(selectedImagePath[0]);

                    if (cbPrestataire.getValue() != null && cbPrestataire.getValue().getId() > 0) {
                        newEv.setPrestataireId(cbPrestataire.getValue().getId());
                    }

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
                // FIXED: dao.ajouter retourne maintenant l'ID généré directement
                int generatedEventId = dao.ajouter(ev);

                if (generatedEventId == 0) {
                    montrerAlerte("Erreur : impossible de récupérer l'ID du nouvel évènement.");
                    return;
                }

                // 2. Créer la Séance avec l'ID fiable
                if (cbLieu.getValue() != null && cbSalle.getValue() != null) {
                    SeanceDAO seanceDAO = new SeanceDAO();
                    String dateHeure = dateHeureField.getText().trim();
                    if (dateHeure.isEmpty()) {
                        dateHeure = "2024-01-01 00:00:00"; // Valeur par défaut si vide
                    }
                    Seance newSeance = new Seance(0, generatedEventId, cbLieu.getValue().getId_lieu(),
                            cbSalle.getValue().getId_salle(), dateHeure);
                    seanceDAO.ajouter(newSeance);
                }

                // 3. Lier les Services avec l'ID fiable
                if (ev.getPrestataireId() != null) {
                    EvenementServiceDAO evtSvcDAO = new EvenementServiceDAO();
                    for (javafx.scene.Node node : servicesBox.getChildren()) {
                        if (node instanceof CheckBox) {
                            CheckBox chk = (CheckBox) node;
                            if (chk.isSelected()) {
                                int svcId = (int) chk.getUserData();
                                evtSvcDAO.lierServiceAEvenement(generatedEventId, svcId);
                            }
                        }
                    }
                }

                chargerPage();
            } catch (Exception e) {
                e.printStackTrace();
                montrerAlerte("Erreur DB lors de la création : " + e.getMessage());
            }
        });
    }

    // === Consultation ===
    private void consulterEvenementSelectionne(Evenement selected) {
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
    private void modifierEvenementSelectionne(Evenement selected) {
        if (selected == null) {
            montrerAlerte("Veuillez sélectionner un évènement à modifier.");
            return;
        }

        Dialog<Evenement> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        dialog.setTitle("Modifier un évènement");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        EvenementDAO dao = new EvenementDAO();

        TextField titreField = new TextField(selected.getTitre());
        TextField descCourteField = new TextField(selected.getDescriptionCourte());
        TextField descLongueField = new TextField(selected.getDescriptionLongue());
        TextField dureeField = new TextField(String.valueOf(selected.getDuree()));
        TextField langueField = new TextField(selected.getLangue());
        TextField ageMinField = new TextField(String.valueOf(selected.getAgeMin()));
        ComboBox<String> cbCategorie = new ComboBox<>(
                FXCollections.observableArrayList("Concert", "Théâtre", "Sport", "Festival", "Cinéma", "Autre"));
        cbCategorie.setValue(selected.getCategorie() != null ? selected.getCategorie() : "Autre");

        ComboBox<Lieu> cbLieu = new ComboBox<>();
        ComboBox<Salle> cbSalle = new ComboBox<>();
        cbSalle.setDisable(true);
        ComboBox<Prestataire> cbPrestataire = new ComboBox<>();
        VBox servicesBox = new VBox(5);
        TextField dateHeureField = new TextField();
        dateHeureField.setPromptText("YYYY-MM-DD HH:MM:SS");

        // Set Converters immediately
        cbLieu.setConverter(new javafx.util.StringConverter<Lieu>() {
            @Override public String toString(Lieu l) { return l != null ? l.getNom() : ""; }
            @Override public Lieu fromString(String s) { return null; }
        });
        cbSalle.setConverter(new javafx.util.StringConverter<Salle>() {
            @Override public String toString(Salle s) { return s != null ? s.getNom() : ""; }
            @Override public Salle fromString(String s) { return null; }
        });
        cbPrestataire.setConverter(new javafx.util.StringConverter<Prestataire>() {
            @Override public String toString(Prestataire p) { return p != null ? p.getNom() : ""; }
            @Override public Prestataire fromString(String s) { return null; }
        });

        // Current Seance details
        final int[] currentSeanceId = { -1 };
        final String[] selectedImagePath = { selected.getAffiche() };
        Button btnChoisirAffiche = new Button("Choisir une affiche");
        Label affichePathLabel = new Label(
                selected.getAffiche() != null ? selected.getAffiche() : "Aucune affiche sélectionnée");
        btnChoisirAffiche.setOnAction(e -> {
            String path = handleChoisirImage((javafx.stage.Stage) dialog.getDialogPane().getScene().getWindow());
            if (path != null) {
                selectedImagePath[0] = path;
                affichePathLabel.setText(path);
            }
        });

        try {
            LieuDAO lieuDAO = new LieuDAO();
            List<Lieu> lieux = lieuDAO.trouverTous();
            cbLieu.setItems(FXCollections.observableArrayList(lieux));

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
                        cbSalle.setDisable(false);
                        // Pre-select Salle
                        cbSalle.getSelectionModel().select(new Salle(existingSalleId, 0, "", 0, 0, 0));
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
                    cbSalle.setDisable(false);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                cbSalle.setDisable(true);
            }
        });

        List<Integer> linkedServices = new java.util.ArrayList<>();
        try {
            PrestataireDAO prestataireDAO = new PrestataireDAO();
            List<Prestataire> prestataires = prestataireDAO.trouverTous();
            prestataires.add(0, new Prestataire(0, "Aucun prestataire", "", "", "", ""));
            cbPrestataire.setItems(FXCollections.observableArrayList(prestataires));

            // Preselect
            if (selected.getPrestataireId() != null && selected.getPrestataireId() > 0) {
                for (Prestataire p : prestataires) {
                    if (p.getId() == selected.getPrestataireId()) {
                        cbPrestataire.getSelectionModel().select(p);
                        break;
                    }
                }
                EvenementServiceDAO eSvcDAO = new EvenementServiceDAO();
                linkedServices.addAll(eSvcDAO.recupererIdServicesPourEvenement(selected.getId()));
            } else {
                cbPrestataire.getSelectionModel().select(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        cbPrestataire.valueProperty().addListener((obs, oldVal, newVal) -> {
            servicesBox.getChildren().clear();
            if (newVal != null && newVal.getId() > 0) {
                try {
                    ServiceDAO serviceDAO = new ServiceDAO();
                    List<Service> services = serviceDAO.trouverParPrestataire(newVal.getId());
                    int prevId = selected.getPrestataireId() != null ? selected.getPrestataireId() : 0;
                    for (Service svc : services) {
                        CheckBox chk = new CheckBox(svc.getNom() + " (" + svc.getDescription() + ")");
                        chk.setUserData(svc.getId());
                        if (linkedServices.contains((Integer) svc.getId()) && newVal.getId() == prevId) {
                            chk.setSelected(true);
                        }
                        servicesBox.getChildren().add(chk);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        // Initial populate for services Box
        if (cbPrestataire.getValue() != null && cbPrestataire.getValue().getId() > 0) {
            try {
                ServiceDAO serviceDAO = new ServiceDAO();
                List<Service> services = serviceDAO.trouverParPrestataire(cbPrestataire.getValue().getId());
                for (Service svc : services) {
                    CheckBox chk = new CheckBox(svc.getNom() + " (" + svc.getDescription() + ")");
                    chk.setUserData(svc.getId());
                    if (linkedServices.contains((Integer) svc.getId()))
                        chk.setSelected(true);
                    servicesBox.getChildren().add(chk);
                }
            } catch (Exception e) {
            }
        }

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
        grid.addRow(10, new Label("Prestataire:"), cbPrestataire);
        grid.addRow(11, new Label("Services liés:"), servicesBox);
        grid.addRow(12, new Label("Affiche:"), new HBox(10, btnChoisirAffiche, affichePathLabel));

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
                    editedEv.setAffiche(selectedImagePath[0]);

                    if (cbPrestataire.getValue() != null && cbPrestataire.getValue().getId() > 0) {
                        editedEv.setPrestataireId(cbPrestataire.getValue().getId());
                    }

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
                if (cbLieu.getValue() != null && cbSalle.getValue() != null) {
                    SeanceDAO seanceDAO = new SeanceDAO();
                    String dateHeure = dateHeureField.getText().trim();
                    if (dateHeure.isEmpty()) {
                        dateHeure = "2024-01-01 00:00:00"; // Valeur par défaut
                    }
                    if (currentSeanceId[0] != -1) {
                        // Update existing
                        Seance updatedSeance = new Seance(currentSeanceId[0], ev.getId(),
                                cbLieu.getValue().getId_lieu(), cbSalle.getValue().getId_salle(),
                                dateHeure);
                        seanceDAO.modifier(updatedSeance);
                    } else {
                        // Create new if there wasn't one
                        Seance newSeance = new Seance(0, ev.getId(), cbLieu.getValue().getId_lieu(),
                                cbSalle.getValue().getId_salle(), dateHeure);
                        seanceDAO.ajouter(newSeance);
                    }
                }

                // Update Services
                EvenementServiceDAO evtSvcDAO = new EvenementServiceDAO();
                evtSvcDAO.supprimerLiaisonsPourEvenement(ev.getId());

                if (ev.getPrestataireId() != null) {
                    for (javafx.scene.Node node : servicesBox.getChildren()) {
                        if (node instanceof CheckBox) {
                            CheckBox chk = (CheckBox) node;
                            if (chk.isSelected()) {
                                int svcId = (int) chk.getUserData();
                                evtSvcDAO.lierServiceAEvenement(ev.getId(), svcId);
                            }
                        }
                    }
                }

                chargerPage();
            } catch (Exception e) {
                e.printStackTrace();
                montrerAlerte("Erreur DB lors de la modification : " + e.getMessage());
            }
        });
    }

    // === Réservation ===
    private void ouvrirFenetreReservation(Evenement selected) {
        if (selected == null) {
            montrerAlerte("Veuillez sélectionner un évènement pour réserver.");
            return;
        }

        try {
            // Check if a seance exists for this event
            int idSeance = -1;
            int idSalle = -1;

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

            // FIXED: gestion correcte de la navigation selon le contexte
            if (!utils.SessionManager.isAdmin() && controllers.client.ClientController.getInstance() != null) {
                // Client dans le dashboard client
                controllers.client.ClientController.getInstance().setCenterView(root);
            } else if (utils.SessionManager.isAdmin() && controllers.admin.AdminController.getInstance() != null) {
                // Admin dans le dashboard admin
                controllers.admin.AdminController.getInstance().setCenterView(root);
            } else {
                // Fallback standalone (fenêtre séparée)
                javafx.stage.Stage stage = (javafx.stage.Stage) event_container.getScene().getWindow();
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
    private void supprimerEvenementSelectionne(Evenement selected) {
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
                dao.supprimer(selected.getId());

                if (eventsData.size() == 1 && page > 1) {
                    page--;
                }

                chargerPage();
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