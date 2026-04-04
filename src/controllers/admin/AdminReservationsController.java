package controllers.admin;

import database.MySQLConnection;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import DAO.BilletDAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminReservationsController {

    public static class ReservationView {
        private int idBillet;
        private String eventTitre;
        private String seanceDate;
        private String clientNom;
        private String placeInfo;
        private String dateAchat;

        public ReservationView(int idBillet, String eventTitre, String seanceDate, String clientNom, String placeInfo,
                String dateAchat) {
            this.idBillet = idBillet;
            this.eventTitre = eventTitre;
            this.seanceDate = seanceDate;
            this.clientNom = clientNom;
            this.placeInfo = placeInfo;
            this.dateAchat = dateAchat;
        }

        public int getIdBillet() {
            return idBillet;
        }

        public String getEventTitre() {
            return eventTitre;
        }

        public String getSeanceDate() {
            return seanceDate;
        }

        public String getClientNom() {
            return clientNom;
        }

        public String getPlaceInfo() {
            return placeInfo;
        }

        public String getDateAchat() {
            return dateAchat;
        }
    }

    @FXML
    private TableView<ReservationView> reservations_table;
    @FXML
    private TableColumn<ReservationView, String> colEvent;
    @FXML
    private TableColumn<ReservationView, String> colDateHeure;
    @FXML
    private TableColumn<ReservationView, String> colClient;
    @FXML
    private TableColumn<ReservationView, String> colPlace;
    @FXML
    private TableColumn<ReservationView, String> colDateAchat;
    @FXML
    private TableColumn<ReservationView, Void> colAction;


    @FXML
    private javafx.scene.chart.PieChart pieChartVentes;
    @FXML
    private javafx.scene.chart.BarChart<String, Number> barChartRevenus;

    private BilletDAO billetDAO = new BilletDAO();

    @FXML
    public void initialize() {
        colEvent.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEventTitre()));
        colDateHeure.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getSeanceDate()));
        colClient.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getClientNom()));
        colPlace.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getPlaceInfo()));
        colDateAchat.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDateAchat()));

        setupActionColumn();

        chargerReservations();
        chargerStatistiques();
    }

    private void chargerStatistiques() {
        try {
            // Ventes (PieChart)
            java.util.Map<String, Integer> ventes = billetDAO.getBilletsVendusParEvenement();
            ObservableList<javafx.scene.chart.PieChart.Data> pieData = FXCollections.observableArrayList();
            for (java.util.Map.Entry<String, Integer> entry : ventes.entrySet()) {
                pieData.add(new javafx.scene.chart.PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }
            pieChartVentes.setData(pieData);

            // Revenus (BarChart)
            java.util.Map<String, Double> revenus = billetDAO.getChiffreAffairesParEvenement();
            javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
            series.setName("Revenus");
            for (java.util.Map.Entry<String, Double> entry : revenus.entrySet()) {
                series.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            barChartRevenus.getData().clear();
            barChartRevenus.getData().add(series);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chargerReservations() {
        ObservableList<ReservationView> data = FXCollections.observableArrayList();
        String query = "SELECT b.id_billet, e.titre, s.date_heure, c.nom, c.email, p.rangee, p.numero, b.date_achat " +
                "FROM Billet b " +
                "JOIN Seance s ON b.id_seance = s.id_seance " +
                "JOIN Evenement e ON s.id_evenement = e.id_evenement " +
                "JOIN Client c ON b.id_client = c.id_client " +
                "JOIN Place p ON b.id_place = p.id_place " +
                "ORDER BY b.date_achat DESC";

        try (Connection conn = MySQLConnection.connect();
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String placeInfo = "R" + rs.getInt("rangee") + " P" + rs.getInt("numero");
                String clientInfo = rs.getString("nom") + " (" + rs.getString("email") + ")";

                data.add(new ReservationView(
                        rs.getInt("id_billet"),
                        rs.getString("titre"),
                        rs.getString("date_heure"),
                        clientInfo,
                        placeInfo,
                        rs.getString("date_achat")));
            }
            reservations_table.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
            alert.setContentText("Impossible de charger les réservations.");
            alert.show();
        }
    }

    private void setupActionColumn() {
        Callback<TableColumn<ReservationView, Void>, TableCell<ReservationView, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<ReservationView, Void> call(final TableColumn<ReservationView, Void> param) {
                final TableCell<ReservationView, Void> cell = new TableCell<>() {
                    private final Button btnCancel = new Button("Annuler");

                    {
                        btnCancel.setStyle("-fx-background-color: #ffaa00; -fx-text-fill: white; -fx-cursor: hand;");
                        btnCancel.setOnAction(event -> {
                            ReservationView res = getTableView().getItems().get(getIndex());
                            annulerReservation(res);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox managebtn = new HBox(btnCancel);
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

    private void annulerReservation(ReservationView res) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        alert.setTitle("Confirmation d'annulation");
        alert.setHeaderText("Annuler la réservation du client : " + res.getClientNom());
        alert.setContentText(
                "Place : " + res.getPlaceInfo() + " pour l'événement " + res.getEventTitre() + ".\nÊtes-vous sûr ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    billetDAO.supprimer(res.getIdBillet());
                    chargerReservations(); // Refresh the table
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
                    errorAlert.setContentText("Erreur lors de l'annulation de la réservation.");
                    errorAlert.show();
                }
            }
        });
    }

}
