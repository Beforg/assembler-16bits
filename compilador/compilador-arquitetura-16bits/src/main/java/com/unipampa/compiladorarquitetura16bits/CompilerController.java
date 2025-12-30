package com.unipampa.compiladorarquitetura16bits;

import com.unipampa.compiladorarquitetura16bits.utils.LabelGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class CompilerController {
    @FXML
    private Label welcomeText;
    @FXML
    private TextArea codingArea;

    private final Parser parser;


    public CompilerController () {
        this.parser = new Parser();
    }

    @FXML
    private void action() {
        String code = parser.parse(codingArea.getText());

        System.out.println(LabelGenerator.substituirLabel(code));
    }
}
