package controllers.admin;

import database.MySQLConnection;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.stream.Collectors;

public class AdminTransactionsController {

    public static class TransactionView {
        private String dateAchat;
        private int idClient;
        private String clientInfo;
        private int nbBillets;
        private double total;
        private String stripePaymentId;
        private String statut;

        public TransactionView(String dateAchat, int idClient, String clientInfo, int nbBillets, double total, String stripePaymentId, int nbValides) {
            this.dateAchat = dateAchat;
            this.idClient = idClient;
            this.clientInfo = clientInfo;
            this.nbBillets = nbBillets;
            this.total = total;
            this.stripePaymentId = stripePaymentId;
            
            if (nbValides == 0) {
                this.statut = "Annulée";
            } else if (nbValides < nbBillets) {
                this.statut = "Partiellement Annulée";
            } else {
                this.statut = "Valide";
            }
        }

        public String getDateAchat() {
            return dateAchat;
        }

        public int getIdClient() {
            return idClient;
        }

        public String getClientInfo() {
            return clientInfo;
        }

        public int getNbBillets() {
            return nbBillets;
        }

        public double getTotal() {
            return total;
        }

        public String getStripePaymentId() {
            return stripePaymentId;
        }

        public String getStatut() {
            return statut;
        }
    }

    @FXML
    private TableView<TransactionView> transactions_table;
    @FXML
    private TableColumn<TransactionView, String> colDate;
    @FXML
    private TableColumn<TransactionView, String> colStatut;
    @FXML
    private TableColumn<TransactionView, String> colClient;
    @FXML
    private TableColumn<TransactionView, String> colNbBillets;
    @FXML
    private TableColumn<TransactionView, String> colTotal;
    @FXML
    private TableColumn<TransactionView, TransactionView> colAction;

    @FXML
    private TextField txtRecherche;

    private ObservableList<TransactionView> toutesLesTransactions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateAchat()));
        colStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut()));
        colClient.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClientInfo()));
        colNbBillets.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getNbBillets())));
        colTotal.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f", data.getValue().getTotal())));

        colAction.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));

        setupActionColumn();
        chargerTransactions();

        // Ajout d'un écouteur pour la recherche
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrerTransactions(newValue);
        });
    }

    private void filtrerTransactions(String recherche) {
        if (recherche == null || recherche.trim().isEmpty()) {
            transactions_table.setItems(toutesLesTransactions);
        } else {
            String lowerCaseFilter = recherche.toLowerCase();
            ObservableList<TransactionView> filtrees = toutesLesTransactions.stream()
                    .filter(t -> t.getClientInfo().toLowerCase().contains(lowerCaseFilter))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            transactions_table.setItems(filtrees);
        }
    }

    private void chargerTransactions() {
        toutesLesTransactions.clear();

        String query = "SELECT b.date_achat, c.id_client, c.nom, c.email, COUNT(b.id_billet) as nb_billets, SUM(t.prix) as total, ps.stripe_payment_id, " +
                "SUM(CASE WHEN b.statut = 'VALIDE' THEN 1 ELSE 0 END) as nb_valides " +
                "FROM Billet b " +
                "JOIN Client c ON b.id_client = c.id_client " +
                "JOIN Tarif t ON b.id_tarif = t.id_tarif " +
                "LEFT JOIN PaiementStripe ps ON b.date_achat = ps.date_achat AND b.id_client = ps.id_client " +
                "GROUP BY b.date_achat, c.id_client, c.nom, c.email, ps.stripe_payment_id " +
                "ORDER BY b.date_achat DESC";

        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String clientInfo = rs.getString("nom") + " (" + rs.getString("email") + ")";
                toutesLesTransactions.add(new TransactionView(
                        rs.getString("date_achat"),
                        rs.getInt("id_client"),
                        clientInfo,
                        rs.getInt("nb_billets"),
                        rs.getDouble("total"),
                        rs.getString("stripe_payment_id"),
                        rs.getInt("nb_valides")));
            }

            transactions_table.setItems(toutesLesTransactions);

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Impossible de charger les transactions.");
            alert.show();
        }
    }

    private void setupActionColumn() {
        colAction.setCellFactory(param -> new TableCell<TransactionView, TransactionView>() {
            private final Button btnDetails = new Button("Détails");
            private final Button btnAnnuler = new Button("Annuler & Rembourser");

            {
                btnDetails.setStyle("-fx-background-color: #bb86fc; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
                btnDetails.setOnAction(event -> {
                    TransactionView trans = getTableView().getItems().get(getIndex());
                    afficherDetails(trans);
                });

                btnAnnuler.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
                btnAnnuler.setOnAction(event -> {
                    TransactionView trans = getTableView().getItems().get(getIndex());
                    annulerTransaction(trans);
                });
            }

            @Override
            protected void updateItem(TransactionView item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    btnAnnuler.setDisable(item.getStatut().contains("Annulée"));
                    HBox box = new HBox(10, btnDetails, btnAnnuler);
                    box.setStyle("-fx-alignment: center");
                    setGraphic(box);
                }
            }
        });
    }

    private void afficherDetails(TransactionView trans) {
        StringBuilder details = new StringBuilder();
        
        String query = "SELECT e.titre, s.date_heure, p.rangee, p.numero, t.prix " +
                "FROM Billet b " +
                "JOIN Seance s ON b.id_seance = s.id_seance " +
                "JOIN Evenement e ON s.id_evenement = e.id_evenement " +
                "JOIN Place p ON b.id_place = p.id_place " +
                "JOIN Tarif t ON b.id_tarif = t.id_tarif " +
                "WHERE b.date_achat = ? AND b.id_client = ?";

        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, trans.getDateAchat());
            ps.setInt(2, trans.getIdClient());
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String eventTitle = rs.getString("titre");
                String dateHeure = rs.getString("date_heure");
                int rangee = rs.getInt("rangee");
                int numero = rs.getInt("numero");
                double prix = rs.getDouble("prix");
                
                String placeLettre = String.valueOf((char)('A' + rangee - 1));
                
                details.append("- ").append(eventTitle)
                       .append(" (").append(dateHeure).append(") ")
                       .append("| Place: ").append(placeLettre).append(numero)
                       .append(" | Prix: ").append(String.format("%.2f €", prix)).append("\n");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            details.append("Erreur lors de la récupération des détails.");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        try {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        } catch(Exception ignored){}
        
        alert.setTitle("Détails de la transaction");
        alert.setHeaderText("Achat du " + trans.getDateAchat() + "\nClient : " + trans.getClientInfo());
        
        TextArea area = new TextArea(details.toString());
        area.setEditable(false);
        area.setWrapText(true);
        area.setMaxWidth(Double.MAX_VALUE);
        area.setMaxHeight(Double.MAX_VALUE);
        
        alert.getDialogPane().setExpandableContent(area);
        alert.getDialogPane().setExpanded(true);
        
        alert.showAndWait();
    }

    private void annulerTransaction(TransactionView trans) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        try { alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm()); } catch(Exception ignored){}
        alert.setTitle("Annuler la transaction");
        alert.setHeaderText("Annuler tous les billets de cet achat ?");
        alert.setContentText("Le remboursement Stripe sera déclenché si le paiement a été fait par carte.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (trans.getStripePaymentId() != null && !trans.getStripePaymentId().isEmpty()) {
                        // Paiement Stripe -> On annule les billets sans recréditer le solde client
                        String updateQuery = "UPDATE Billet SET statut = 'ANNULE' WHERE date_achat = ? AND id_client = ?";
                        try (Connection conn = MySQLConnection.connect();
                             PreparedStatement psUpdate = conn.prepareStatement(updateQuery)) {
                            psUpdate.setString(1, trans.getDateAchat());
                            psUpdate.setInt(2, trans.getIdClient());
                            psUpdate.executeUpdate();
                        }
                        
                        boolean refundSuccess = utils.StripeService.refundPayment(trans.getStripePaymentId());
                        if (refundSuccess) {
                            Alert info = new Alert(Alert.AlertType.INFORMATION);
                            info.setContentText("Les billets ont été annulés et le remboursement Stripe a été effectué avec succès !");
                            info.show();
                        } else {
                            Alert err = new Alert(Alert.AlertType.WARNING);
                            err.setContentText("Billets annulés, mais une erreur est survenue lors du remboursement Stripe.");
                            err.show();
                        }
                    } else {
                        // Paiement par solde -> On annule les billets normalement (ce qui recrédite le solde)
                        DAO.BilletDAO billetDAO = new DAO.BilletDAO();
                        String getIdsQuery = "SELECT id_billet FROM Billet WHERE date_achat = ? AND id_client = ? AND statut = 'VALIDE'";
                        try (Connection conn = MySQLConnection.connect();
                             PreparedStatement ps = conn.prepareStatement(getIdsQuery)) {
                            ps.setString(1, trans.getDateAchat());
                            ps.setInt(2, trans.getIdClient());
                            ResultSet rs = ps.executeQuery();
                            while (rs.next()) {
                                try {
                                    billetDAO.annulerBillet(rs.getInt("id_billet"));
                                } catch (Exception ignored) {}
                            }
                        }
                        Alert info = new Alert(Alert.AlertType.INFORMATION);
                        info.setContentText("Billets annulés. Le solde du client a été recrédité.");
                        info.show();
                    }
                    chargerTransactions();
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setContentText("Erreur lors de l'annulation de la transaction.");
                    errorAlert.show();
                }
            }
        });
    }
}
