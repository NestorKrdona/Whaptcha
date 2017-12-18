package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * Created by NestorKrdona 13/12/2017
 */
public class ClassifyController
{
    @FXML
    private BorderPane root;
    @FXML
    public Label resultLabel;

    @FXML
    private void initialize()
    {
        resultLabel.setText("");
    }
}
