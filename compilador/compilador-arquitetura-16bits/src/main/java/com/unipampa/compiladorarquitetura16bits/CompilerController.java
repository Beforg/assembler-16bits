package com.unipampa.compiladorarquitetura16bits;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class CompilerController {
    @FXML
    private Label welcomeText;
    @FXML
    private TextArea codingArea;

    @FXML
    private void action() {
        Parser parser = new Parser();
        String code = parser.parse(codingArea.getText());
        System.out.println(code);
    }
}
