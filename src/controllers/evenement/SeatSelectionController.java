package controllers.evenement;

import database.MySQLConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import utils.SessionManager;

public class SeatSelectionController {

    @FXML
    private GridPane seatGrid;

    private int idEvenement;
    private int idSeance;
    private int idSalle;
    private int rows;
    private int cols;

    private List<models.Place> allRoomPlaces = new ArrayList<>();
    private List<Integer> reservedSeats = new ArrayList<>();
    private List<Integer> selectedSeats = new ArrayList<>();

    public void initData(int idEvenement, int idSeance, int idSalle) {
        this.idEvenement = idEvenement;
        this.idSeance = idSeance;
        this.idSalle = idSalle;

        loadRoomData();
        loadReservedSeats();
        generateSeatGrid();
    }

    private void loadRoomData() {
        try (Connection conn = MySQLConnection.connect()) {
            // Load dimensions
            PreparedStatement psS = conn.prepareStatement("SELECT nb_rangees, nb_colonnes FROM Salle WHERE id_salle = ?");
            psS.setInt(1, idSalle);
            ResultSet rsS = psS.executeQuery();
            if (rsS.next()) {
                rows = rsS.getInt("nb_rangees");
                cols = rsS.getInt("nb_colonnes");
            }

            // Load all places
            PreparedStatement psP = conn.prepareStatement("SELECT * FROM Place WHERE id_salle = ?");
            psP.setInt(1, idSalle);
            ResultSet rsP = psP.executeQuery();
            while (rsP.next()) {
                allRoomPlaces.add(new models.Place(
                        rsP.getInt("id_place"),
                        rsP.getInt("id_salle"),
                        rsP.getInt("rangee"),
                        rsP.getInt("numero"),
                        rsP.getInt("statut")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadReservedSeats() {
        reservedSeats.clear();
        try (Connection conn = MySQLConnection.connect()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_place FROM Billet WHERE id_seance = ? AND (statut = 'VALIDE' OR statut = 'ATTENTE')");
            ps.setInt(1, idSeance);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                reservedSeats.add(rs.getInt("id_place"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateSeatGrid() {
        seatGrid.getChildren().clear();
        boolean hasSeats = false;

        for (int r = 1; r <= rows; r++) {
            // Row Label (Letter) at the beginning of each row
            javafx.scene.control.Label rowLabel = new javafx.scene.control.Label(String.valueOf((char)('A' + r - 1)));
            rowLabel.setStyle("-fx-text-fill: #bb86fc; -fx-font-weight: bold; -fx-min-width: 30; -fx-alignment: center;");
            seatGrid.add(rowLabel, 0, r);

            for (int c = 1; c <= cols; c++) {
                // Column Label (Number) - only once at the top
                if (r == 1) {
                    javafx.scene.control.Label colLabel = new javafx.scene.control.Label(String.valueOf(c));
                    colLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10; -fx-min-width: 40; -fx-alignment: center;");
                    seatGrid.add(colLabel, c, 0);
                }

                final int finalR = r;
                final int finalC = c;
                
                // Find place at this coordination
                models.Place place = allRoomPlaces.stream()
                        .filter(p -> p.getRangee() == finalR && p.getNumero() == finalC)
                        .findFirst().orElse(null);

                if (place == null || place.getStatut() == 0) {
                    // Empty space
                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    spacer.setPrefSize(40, 40);
                    seatGrid.add(spacer, c, r);
                    continue;
                }

                hasSeats = true;
                int placeId = place.getId_place();
                Button btnSeat = new Button(String.valueOf(c)); // Display seat number
                btnSeat.setPrefSize(40, 40);
                btnSeat.setTooltip(new Tooltip("Rangée " + (char)('A'+r-1) + ", Place " + c));
                
                // Base style: Elegant Dark Purple
                String baseStyle = "-fx-background-color: #2b2b3c; -fx-text-fill: white; -fx-border-color: #bb86fc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 11;";
                String reservedStyle = "-fx-background-color: #444; -fx-text-fill: #888; -fx-background-radius: 5; -fx-opacity: 0.5; -fx-font-size: 11;";
                String selectedStyle = "-fx-background-color: #bb86fc; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 11; -fx-font-weight: bold;";

                if (reservedSeats.contains(placeId)) {
                    btnSeat.setStyle(reservedStyle);
                    btnSeat.setDisable(true);
                } else {
                    btnSeat.setStyle(baseStyle);
                    btnSeat.setOnAction(event -> {
                        if (selectedSeats.contains(placeId)) {
                            selectedSeats.remove(Integer.valueOf(placeId));
                            btnSeat.setStyle(baseStyle);
                        } else {
                            selectedSeats.add(placeId);
                            btnSeat.setStyle(selectedStyle);
                        }
                    });
                }

                seatGrid.add(btnSeat, c, r);
            }
        }

        if (!hasSeats && (rows > 0 || cols > 0)) {
            javafx.scene.control.Label lblWarn = new javafx.scene.control.Label("Cette salle n'est pas encore configurée ou ne contient aucun siège actif.");
            lblWarn.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 16px; -fx-padding: 20;");
            seatGrid.add(lblWarn, 0, 0, cols > 0 ? cols : 1, rows > 0 ? rows : 1);
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
            // No-Gap Policy Validation
            String validationError = checkSelectionValidity();
            if (validationError != null) {
                showAlert(validationError);
                return;
            }

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

    /**
     * Validates that the selection doesn't leave gaps (same row, adjacent seats, no orphans).
     */
    private String checkSelectionValidity() {
        if (selectedSeats.size() <= 1) return null; // No gap possible with 1 seat

        // Get actual Place objects for selection
        List<models.Place> selectedPlaces = allRoomPlaces.stream()
                .filter(p -> selectedSeats.contains(p.getId_place()))
                .collect(Collectors.toList());

        // 1. Same Row Check
        int firstRow = selectedPlaces.get(0).getRangee();
        for (models.Place p : selectedPlaces) {
            if (p.getRangee() != firstRow) {
                return "Toutes les places sélectionnées doivent être sur la même rangée (" + (char)('A' + firstRow - 1) + ").";
            }
        }

        // 2. Contiguity Check
        selectedPlaces.sort(Comparator.comparingInt(models.Place::getNumero));
        for (int i = 0; i < selectedPlaces.size() - 1; i++) {
            if (selectedPlaces.get(i+1).getNumero() - selectedPlaces.get(i).getNumero() != 1) {
                return "Les places doivent être côte à côte. Il y a un trou entre la place " + 
                       selectedPlaces.get(i).getNumero() + " et " + selectedPlaces.get(i+1).getNumero() + ".";
            }
        }

        // 3. Orphan Prevention (No single hole left)
        int minNum = selectedPlaces.get(0).getNumero();
        int maxNum = selectedPlaces.get(selectedPlaces.size() - 1).getNumero();

        // Check LEFT orphan
        if (isOrphan(firstRow, minNum - 1)) {
            return "Attention : Votre sélection laisse un siège vide isolé sur la gauche. Veuillez ne pas laisser de 'trou' d'une seule place.";
        }

        // Check RIGHT orphan
        if (isOrphan(firstRow, maxNum + 1)) {
            return "Attention : Votre sélection laisse un siège vide isolé sur la droite. Veuillez ne pas laisser de 'trou' d'une seule place.";
        }

        return null;
    }

    private boolean isOrphan(int row, int num) {
        // Is this seat even part of the room?
        models.Place target = findPlace(row, num);
        if (target == null || target.getStatut() == 0) return false; // Not a seat or inactive

        // Is it already taken?
        if (reservedSeats.contains(target.getId_place()) || selectedSeats.contains(target.getId_place())) return false;

        // Ok, target is EMPTY. Is it an orphan? 
        // An orphan is empty AND its neighbors are BOTH unavailable (reserved, selected, or wall/inactive)
        
        models.Place leftOfTarget = findPlace(row, num - 1);
        models.Place rightOfTarget = findPlace(row, num + 1);

        boolean leftBlocked = (leftOfTarget == null || leftOfTarget.getStatut() == 0 || 
                               reservedSeats.contains(leftOfTarget.getId_place()) || 
                               selectedSeats.contains(leftOfTarget.getId_place()));
                               
        boolean rightBlocked = (rightOfTarget == null || rightOfTarget.getStatut() == 0 || 
                                reservedSeats.contains(rightOfTarget.getId_place()) || 
                                selectedSeats.contains(rightOfTarget.getId_place()));

        return leftBlocked && rightBlocked;
    }

    private models.Place findPlace(int row, int num) {
        return allRoomPlaces.stream()
                .filter(p -> p.getRangee() == row && p.getNumero() == num)
                .findFirst().orElse(null);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
