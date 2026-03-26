package controllers.evenement;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import DAO.BilletDAO;
import database.MySQLConnection;
import models.Billet;
import models.Client;
import utils.SessionManager;
import utils.EmailSender;
import utils.PdfGenerator;
import models.BilletDisplay;

public class CheckoutController {

    @FXML
    private VBox vboxPlaces;

    @FXML
    private Label lblTotal;

    @FXML
    private TextField txtCardNumber;
    @FXML
    private TextField txtExpiration;
    @FXML
    private TextField txtCvv;

    private int idEvenement;
    private int idSeance;
    private List<Integer> selectedSeats;
    
    // Hardcoded demo tarifs
    private final double TARIF_STANDARD = 15.0;
    private final double TARIF_ETUDIANT = 10.0;
    private final double TARIF_ENFANT = 5.0;

    // Keep track of the selected price per seat
    private List<Double> seatPrices = new ArrayList<>();

    public void initData(int idEvenement, int idSeance, int idSalle, List<Integer> selectedSeats) {
        this.idEvenement = idEvenement;
        this.idSeance = idSeance;
        this.selectedSeats = selectedSeats;

        populateSeatsList();
    }

    private void populateSeatsList() {
        vboxPlaces.getChildren().clear();
        seatPrices.clear();

        for (int i = 0; i < selectedSeats.size(); i++) {
            int placeId = selectedSeats.get(i);
            seatPrices.add(TARIF_STANDARD); // Default to standard

            HBox row = new HBox(10);
            row.setStyle("-fx-alignment: center-left; -fx-padding: 5; -fx-border-color: #444; -fx-border-radius: 5;");
            
            Label lblPlace = new Label("Place N°" + placeId);
            lblPlace.setStyle("-fx-text-fill: #e0e0e0;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            ComboBox<String> cbTarif = new ComboBox<>(FXCollections.observableArrayList(
                    "Standard (15.00€)", "Étudiant (10.00€)", "Enfant (5.00€)"));
            cbTarif.getSelectionModel().selectFirst();
            
            final int index = i;
            cbTarif.setOnAction(e -> {
                String val = cbTarif.getValue();
                if (val.contains("Étudiant")) seatPrices.set(index, TARIF_ETUDIANT);
                else if (val.contains("Enfant")) seatPrices.set(index, TARIF_ENFANT);
                else seatPrices.set(index, TARIF_STANDARD);
                
                updateTotal();
            });

            row.getChildren().addAll(lblPlace, spacer, cbTarif);
            vboxPlaces.getChildren().add(row);
        }
        updateTotal();
    }

    private void updateTotal() {
        double total = 0;
        for (Double price : seatPrices) {
            total += price;
        }
        lblTotal.setText(String.format("%.2f", total) + " €");
    }

    @FXML
    public void handlePay(ActionEvent event) {
        String card = txtCardNumber.getText().trim();
        String exp = txtExpiration.getText().trim();
        String cvv = txtCvv.getText().trim();

        if (card.isEmpty() || exp.isEmpty() || cvv.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez remplir tous les champs bancaires fictifs.");
            return;
        }

        // Simulate validation
        if (card.length() < 10) {
            showAlert(Alert.AlertType.ERROR, "Paiement Refusé", "Numéro de carte invalide.");
            return;
        }

        Client currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté.");
            return;
        }

        try {
            // Ensure Tarifs exist in DB and get their IDs, or just map them to the default one for simplicity since DB structure might only have one
            // We'll create or fetch the Tarif ID so FK constraints pass
            getOrCreateTarifId(TARIF_STANDARD, "Standard");

            BilletDAO billetDAO = new BilletDAO();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDate = sdf.format(new java.util.Date());

            List<BilletDisplay> generatedBillets = new ArrayList<>();

            for (int i = 0; i < selectedSeats.size(); i++) {
                int idPlace = selectedSeats.get(i);
                double actualPrice = seatPrices.get(i);
                
                // Get correct Tarif ID
                int idTarif = getOrCreateTarifId(actualPrice, actualPrice == 15.0 ? "Standard" : (actualPrice == 10.0 ? "Etudiant" : "Enfant"));

                Billet newBillet = new Billet(0, idSeance, idTarif, currentUser.getId(), idPlace, "VALIDE", currentDate);
                billetDAO.ajouter(newBillet);

                // Fetch the detailed BilletDisplay info to generate PDF/Email
                try (Connection conn = MySQLConnection.connect();
                     PreparedStatement ps = conn.prepareStatement(
                             "SELECT b.id_billet, e.titre, s.date_heure, l.nom as lieu, sa.nom as salle, p.rangee, p.numero, t.prix " +
                             "FROM Billet b " +
                             "JOIN Seance s ON b.id_seance = s.id_seance " +
                             "JOIN Evenement e ON s.id_evenement = e.id_evenement " +
                             "JOIN Lieu l ON s.id_lieu = l.id_lieu " +
                             "JOIN Salle sa ON s.id_salle = sa.id_salle " +
                             "JOIN Place p ON b.id_place = p.id_place " +
                             "JOIN Tarif t ON b.id_tarif = t.id_tarif " +
                             "WHERE b.id_seance=? AND b.id_place=? AND b.id_client=? ORDER BY b.id_billet DESC LIMIT 1")) {
                    ps.setInt(1, idSeance);
                    ps.setInt(2, idPlace);
                    ps.setInt(3, currentUser.getId());
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()) {
                        generatedBillets.add(new BilletDisplay(
                            rs.getInt("id_billet"),
                            rs.getString("titre"),
                            rs.getString("date_heure"),
                            rs.getString("date_heure"), // rough mapping
                            rs.getString("salle"),
                            rs.getString("lieu"),
                            String.valueOf(rs.getInt("rangee")),
                            String.valueOf(rs.getInt("numero")),
                            rs.getDouble("prix"),
                            "VALIDE",
                            currentDate
                        ));
                    }
                }
            }

            // Send confirmation email with PDF for the first billet (or all, but let's do the first for demo)
            if(!generatedBillets.isEmpty()) {
                BilletDisplay mainBillet = generatedBillets.get(0);
                String pdfPath = "ressource/uploads/Billet_" + mainBillet.getNom_evenement().replaceAll("\\s+", "_") + "_" + mainBillet.getId_billet() + ".pdf";
                
                try {
                    PdfGenerator.generateBilletPdf(mainBillet, pdfPath);
                    EmailSender.sendTicketEmail(currentUser.getEmail(), currentUser.getNom(), mainBillet.getNom_evenement(), pdfPath);
                } catch(Exception pdfEx) {
                    System.err.println("Failed to generate PDF/Email: " + pdfEx.getMessage());
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Paiement réussi !", "Vos " + selectedSeats.size() + " places sont réservées. Vous recevrez un e-mail de confirmation très bientôt.");
            
            // Redirect to Mes Billets
            try {
                if (!utils.SessionManager.isAdmin() && controllers.ClientController.getInstance() != null) {
                    controllers.ClientController.getInstance().loadCenterView("/views/billet/ClientBillets.fxml");
                } else {
                    Parent root = FXMLLoader.load(getClass().getResource("/views/billet/ClientBillets.fxml"));
                    Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                    stage.setTitle("Mes Billets");
                    Scene scene = new Scene(root);
                    String css = this.getClass().getResource("/views/style.css").toExternalForm();
                    scene.getStylesheets().add(css);
                    stage.setScene(scene);
                    stage.show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'enregistrement de vos billets.");
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        // Redirect back to seat selection or events
        try {
            if (!utils.SessionManager.isAdmin() && controllers.ClientController.getInstance() != null) {
                controllers.ClientController.getInstance().loadCenterView("/views/evenement/Evenement.fxml");
            } else {
                Parent root = FXMLLoader.load(getClass().getResource("/views/evenement/Evenement.fxml"));
                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                stage.setTitle("Évènements");
                Scene scene = new Scene(root);
                String css = this.getClass().getResource("/views/style.css").toExternalForm();
                scene.getStylesheets().add(css);
                stage.setScene(scene);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getOrCreateTarifId(double price, String libelle) {
        int idTarif = -1;
        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT id_tarif FROM Tarif WHERE id_evenement = ? AND prix = ? LIMIT 1")) {
            ps.setInt(1, idEvenement);
            ps.setDouble(2, price);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                idTarif = rs.getInt("id_tarif");
            } else {
                PreparedStatement insertTarif = conn.prepareStatement(
                        "INSERT INTO Tarif (id_evenement, libelle, prix) VALUES (?, ?, ?)",
                        java.sql.Statement.RETURN_GENERATED_KEYS);
                insertTarif.setInt(1, idEvenement);
                insertTarif.setString(2, libelle);
                insertTarif.setDouble(3, price);
                insertTarif.executeUpdate();
                ResultSet rsTarif = insertTarif.getGeneratedKeys();
                if (rsTarif.next())
                    idTarif = rsTarif.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return idTarif;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        try {
            String css = this.getClass().getResource("/views/style.css").toExternalForm();
            alert.getDialogPane().getStylesheets().add(css);
        } catch(Exception ignored){}
        alert.showAndWait();
    }
}
