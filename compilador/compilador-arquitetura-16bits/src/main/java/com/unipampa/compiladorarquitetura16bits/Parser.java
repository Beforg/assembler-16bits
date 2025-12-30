package com.unipampa.compiladorarquitetura16bits;

import com.unipampa.compiladorarquitetura16bits.model.CompiladorSintaxe;
import com.unipampa.compiladorarquitetura16bits.model.Opcode;

import java.util.HashMap;
import java.util.Map;

public class Parser {
    private final Map<String, String> symbolTable = new HashMap<>();
    private final int MAX_INSTRUCTIONS = 15;

    public String parse(String codigoFonte) {
        StringBuilder codigoAsm = new StringBuilder();
        String[] linhas = codigoFonte.split("\\n");
        int regAtual = 0;

        for (String linha : linhas) {
            linha = linha.trim();

            if (linha.isEmpty() || linha.startsWith("//")) {
                continue;
            }

            if (linha.startsWith(CompiladorSintaxe.INTEIRO.getSintaxe())) {
                montarInstrucaoLoadInteiro(linha, symbolTable, regAtual, codigoAsm);
                regAtual++;

            } else if (linha.contains(CompiladorSintaxe.ATRIBUICAO.getSintaxe())) {

                String[] partes = linha.replace(";", "").split("=");
                String variavelUsada = partes[0].trim();
                String expressao = partes[1].trim();
                String registradorDaVariavelUsada = symbolTable.get(variavelUsada);

                if (registradorDaVariavelUsada == null) {
                    throw new IllegalArgumentException("Variável " + variavelUsada + " não declarada.");
                }

                if (expressao.contains(CompiladorSintaxe.SOMA.getSintaxe())) {
                    montarSoma(expressao, codigoAsm, registradorDaVariavelUsada);
                }

                else if (expressao.contains(CompiladorSintaxe.SUBTRACAO.getSintaxe())) {
                    montarSubtracao(expressao, "-", codigoAsm, Opcode.SUB, registradorDaVariavelUsada);
                }

                else if (expressao.contains(CompiladorSintaxe.MULTIPLICACAO.getSintaxe())) {
                    montarMultiplicacao(expressao, "\\*", codigoAsm, Opcode.MUL, registradorDaVariavelUsada);
                }

            } else if (linha.startsWith("se")) {

            }


        }
        return codigoAsm.toString();
    }

    private void montarMultiplicacao(String expressao, String regex, StringBuilder codigoAsm, Opcode mul, String registradorDaVariavelUsada) {
        String[] ops = expressao.split(regex);
        String reg1 = symbolTable.get(ops[0].trim());
        String reg2 = symbolTable.get(ops[1].trim());

        validateRegs(reg1, reg2);
        codigoAsm.append(mul.getCode())
                .append(registradorDaVariavelUsada)
                .append(",")
                .append(reg1)
                .append(",")
                .append(reg2)
                .append("\n");
    }

    private void montarSubtracao(String expressao, String regex, StringBuilder codigoAsm, Opcode sub, String registradorDaVariavelUsada) {
        String[] ops = expressao.split(regex);
        String reg1 = symbolTable.get(ops[0].trim());
        String reg2 = symbolTable.get(ops[1].trim());

        validateRegs(reg1, reg2);
        codigoAsm.append(sub.getCode())
                .append(registradorDaVariavelUsada)
                .append(",")
                .append(reg1)
                .append(",")
                .append(reg2)
                .append("\n");
    }

    private void montarSoma(String expressao, StringBuilder codigoAsm, String registradorDaVariavelUsada) {
        String[] ops = expressao.split("\\+");
        System.out.println(ops[0] + " - " + ops[1]);
        String reg1 = symbolTable.get(ops[0].trim());
        String reg2 = symbolTable.get(ops[1].trim());

        validateRegs(reg1, reg2);
        codigoAsm.append(Opcode.SUM.getCode())
                .append(registradorDaVariavelUsada)
                .append(",")
                .append(reg1)
                .append(",")
                .append(reg2)
                .append("\n");
    }

    private void montarInstrucaoLoadInteiro(String linha, Map<String, String> symbolTable, int regAtual, StringBuilder codigoAsm) {
        int valor;
        String nomeDaNovaVariavel;
        String registradorDaVariavel;

        String[] partes = linha.replace(";", "").split("=");
        if (variavelEstaInicializada(partes)) {
            nomeDaNovaVariavel = extrairNomeVariavel(partes);
            valor = extrairValorInteiro(partes);
            registradorDaVariavel = montarRegistradorParaVariavel(regAtual);
            symbolTable.put(nomeDaNovaVariavel, registradorDaVariavel);
            codigoAsm.append(Opcode.LDA.getCode()).append(registradorDaVariavel).append(",").append(valor).append("\n");
        } else {
            nomeDaNovaVariavel = extrairNomeVariavel(partes);
            registradorDaVariavel = montarRegistradorParaVariavel(regAtual);
            symbolTable.put(nomeDaNovaVariavel, registradorDaVariavel);
            codigoAsm.append(Opcode.LDA.getCode()).append(registradorDaVariavel).append(",").append("0").append("\n");
        }

    }

    private String montarRegistradorParaVariavel(int regAtual) {
        return "R" + regAtual;
    }

    private String extrairNomeVariavel(String[] partes) {
        return partes[0].replace("inteiro", "").trim();
    }

    private int extrairValorInteiro(String[] partes) {

        if (verificarSeContemOperacaoNaAtribuicao(partes[1])) {
            System.out.println("Expressão com operação detectada: " + partes[1]);
            throw new IllegalArgumentException("Inicialização com expressão não suportada ainda.");
        } else {
            int valor = Integer.parseInt(partes[1].trim());
            validateInt(valor);
            return valor;
        }
    }

    private boolean verificarSeContemOperacaoNaAtribuicao(String expressao) {
        return expressao.contains(CompiladorSintaxe.SOMA.getSintaxe()) ||
               expressao.contains(CompiladorSintaxe.SUBTRACAO.getSintaxe()) ||
               expressao.contains(CompiladorSintaxe.MULTIPLICACAO.getSintaxe());
    }


    private boolean variavelEstaInicializada(String[] partes) {
        return partes.length == 2 ;
    }

    private void validateRegs(String reg1, String reg2) {
        if (reg1 == null || reg2 == null) {
            throw new IllegalArgumentException("Variável não declarada na expressão.");
        }
    }

    private void validateInt(int value) {
        if (value < 0 || value > 15) {
            throw new IllegalArgumentException("Valor inteiro fora do intervalo permitido (0-15).");
        }
    }
}
