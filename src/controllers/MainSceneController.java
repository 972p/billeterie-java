package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MainSceneController {

    @FXML
    private TextField tftitre;

    @FXML
    private Label title;

    @FXML
    void btnOk(ActionEvent event) {
        Stage mainWindow = (Stage) tftitre.getScene().getWindow();
        mainWindow.setTitle(tftitre.getText());
    }

}
