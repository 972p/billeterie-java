package controllers.evenement;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import DAO.BilletDAO;
import DAO.ClientDAO;
import DAO.EvenementDAO;
import DAO.CodePromoDAO;
import database.MySQLConnection;
import models.Billet;
import models.Client;
import models.Evenement;
import models.CodePromo;
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
    private ImageView imgPoster;
    @FXML
    private Label lblEventTitle;
    @FXML
    private Label lblEventDate;
    @FXML
    private Label lblEventLieu;
    @FXML
    private Label lblUserBalance;
    
    @FXML
    private VBox btnCardPayment;
    @FXML
    private VBox btnBalancePayment;
    @FXML
    private VBox paneCard;
    @FXML
    private VBox paneBalance;

    @FXML
    private TextField txtCardNumber;
    @FXML
    private TextField txtExpiration;
    @FXML
    private TextField txtCvv;

    @FXML
    private TextField txtPromoCode;
    @FXML
    private Label lblDiscount;
    @FXML
    private HBox hboxDiscount;

    private int idEvenement;
    private int idSeance;
    private List<Integer> selectedSeats;
    
    // Hardcoded demo tarifs
    private final double TARIF_STANDARD = 15.0;
    private final double TARIF_ETUDIANT = 10.0;
    private final double TARIF_ENFANT = 5.0;

    private List<Double> seatPrices = new ArrayList<>();
    private boolean useBalance = false;
    private double totalAmount = 0;
    private double discountAmount = 0;

    public void initData(int idEvenement, int idSeance, int idSalle, List<Integer> selectedSeats) {
        this.idEvenement = idEvenement;
        this.idSeance = idSeance;
        this.selectedSeats = selectedSeats;

        loadEventSummary();
        loadUserBalance();
        populateSeatsList();
        selectCardPayment(); // Default method
    }

    private void loadEventSummary() {
        try {
            EvenementDAO evenementDAO = new EvenementDAO();
            Evenement ev = evenementDAO.trouver(idEvenement);
            if (ev != null) {
                lblEventTitle.setText(ev.getTitre());
                
                // Fetch séance details for date/lieu
                try (Connection conn = MySQLConnection.connect();
                     PreparedStatement ps = conn.prepareStatement(
                             "SELECT s.date_heure, l.nom as lieu_nom FROM Seance s JOIN Lieu l ON s.id_lieu = l.id_lieu WHERE s.id_seance = ?")) {
                    ps.setInt(1, idSeance);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        lblEventDate.setText(rs.getString("date_heure"));
                        lblEventLieu.setText(rs.getString("lieu_nom"));
                    }
                }

                // Poster
                String path = ev.getAffiche();
                if (path != null && !path.isEmpty()) {
                    File file = new File(path);
                    if (file.exists()) {
                        imgPoster.setImage(new Image(file.toURI().toString()));
                    } else if (getClass().getResource("/" + path) != null) {
                        imgPoster.setImage(new Image(getClass().getResource("/" + path).toExternalForm()));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUserBalance() {
        Client current = SessionManager.getCurrentUser();
        if (current != null) {
            lblUserBalance.setText(String.format("%.2f €", current.getSolde()));
        }
    }

    @FXML
    private void selectCardPayment() {
        useBalance = false;
        btnCardPayment.getStyleClass().add("payment-method-selected");
        btnBalancePayment.getStyleClass().remove("payment-method-selected");
        paneCard.setVisible(true);
        paneBalance.setVisible(false);
    }

    @FXML
    private void selectBalancePayment() {
        useBalance = true;
        btnBalancePayment.getStyleClass().add("payment-method-selected");
        btnCardPayment.getStyleClass().remove("payment-method-selected");
        paneCard.setVisible(false);
        paneBalance.setVisible(true);
    }

    private void populateSeatsList() {
        vboxPlaces.getChildren().clear();
        seatPrices.clear();

        for (int i = 0; i < selectedSeats.size(); i++) {
            int placeId = selectedSeats.get(i);
            seatPrices.add(TARIF_STANDARD);

            HBox row = new HBox(10);
            row.getStyleClass().add("order-item");
            row.setStyle("-fx-alignment: center-left;");
            
            Label lblPlace = new Label("Place N°" + placeId);
            lblPlace.setStyle("-fx-text-fill: #e0e0e0;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

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
        totalAmount = 0;
        for (Double price : seatPrices) {
            totalAmount += price;
        }
        totalAmount -= discountAmount;
        if (totalAmount < 0) totalAmount = 0;
        lblTotal.setText(String.format("%.2f", totalAmount) + " €");
    }

    @FXML
    public void handleApplyPromo(ActionEvent event) {
        String code = txtPromoCode.getText().trim();
        if (code.isEmpty()) return;

        try {
            CodePromoDAO dao = new CodePromoDAO();
            CodePromo cp = dao.trouverParCode(code);
            if (cp != null) {
                double baseTotal = 0;
                for (Double price : seatPrices) baseTotal += price;

                if ("POURCENTAGE".equals(cp.getType_reduction())) {
                    discountAmount = baseTotal * (cp.getValeur_reduction() / 100.0);
                } else {
                    discountAmount = cp.getValeur_reduction();
                }
                if (discountAmount > baseTotal) discountAmount = baseTotal;

                lblDiscount.setText("- " + String.format("%.2f", discountAmount) + " €");
                hboxDiscount.setVisible(true);
                hboxDiscount.setManaged(true);
                updateTotal();
                showAlert(Alert.AlertType.INFORMATION, "Code Promo", "Code appliqué avec succès !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Ce code promo est invalide ou expiré.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePay(ActionEvent event) {
        Client currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté.");
            return;
        }

        if (useBalance) {
            if (currentUser.getSolde() < totalAmount) {
                showAlert(Alert.AlertType.ERROR, "Solde insuffisant", "Votre solde (" + String.format("%.2f", currentUser.getSolde()) + " €) est insuffisant pour régler " + String.format("%.2f", totalAmount) + " €.");
                return;
            }
            
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmer le paiement");
            confirm.setHeaderText("Paiement par solde");
            confirm.setContentText("Voulez-vous confirmer l'achat de " + selectedSeats.size() + " places pour un montant de " + String.format("%.2f", totalAmount) + " € ?");
            
            if (confirm.showAndWait().get() != ButtonType.OK) {
                return;
            }
        } else {
            String card = txtCardNumber.getText().trim();
            String exp = txtExpiration.getText().trim();
            String cvv = txtCvv.getText().trim();

            if (card.isEmpty() || exp.isEmpty() || cvv.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez remplir tous les champs bancaires fictifs.");
                return;
            }
            if (card.length() < 10) {
                showAlert(Alert.AlertType.ERROR, "Paiement Refusé", "Numéro de carte invalide.");
                return;
            }
        }

        try {
            BilletDAO billetDAO = new BilletDAO();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDate = sdf.format(new java.util.Date());
            List<BilletDisplay> generatedBillets = new ArrayList<>();

            for (int i = 0; i < selectedSeats.size(); i++) {
                int idPlace = selectedSeats.get(i);
                double actualPrice = seatPrices.get(i);
                int idTarif = getOrCreateTarifId(actualPrice, actualPrice == 15.0 ? "Standard" : (actualPrice == 10.0 ? "Etudiant" : "Enfant"));

                Billet newBillet = new Billet(0, idSeance, idTarif, currentUser.getId(), idPlace, "VALIDE", currentDate);
                billetDAO.ajouter(newBillet);

                // Fetch info for PDF
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
                            rs.getInt("id_billet"), rs.getString("titre"), rs.getString("date_heure"), rs.getString("date_heure"),
                            rs.getString("salle"), rs.getString("lieu"), String.valueOf(rs.getInt("rangee")), 
                            String.valueOf(rs.getInt("numero")), rs.getDouble("prix"), "VALIDE", currentDate
                        ));
                    }
                }
            }

            if (useBalance) {
                ClientDAO clientDAO = new ClientDAO();
                clientDAO.updateSolde(currentUser.getId(), currentUser.getSolde() - totalAmount);
                currentUser.setSolde(currentUser.getSolde() - totalAmount);
            }

            if(!generatedBillets.isEmpty()) {
                BilletDisplay mainBillet = generatedBillets.get(0);
                String pdfPath = "ressource/uploads/Billet_" + mainBillet.getNom_evenement().replaceAll("\\s+", "_") + "_" + mainBillet.getId_billet() + ".pdf";
                try {
                    PdfGenerator.generateBilletPdf(mainBillet, pdfPath);
                    EmailSender.sendTicketEmail(currentUser.getEmail(), currentUser.getNom(), mainBillet.getNom_evenement(), pdfPath);
                } catch(Exception ignored) {}
            }

            showAlert(Alert.AlertType.INFORMATION, "Paiement réussi !", "Vos " + selectedSeats.size() + " places sont réservées.");
            
            if (!utils.SessionManager.isAdmin() && controllers.ClientController.getInstance() != null) {
                controllers.ClientController.getInstance().loadCenterView("/views/billet/ClientBillets.fxml");
            } else {
                Parent root = FXMLLoader.load(getClass().getResource("/views/billet/ClientBillets.fxml"));
                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                stage.setTitle("Mes Billets");
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
                stage.setScene(scene);
                stage.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'enregistrement.");
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        try {
            if (!utils.SessionManager.isAdmin() && controllers.ClientController.getInstance() != null) {
                controllers.ClientController.getInstance().loadCenterView("/views/evenement/Evenement.fxml");
            } else {
                Parent root = FXMLLoader.load(getClass().getResource("/views/evenement/Evenement.fxml"));
                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
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
                if (rsTarif.next()) idTarif = rsTarif.getInt(1);
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
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        } catch(Exception ignored){}
        alert.showAndWait();
    }
}
