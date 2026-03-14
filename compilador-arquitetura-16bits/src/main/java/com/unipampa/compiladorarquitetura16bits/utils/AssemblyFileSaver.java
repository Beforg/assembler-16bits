package com.unipampa.compiladorarquitetura16bits.utils;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe utilitária responsável por salvar o código Assembly gerado em arquivo .asm
 */
public class AssemblyFileSaver {

    /**
     * Salva o código Assembly em um arquivo .asm
     * Abre um diálogo para o usuário escolher o local e nome do arquivo
     *
     * @param assemblyCode O código Assembly a ser salvo
     * @param ownerStage   O Stage pai (janela principal) para o diálogo
     * @return true se o arquivo foi salvo com sucesso, false caso contrário
     */
    public static boolean salvarArquivo(String assemblyCode, Stage ownerStage) {
        if (assemblyCode == null || assemblyCode.trim().isEmpty()) {
            System.err.println("Código Assembly vazio. Nada para salvar.");
            return false;
        }

        // Criar FileChooser para diálogo de salvamento
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Assembly");

        // Definir nome padrão com timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName("assembly_" + timestamp + ".asm");

        // Adicionar filtros de extensão
        FileChooser.ExtensionFilter asmFilter = new FileChooser.ExtensionFilter("Assembly Files (*.asm)", "*.asm");
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All Files (*.*)", "*.*");

        fileChooser.getExtensionFilters().addAll(asmFilter, txtFilter, allFilter);
        fileChooser.setSelectedExtensionFilter(asmFilter);

        // Abrir diálogo de salvamento
        File arquivo = fileChooser.showSaveDialog(ownerStage);

        if (arquivo != null) {
            return escreverArquivo(arquivo, assemblyCode);
        } else {
            System.out.println("Salvamento cancelado pelo usuário.");
            return false;
        }
    }

    /**
     * Escreve o conteúdo do Assembly no arquivo especificado
     *
     * @param arquivo      O arquivo onde será salvo
     * @param assemblyCode O código Assembly a ser escrito
     * @return true se escreveu com sucesso, false em caso de erro
     */
    private static boolean escreverArquivo(File arquivo, String assemblyCode) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo))) {
            // Adicionar cabeçalho ao arquivo
            writer.write("# ==================================================");
            writer.newLine();
            writer.write("# Assembly gerado pelo Compilador 16-bits");
            writer.newLine();
            writer.write("# Data: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            writer.newLine();
            writer.write("# ==================================================");
            writer.newLine();
            writer.newLine();

            // Escrever código Assembly
            writer.write(assemblyCode);

            // Adicionar rodapé
            writer.newLine();
            writer.newLine();
            writer.write("# ==================================================");
            writer.newLine();
            writer.write("# Fim do Assembly");
            writer.newLine();
            writer.write("# ==================================================");

            System.out.println("Assembly salvo com sucesso em: " + arquivo.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Salva o código Assembly diretamente em um arquivo específico (sem diálogo)
     *
     * @param assemblyCode O código Assembly a ser salvo
     * @param caminhoArquivo O caminho completo do arquivo
     * @return true se o arquivo foi salvo com sucesso, false caso contrário
     */
    public static boolean salvarArquivoDireto(String assemblyCode, String caminhoArquivo) {
        if (assemblyCode == null || assemblyCode.trim().isEmpty()) {
            System.err.println("Código Assembly vazio. Nada para salvar.");
            return false;
        }

        if (caminhoArquivo == null || caminhoArquivo.trim().isEmpty()) {
            System.err.println("Caminho de arquivo inválido.");
            return false;
        }

        File arquivo = new File(caminhoArquivo);
        return escreverArquivo(arquivo, assemblyCode);
    }
}

