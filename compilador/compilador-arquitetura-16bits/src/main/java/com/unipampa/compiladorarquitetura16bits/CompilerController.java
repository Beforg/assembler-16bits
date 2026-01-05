package com.unipampa.compiladorarquitetura16bits;

import com.unipampa.compiladorarquitetura16bits.utils.LabelGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.util.HashMap;
import java.util.Map;

public class CompilerController {
    @FXML
    private Label label_valorReg0,
            label_valorReg1,
            label_valorReg2,
            label_valorReg3,
            label_valorReg4,
            label_valorReg5,
            label_valorReg6,
            label_valorReg7,
            label_reg0,
            label_reg1,
            label_reg2,
            label_reg3,
            label_reg4,
            label_reg5;
    @FXML
    private TextArea codingArea;

    private final Parser parser;
    private final Map<String, String> symbolTable = new HashMap<>();


    public CompilerController () {
        this.parser = new Parser(symbolTable);
    }

    @FXML
    private void action() {
        String code = parser.parse(codingArea.getText());
        setRegs(symbolTable);
        System.out.println(LabelGenerator.substituirLabel(code));
    }

    private void setRegs(Map<String, String> symbolTable) {
        label_valorReg0.setText(symbolTable.get("x"));
        label_valorReg1.setText(symbolTable.get("R1"));
        label_valorReg2.setText(symbolTable.get("R2"));
        label_valorReg3.setText(symbolTable.get("R3"));
        label_valorReg4.setText(symbolTable.get("R4"));
        label_valorReg5.setText(symbolTable.get("R5"));
        label_valorReg6.setText(symbolTable.get("R6"));
        label_valorReg7.setText(symbolTable.get("R7"));
    }
}
