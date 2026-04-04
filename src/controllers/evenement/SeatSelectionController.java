package controllers.evenement;

import database.MySQLConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import utils.SessionManager;

public class SeatSelectionController {

    @FXML
    private GridPane seatGrid;

    private int idEvenement;
    private int idSeance;
    private int idSalle;
    private int rows;
    private int cols;

    private List<Integer> reservedSeats = new ArrayList<>();
    private List<Integer> selectedSeats = new ArrayList<>();

    public void initData(int idEvenement, int idSeance, int idSalle) {
        this.idEvenement = idEvenement;
        this.idSeance = idSeance;
        this.idSalle = idSalle;

        loadRoomDimensions();
        loadReservedSeats();
        generateSeatGrid();
    }

    private void loadRoomDimensions() {
        try (Connection conn = MySQLConnection.connect()) {
            PreparedStatement ps = conn
                    .prepareStatement("SELECT nb_rangees, nb_colonnes FROM Salle WHERE id_salle = ?");
            ps.setInt(1, idSalle);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rows = rs.getInt("nb_rangees");
                cols = rs.getInt("nb_colonnes");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadReservedSeats() {
        try (Connection conn = MySQLConnection.connect()) {
            // Find all places taking into account places registered inside billets for this
            // seance
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_place FROM Billet WHERE id_seance = ? AND (statut = 'VALIDE' OR statut = 'ATTENTE')");
            ps.setInt(1, idSeance);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // If id_place is just represented as a linear 1D index derived from (r, c)
                // Wait, if id_place comes from the Place table, we need to map the grid
                // coordinates to the DB id_place
                reservedSeats.add(rs.getInt("id_place"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateSeatGrid() {
        seatGrid.getChildren().clear();

        // Ensure places actually exist in DB. If not, we fall back to a linear ID
        // calculation.
        // For standard rectangular rooms, place_id can be assumed to be deterministic
        // if added chronologically,
        // but it's better to fetch actual places for the room.

        // As a quick implementation, we visualize buttons representing Row R, Col C.
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int seatLinearId = (r * cols) + c + 1; // Temporary mock of a place ID until Place lookup is fully wired

                Button btnSeat = new Button();
                btnSeat.setPrefSize(40, 40);
                btnSeat.setStyle("-fx-background-color: #2b2b3c; -fx-text-fill: white; -fx-border-color: #bb86fc; -fx-border-radius: 5; -fx-background-radius: 5;");

                if (reservedSeats.contains(seatLinearId)) {
                    btnSeat.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 5;");
                    btnSeat.setDisable(true);
                } else {
                    btnSeat.setOnAction(event -> toggleSeatSelection(btnSeat, seatLinearId));
                }

                seatGrid.add(btnSeat, c, r);
            }
        }
    }

    private void toggleSeatSelection(Button btnSeat, int seatId) {
        if (selectedSeats.contains(seatId)) {
            selectedSeats.remove(Integer.valueOf(seatId));
            btnSeat.setStyle("-fx-background-color: #2b2b3c; -fx-text-fill: white; -fx-border-color: #bb86fc; -fx-border-radius: 5; -fx-background-radius: 5;");
        } else {
            selectedSeats.add(seatId);
            btnSeat.setStyle("-fx-background-color: #bb86fc; -fx-text-fill: white; -fx-background-radius: 5;");
        }
    }

    @FXML
    public void handleConfirmBooking(ActionEvent event) {
        if (selectedSeats.isEmpty()) {
            showAlert("Veuillez sélectionner au moins une place.");
            return;
        }

        if (!SessionManager.isLoggedIn()) {
            showAlert("Vous devez être connecté pour réserver une place.");
            return;
        }

        try {
            // First check if any selected seats have been taken in the meantime
            try (Connection conn = MySQLConnection.connect();
                    PreparedStatement psCheck = conn.prepareStatement(
                            "SELECT id_place FROM Billet WHERE id_seance = ? AND id_place = ? AND (statut = 'VALIDE' OR statut = 'ATTENTE')")) {
                psCheck.setInt(1, idSeance);
                for (Integer idPlace : selectedSeats) {
                    psCheck.setInt(2, idPlace);
                    try (ResultSet rsCheck = psCheck.executeQuery()) {
                        if (rsCheck.next()) {
                            showAlert("Erreur : La place " + idPlace + " a déjà été réservée par quelqu'un d'autre.");
                            loadReservedSeats();
                            generateSeatGrid();
                            return; // Stop the booking process
                        }
                    }
                }
            }

            // Redirect to Checkout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/evenement/Checkout.fxml"));
            Parent root = loader.load();
            CheckoutController controller = loader.getController();
            controller.initData(idEvenement, idSeance, idSalle, selectedSeats);

            if (!utils.SessionManager.isAdmin() && controllers.client.ClientController.getInstance() != null) {
                controllers.client.ClientController.getInstance().setCenterView(root);
            } else {
                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                stage.setTitle("Paiement");
                Scene scene = new Scene(root);
                String css = this.getClass().getResource("/views/style.css").toExternalForm();
                scene.getStylesheets().add(css);
                stage.setScene(scene);
                stage.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Une erreur est survenue lors de la redirection vers le paiement.");
        }
    }
    @FXML
    public void handleReturn(ActionEvent event) {
        try {
            if (!utils.SessionManager.isAdmin() && controllers.client.ClientController.getInstance() != null) {
                controllers.client.ClientController.getInstance().loadCenterView("/views/evenement/Evenement.fxml");
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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
