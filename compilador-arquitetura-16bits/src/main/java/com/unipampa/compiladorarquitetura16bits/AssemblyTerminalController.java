package com.unipampa.compiladorarquitetura16bits;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Controller para a tela de terminal que exibe o assembly gerado
 */
public class AssemblyTerminalController {

    @FXML
    private TextArea terminalArea;

    private String assemblyCode;

    /**
     * Define o código assembly a ser exibido no terminal
     */
    public void setAssemblyCode(String code) {
        this.assemblyCode = code;
        if (terminalArea != null) {
            terminalArea.setText(code);
            terminalArea.positionCaret(0); // Posiciona o cursor no início
        }
    }

    /**
     * Inicializa o controller após o FXML ser carregado
     */
    @FXML
    private void initialize() {
        if (assemblyCode != null && terminalArea != null) {
            terminalArea.setText(assemblyCode);
        }
    }

    /**
     * Copia o assembly para a área de transferência
     */
    @FXML
    private void copiarAssembly() {
        if (assemblyCode != null && !assemblyCode.isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(assemblyCode);
            clipboard.setContent(content);

            System.out.println("Assembly copiado para área de transferência!");
        }
    }

    /**
     * Fecha a janela do terminal
     */
    @FXML
    private void fecharTerminal(ActionEvent event) {
        Button btn = (Button) event.getSource();
        Stage stage = (Stage) btn.getScene().getWindow();
        stage.close();
    }

    /**
     * Efeito hover no botão Copiar - mouse entra
     */
    @FXML
    private void onCopiarMouseEntered(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #6A9A5B; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 25 8 25; -fx-cursor: hand;");
    }

    /**
     * Efeito hover no botão Copiar - mouse sai
     */
    @FXML
    private void onCopiarMouseExited(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #7FB069; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 25 8 25; -fx-cursor: hand;");
    }

    /**
     * Efeito hover no botão Fechar - mouse entra
     */
    @FXML
    private void onFecharMouseEntered(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #404040; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 25 8 25; -fx-cursor: hand;");
    }

    /**
     * Efeito hover no botão Fechar - mouse sai
     */
    @FXML
    private void onFecharMouseExited(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 25 8 25; -fx-cursor: hand;");
    }
}

