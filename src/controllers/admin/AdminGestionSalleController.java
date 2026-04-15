package controllers.admin;

import DAO.SalleDAO;
import models.Salle;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class AdminGestionSalleController extends Application {

    private SalleDAO salleDAO = new SalleDAO(); // Ton DAO
    private GridPane grillePreview = new GridPane(); // La fameuse grille
    private Label labelCapacite = new Label("Capacité totale : 0 places");

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Administration - Créer une Salle");

        // --- 1. LES CHAMPS DU FORMULAIRE ---
        TextField champNom = new TextField();
        champNom.setPromptText("Nom de la salle");

        TextField champRangees = new TextField();
        champRangees.setPromptText("Nombre de rangées");

        TextField champColonnes = new TextField();
        champColonnes.setPromptText("Nombre de colonnes");

        Button btnSauvegarder = new Button("Sauvegarder la salle");

        // --- 2. METTRE À JOUR LA GRILLE EN TEMPS RÉEL ---
        // On écoute les changements dans les champs de texte pour dessiner la grille
        // instantanément
        champRangees.textProperty()
                .addListener((obs, oldVal, newVal) -> dessinerGrille(champRangees.getText(), champColonnes.getText()));
        champColonnes.textProperty()
                .addListener((obs, oldVal, newVal) -> dessinerGrille(champRangees.getText(), champColonnes.getText()));

        // --- 3. L'ACTION DU BOUTON SAUVEGARDER ---
        btnSauvegarder.setOnAction(e -> {
            try {
                String nom = champNom.getText();
                int rangees = Integer.parseInt(champRangees.getText());
                int colonnes = Integer.parseInt(champColonnes.getText());
                int idLieu = 1; // À remplacer par ton choix de lieu réel

                Salle nouvelleSalle = new Salle(idLieu, nom, rangees, colonnes);
                salleDAO.ajouter(nouvelleSalle);

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "La salle a été créée avec succès !");
                alert.showAndWait();

            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Veuillez entrer des nombres valides pour les dimensions.").show();
            } catch (SQLException ex) {
                new Alert(Alert.AlertType.ERROR, "Erreur base de données : " + ex.getMessage()).show();
            }
        });

        // --- 4. MISE EN PAGE (Layout) ---
        grillePreview.setHgap(5); // Espace horizontal entre les sièges
        grillePreview.setVgap(5); // Espace vertical entre les sièges
        grillePreview.setAlignment(Pos.CENTER);

        VBox formulaire = new VBox(10, new Label("Nom :"), champNom, new Label("Rangées :"), champRangees,
                new Label("Colonnes :"), champColonnes, btnSauvegarder, labelCapacite);
        formulaire.setPadding(new Insets(20));
        formulaire.setPrefWidth(250);

        HBox root = new HBox(20, formulaire, grillePreview);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- 5. LA LOGIQUE DE DESSIN DE LA GRILLE ---
    private void dessinerGrille(String textRangees, String textColonnes) {
        grillePreview.getChildren().clear(); // On nettoie l'ancienne grille

        try {
            int rangees = Integer.parseInt(textRangees);
            int colonnes = Integer.parseInt(textColonnes);

            if (rangees > 0 && colonnes > 0) {
                labelCapacite.setText("Capacité totale : " + (rangees * colonnes) + " places");
                int numeroSiege = 1;

                // Double boucle pour placer les boutons (sièges) dans le GridPane
                for (int r = 0; r < rangees; r++) {
                    for (int c = 0; c < colonnes; c++) {
                        Button siege = new Button(String.valueOf(numeroSiege));
                        siege.setPrefSize(40, 40); // Taille fixe pour faire de jolis carrés
                        siege.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");

                        // L'ajout dans le GridPane se fait par (colonne, ligne)
                        grillePreview.add(siege, c, r);
                        numeroSiege++;
                    }
                }
            } else {
                labelCapacite.setText("Capacité totale : 0 places");
            }
        } catch (NumberFormatException e) {
            labelCapacite.setText("Capacité totale : 0 places");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}