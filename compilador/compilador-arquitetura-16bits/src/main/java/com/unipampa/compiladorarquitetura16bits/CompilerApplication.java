package com.unipampa.compiladorarquitetura16bits;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class CompilerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Criar e exibir a tela de splash (initializer)
        showSplashScreen(stage);
    }

    /**
     * Exibe a tela de splash por 3 segundos antes de abrir a tela principal
     */
    private void showSplashScreen(Stage primaryStage) throws IOException {
        // Carregar o FXML do initializer
        FXMLLoader splashLoader = new FXMLLoader(CompilerApplication.class.getResource("initializer.fxml"));
        Scene splashScene = new Scene(splashLoader.load());

        // Criar stage para splash sem decoração (sem bordas)
        Stage splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setScene(splashScene);
        splashStage.centerOnScreen();
        splashStage.show();

        // Após 3 segundos, fechar splash e abrir tela principal
        new Thread(() -> {
            try {
                Thread.sleep(3000); // 3 segundos

                // Executar na thread do JavaFX
                Platform.runLater(() -> {
                    try {
                        // Fechar splash
                        splashStage.close();

                        // Abrir tela principal
                        showMainWindow(primaryStage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Exibe a janela principal do compilador
     */
    private void showMainWindow(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CompilerApplication.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Compilador para Arquitetura 16 bits");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
