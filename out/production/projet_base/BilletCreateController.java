package controllers.billet;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

public class BilletCreateController {

    @FXML
    private ComboBox<?> clientCombo;

    @FXML
    private ComboBox<?> eventCombo;

    @FXML
    private ComboBox<?> seanceCombo;

    @FXML
    private ComboBox<?> tarifCombo;

    @FXML
    private Button validateBtn;

    @FXML
    void handleCreate(ActionEvent event) {

    }

}
