package com.unipampa.compiladorarquitetura16bits;

import com.unipampa.compiladorarquitetura16bits.memory.MemoryManager;
import com.unipampa.compiladorarquitetura16bits.memory.MemorySlot;
import com.unipampa.compiladorarquitetura16bits.model.Registrador;
import com.unipampa.compiladorarquitetura16bits.model.Variavel;
import com.unipampa.compiladorarquitetura16bits.utils.LabelGenerator;
import com.unipampa.compiladorarquitetura16bits.utils.exceptions.CompilationException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompilerController {
    @FXML
    private Label  label_reg0, label_reg1, label_reg2, label_reg3, label_reg4,
            label_reg5, label_reg6, label_reg7, label_mem0,label_mem1, label_mem2, label_mem3, label_mem4, label_mem5, label_mem6, label_mem7, label_mem8,
            label_mem9, label_mem10, label_mem11, label_mem12, label_mem13, label_mem14,
            label_mem15, label_avisos;
    @FXML
    private Pane pane_avisos;
//    @FXML
//    private Label label_valorMem0,label_valorMem1, label_valorMem2, label_valorMem3, label_valorMem4,
//            label_valorMem5, label_valorMem6, label_valorMem7, label_valorMem8, label_valorMem9,
//            label_valorMem10, label_valorMem11, label_valorMem12, label_valorMem13,
//            label_valorMem14, label_valorMem15, label_valorReg0,
//            label_valorReg1, label_valorReg2, label_valorReg3, label_valorReg4, label_valorReg5, label_valorReg6,
//            label_valorReg7;
    @FXML
    private TextArea codingArea;
    @FXML
    private Pane container_regs, container_mem;

    private final Parser parser;
    private final Map<Variavel, Registrador> symbolTable = new HashMap<>();
    private String assemblyCodeGenerated = ""; // Armazena o último assembly gerado


    public CompilerController () {
        this.parser = new Parser(symbolTable);
    }

    @FXML
    private void action() {
        try {
            // Resetar estados antes de cada compilação
            symbolTable.clear();
            parser.reset();

            String code = parser.parse(codingArea.getText());

            // Substituir labels e armazenar o assembly gerado
            assemblyCodeGenerated = LabelGenerator.substituirLabel(code);

            setRegs(symbolTable);
            setMemory(parser.getMemoryManager());

            // Mostrar estatísticas no console
//            System.out.println("\n=== ESTATÍSTICAS ===");
//            System.out.println(parser.getAllocator().getStatistics());
//            System.out.println("\n=== ASSEMBLY GERADO ===");
//            System.out.println(assemblyCodeGenerated);

            // Exibir mensagem de sucesso
            setAviso("Compilação concluída com sucesso!", "#7FB069", "white");

        } catch (CompilationException e) {
            // Capturar erros de compilação e exibir no componente
            setAviso(e.getFormattedMessage(), "#E74C3C", "white");
            System.err.println("Erro de compilação: " + e.getMessage());

        } catch (Exception e) {
            // Capturar outros erros inesperados
            setAviso("Erro inesperado: " + e.getMessage(), "#C0392B", "white");
            e.printStackTrace();
        }
    }

    /**
     * Atualiza o componente de avisos com mensagem, cor de fundo e cor do texto
     */
    private void setAviso(String aviso, String corPane, String corLabel) {
        label_avisos.setText(aviso);
        label_avisos.setStyle("-fx-text-fill: " + corLabel);
        pane_avisos.setStyle("-fx-background-color: " + corPane);
    }

    private void setRegs(Map<Variavel, Registrador> symbolTable) {
        // Array com os labels de valor
//        Label[] labelValores = {
//            label_valorReg0, label_valorReg1, label_valorReg2, label_valorReg3,
//            label_valorReg4, label_valorReg5, label_valorReg6, label_valorReg7
//        };

        // Array com os labels de nome de variável
        Label[] labelNomes = {
            label_reg0, label_reg1, label_reg2, label_reg3,
            label_reg4, label_reg5, label_reg6, label_reg7
        };

        // Limpar todos os labels primeiro
//        for (int i = 0; i < 8; i++) {
//            labelValores[i].setText("0");
//            if (labelNomes[i] != null) {
//                labelNomes[i].setText("-");
//            }
//        }

        // Ordenar entries por número do registrador para exibir na ordem R0, R1, R2...
        List<Map.Entry<Variavel, Registrador>> sortedEntries = new ArrayList<>(symbolTable.entrySet());
        sortedEntries.sort((e1, e2) -> {
            String reg1 = e1.getValue().getNome();
            String reg2 = e2.getValue().getNome();
            int num1 = extrairNumeroRegistrador(reg1);
            int num2 = extrairNumeroRegistrador(reg2);
            return Integer.compare(num1, num2);
        });

        int index = 0;
        for (Map.Entry<Variavel, Registrador> entry : sortedEntries) {
            if (index >= 8) break; // Limite de 8 registradores na UI

            Variavel var = entry.getKey();
            Registrador reg = entry.getValue();

            if (var != null && reg != null) {
                // Extrair número do registrador (ex: "R0" -> 0)
                int regNum = extrairNumeroRegistrador(reg.getNome());

                if (regNum >= 0 && regNum < 8) {
                    // Mostrar valor do registrador
                   // labelValores[regNum].setText(String.valueOf(reg.getValor()));

                    // Mostrar nome da variável
                    if (labelNomes[regNum] != null) {
                        labelNomes[regNum].setText(var.getNome());
                    }
                }
            }
            index++;
        }
    }

    private int extrairNumeroRegistrador(String nomeReg) {
        try {
            return Integer.parseInt(nomeReg.replace("R", "").trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private void setMemory(MemoryManager memoryManager) {
        // Arrays com os labels de memória
        Label[] labelNomesMemoria = {
            label_mem0, label_mem1, label_mem2, label_mem3,
            label_mem4, label_mem5, label_mem6, label_mem7,
            label_mem8, label_mem9, label_mem10, label_mem11,
            label_mem12, label_mem13, label_mem14, label_mem15
        };

//        Label[] labelValoresMemoria = {
//            label_valorMem0, label_valorMem1, label_valorMem2, label_valorMem3,
//            label_valorMem4, label_valorMem5, label_valorMem6, label_valorMem7,
//            label_valorMem8, label_valorMem9, label_valorMem10, label_valorMem11,
//            label_valorMem12, label_valorMem13, label_valorMem14, label_valorMem15
//        };

        // Limpar todos os labels primeiro
        for (int i = 0; i < 16; i++) {
            if (labelNomesMemoria[i] != null) {
                labelNomesMemoria[i].setText("-");
            }
//            if (labelValoresMemoria[i] != null) {
//                labelValoresMemoria[i].setText("0");
//            }
        }

        // Obter mapa de memória do MemoryManager
        Map<String, MemorySlot> memoryMap = memoryManager.getMemoryMap();

        // Ordenar por endereço de memória (0-15)
        List<Map.Entry<String, MemorySlot>> sortedMemory = new ArrayList<>(memoryMap.entrySet());
        sortedMemory.sort((e1, e2) -> Integer.compare(e1.getValue().getAddress(), e2.getValue().getAddress()));

        // Preencher labels com dados da memória
        for (Map.Entry<String, MemorySlot> entry : sortedMemory) {
            MemorySlot slot = entry.getValue();
            int address = slot.getAddress();

            if (address >= 0 && address < 16) {
                // Mostrar nome da variável
                if (labelNomesMemoria[address] != null) {
                    labelNomesMemoria[address].setText(slot.getVarName());
                }

                // Mostrar valor
//                if (labelValoresMemoria[address] != null) {
//                    labelValoresMemoria[address].setText(String.valueOf(slot.getValue()));
//                }
            }
        }
    }

    @FXML
    private void showRegsValues() {
        container_regs.setVisible(true);
        container_mem.setVisible(false);
    }

    @FXML
    private void showMemValues () {
        container_regs.setVisible(false);
        container_mem.setVisible(true);
    }

    @FXML
    private void exibirCreditos() {
        try {
            // Carregar o FXML da tela de créditos
            FXMLLoader loader = new FXMLLoader(getClass().getResource("creditos.fxml"));
            Scene scene = new Scene(loader.load());

            // Criar nova janela (Stage)
            Stage creditosStage = new Stage();
            creditosStage.setTitle("Créditos");
            creditosStage.setScene(scene);
            creditosStage.setResizable(false);

            // Configurar como modal (bloqueia interação com janela principal)
            creditosStage.initModality(Modality.APPLICATION_MODAL);

            // Exibir janela e aguardar até ser fechada
            creditosStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir tela de créditos: " + e.getMessage());
        }
    }

    @FXML
    private void exibirAssembly() {
        try {
            // Verificar se há assembly gerado
            if (assemblyCodeGenerated == null || assemblyCodeGenerated.isEmpty()) {
                System.out.println("Nenhum assembly gerado ainda. Compile o código primeiro.");
                setAviso("Nenhum assembly gerado ainda. Compile o código primeiro.", "yellow", "black");
                return;
            }

            // Carregar o FXML do terminal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("assembly-terminal.fxml"));
            Scene scene = new Scene(loader.load());


            // Obter o controller e passar o código assembly
            AssemblyTerminalController controller = loader.getController();
            controller.setAssemblyCode(assemblyCodeGenerated);

            // Criar nova janela (Stage)
            Stage terminalStage = new Stage();
            terminalStage.setTitle("Assembly - Terminal");
            terminalStage.setScene(scene);
            terminalStage.setResizable(true);

            // Configurar como modal (bloqueia interação com janela principal)
            terminalStage.initModality(Modality.APPLICATION_MODAL);

            // Exibir janela
            terminalStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir terminal de assembly: " + e.getMessage());
        }
    }
}
