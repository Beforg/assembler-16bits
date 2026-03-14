package com.unipampa.compiladorarquitetura16bits;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Controller para a tela de sintaxe do compilador
 */
public class SintaxeController {

    /**
     * Fecha a janela de sintaxe
     */
    @FXML
    private void fecharSintaxe(ActionEvent event) {
        // Obter o Stage através do botão que disparou o evento
        Button btn = (Button) event.getSource();
        Stage stage = (Stage) btn.getScene().getWindow();
        stage.close();
    }

    /**
     * Efeito hover no botão - mouse entra
     */
    @FXML
    private void onMouseEntered(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #6A9A5B; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 30 10 30; -fx-cursor: hand;");
    }

    /**
     * Efeito hover no botão - mouse sai
     */
    @FXML
    private void onMouseExited(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #7FB069; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 30 10 30; -fx-cursor: hand;");
    }
}

