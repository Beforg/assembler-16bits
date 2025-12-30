package com.unipampa.compiladorarquitetura16bits;

import com.unipampa.compiladorarquitetura16bits.model.CompiladorSintaxe;
import com.unipampa.compiladorarquitetura16bits.model.Opcode;
import com.unipampa.compiladorarquitetura16bits.utils.CondicionalUtils;
import com.unipampa.compiladorarquitetura16bits.utils.IntegerUtils;
import com.unipampa.compiladorarquitetura16bits.utils.LabelGenerator;
import com.unipampa.compiladorarquitetura16bits.utils.LabelsCompilador;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Parser {
    private final Map<String, String> symbolTable = new HashMap<>();
    private final int MAX_INSTRUCTIONS = 15;



    private final Deque<IfLabels> ifLabelStack = new ArrayDeque<>();

    private int registradorAtualLivre = 0;

    private static class IfLabels {
        final String thenLabel;
        final String elseLabel;
        final String endLabel;

        IfLabels(String thenLabel, String elseLabel, String endLabel) {
            this.thenLabel = thenLabel;
            this.elseLabel = elseLabel;
            this.endLabel = endLabel;
        }
    }

    public String parse(String codigoFonte) {
        StringBuilder codigoAsm = new StringBuilder();
        String[] linhas = codigoFonte.split("\\n");

        for (int i = 0; i < linhas.length; i++) {
            String linha = linhas[i].trim();

            if (linha.isEmpty() || linha.startsWith("//")) {
                continue;
            }

            if (linha.startsWith(CompiladorSintaxe.INTEIRO.getSintaxeEmString())) {
                montarInstrucaoLoadInteiro(linha, symbolTable, registradorAtualLivre, codigoAsm);
                registradorAtualLivre++;

            } else if (linha.startsWith(CompiladorSintaxe.SE.getSintaxeEmString()) && !linha.contains(CompiladorSintaxe.SENAO.getSintaxeEmString())) {
                if (sePosuiSenao(linhas, linha, i)) {
                    String operandoEsquerdo = CondicionalUtils.extrairOperandoEsquerdo(linha);
                    String operandoDireito = CondicionalUtils.extrairOperandoDireito(linha);
                    String operadorCondicionalOpcode = CondicionalUtils.extrairOperadorCondicional(linha);

                    String labelElse = LabelGenerator.gerarLabel(LabelsCompilador.SENAO_INICIO);
                    String labelThen = LabelGenerator.gerarLabel(LabelsCompilador.SE_INICIO);
                    String labelEnd = LabelGenerator.gerarLabel(LabelsCompilador.FIM_SE);
                    ifLabelStack.push(new IfLabels(labelThen, labelElse, labelEnd));

                    operandoEsquerdo = montarOperador(operandoEsquerdo, symbolTable, codigoAsm, registradorAtualLivre);
                    operandoDireito = montarOperador(operandoDireito, symbolTable, codigoAsm, registradorAtualLivre);

                    montarInstrucaoSe(codigoAsm, operandoEsquerdo, operandoDireito, operadorCondicionalOpcode, labelThen);
                    montarInstrucaoJump(codigoAsm, labelElse);
                    codigoAsm.append(labelThen).append(":\n");

                } else {
                    codigoAsm.append("// Início do bloco SE sem SENÃO\n");
                }
            } else if (linha.trim().contains(CompiladorSintaxe.SENAO.getSintaxeEmString())) {
                if (ifLabelStack.isEmpty()) {
                    throw new IllegalStateException("Encontrado 'senao' sem 'se' correspondente.");
                }
                IfLabels labels = ifLabelStack.pop();
                // salta do bloco then para o fim usando a mesma label
                codigoAsm.append(Opcode.JMP.getCode()).append(labels.endLabel).append("\n");
                codigoAsm.append(labels.elseLabel).append(":\n");
                int fimSenao = montarInstrucaoSenao(i, linhas, codigoAsm, labels.endLabel);
                i = fimSenao;
                codigoAsm.append(labels.endLabel).append(":\n");

            } else if (linha.contains(CompiladorSintaxe.ATRIBUICAO.getSintaxeEmString()) ) {

                String[] partes = linha.replace(";", "").split("=");
                String variavelUsada = partes[0].trim();
                String expressao = partes[1].trim();
                String registradorDaVariavelUsada = symbolTable.get(variavelUsada);

                if (registradorDaVariavelUsada == null) {
                    throw new IllegalArgumentException("Variável " + variavelUsada + " não declarada.");
                }

                if (expressao.contains(CompiladorSintaxe.SOMA.getSintaxeEmString())) {
                    montarInstrucaoSoma(expressao, codigoAsm, registradorDaVariavelUsada, registradorAtualLivre);
                }

                else if (expressao.contains(CompiladorSintaxe.SUBTRACAO.getSintaxeEmString())) {
                    montarInstrucaoSubtracao(expressao, "-", codigoAsm, Opcode.SUB, registradorDaVariavelUsada);
                }

                else if (expressao.contains(CompiladorSintaxe.MULTIPLICACAO.getSintaxeEmString())) {
                    montarInstrucaoMultiplicacao(expressao, "\\*", codigoAsm, Opcode.MUL, registradorDaVariavelUsada);
                }

            }


        }
        return codigoAsm.toString();
    }

    private String montarOperador(String operador, Map<String, String> symbolTable, StringBuilder codigoAsm, int registradorAtualLivre) {
        String registradorFonte;

        if (IntegerUtils.verificaSeEhInteiro(operador)) {
            int valorDoInteiro = Integer.parseInt(operador.trim());
            validateInt(valorDoInteiro);
            registradorFonte = "R" + registradorAtualLivre;
            codigoAsm.append(Opcode.LDA.getCode())
                    .append(registradorFonte)
                    .append(",")
                    .append(valorDoInteiro)
                    .append("\n");
            return registradorFonte;
        } else {
            String registradorDaVariavel = symbolTable.get(operador);
            validateRegs(registradorDaVariavel, registradorDaVariavel);
            return registradorDaVariavel;
        }
    }

    private void montarInstrucaoSe(
            StringBuilder codigoAsm,
            String operandoEsquerdo,
            String operandoDireito,
            String operadorCondicionalOpcode,
            String labelInicioSe) {
        codigoAsm.append(operadorCondicionalOpcode)
                .append(operandoEsquerdo)
                .append(",")
                .append(operandoDireito)
                .append(",")
                .append(labelInicioSe)
                .append("\n");
    }

    private int montarInstrucaoSenao(int i, String[] linhas, StringBuilder codigoAsm, String labelFim) {
        int j = i + 1;
        while (j < linhas.length) {
            String atual = linhas[j].trim();
            if (atual.isEmpty() || atual.startsWith("//")) { j++; continue; }
            if (atual.endsWith("}")) return j; // fecha o senao
            processLinhaBasica(atual, codigoAsm);
            j++;
        }
        return j;
    }

    private void processLinhaBasica(String linha, StringBuilder codigoAsm) {
        if (linha.startsWith(CompiladorSintaxe.INTEIRO.getSintaxeEmString())) {
            montarInstrucaoLoadInteiro(linha, symbolTable, registradorAtualLivre, codigoAsm);
            registradorAtualLivre++;
        } else if (linha.contains(CompiladorSintaxe.ATRIBUICAO.getSintaxeEmString())) {
            String[] partes = linha.replace(";", "").split("=");
            String variavelUsada = partes[0].trim();
            String expressao = partes[1].trim();
            String registradorDaVariavelUsada = symbolTable.get(variavelUsada);
            if (registradorDaVariavelUsada == null) {
                throw new IllegalArgumentException("Variável " + variavelUsada + " não declarada.");
            }
            if (expressao.contains(CompiladorSintaxe.SOMA.getSintaxeEmString())) {
                montarInstrucaoSoma(expressao, codigoAsm, registradorDaVariavelUsada, registradorAtualLivre);
            } else if (expressao.contains(CompiladorSintaxe.SUBTRACAO.getSintaxeEmString())) {
                montarInstrucaoSubtracao(expressao, "-", codigoAsm, Opcode.SUB, registradorDaVariavelUsada);
            } else if (expressao.contains(CompiladorSintaxe.MULTIPLICACAO.getSintaxeEmString())) {
                montarInstrucaoMultiplicacao(expressao, "\\*", codigoAsm, Opcode.MUL, registradorDaVariavelUsada);
            }
        }
    }


    private void montarInstrucaoJump(StringBuilder codigoAsm, String labelJump) {
        codigoAsm.append(Opcode.JMP.getCode())
                .append(labelJump)
                .append("\n");
    }

    private boolean sePosuiSenao(String[] linhas, String linha, int i) {
        boolean possuiSenao = false;
        int j = i + 1;
        int profundidadeDoBloco = 0;

        CondicionalUtils.verificarEstruturaDaCondicao(linha);
        if (linha.contains("{")) {
            profundidadeDoBloco = 1;
        }

        while (j < linhas.length) {
            String proximaLinha = linhas[j].trim();



            if (proximaLinha.isEmpty() || proximaLinha.startsWith("//")) {
                j++;
                continue;
            }


            if (proximaLinha.contains("{") && !proximaLinha.contains(CompiladorSintaxe.SENAO.getSintaxeEmString())) profundidadeDoBloco++;
            if (proximaLinha.contains("}")) {
                profundidadeDoBloco = Math.max(0, profundidadeDoBloco - 1);
            }


            if (profundidadeDoBloco == 0 && proximaLinha.contains(CompiladorSintaxe.SENAO.getSintaxeEmString())) {
                possuiSenao = true;
                break;
            }


            if (profundidadeDoBloco == 0 && (proximaLinha.startsWith(CompiladorSintaxe.SE.getSintaxeEmString())
                    || proximaLinha.endsWith("}"))) {
            }

            j++;
        }
        return possuiSenao;
    }



    private void montarInstrucaoMultiplicacao(String expressao, String regex, StringBuilder codigoAsm, Opcode mul, String registradorDaVariavelUsada) {
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

    private void montarInstrucaoSubtracao(String expressao, String regex, StringBuilder codigoAsm, Opcode sub, String registradorDaVariavelUsada) {
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

    private void montarInstrucaoSoma(String expressao, StringBuilder codigoAsm, String registradorDaVariavelUsada, int registradorAtualLivre) {
        String[] elementosDaExpressao = expressao.split("\\+");
        int contagemParaMontarRegs = 0;
        int somaDasConstantes = 0;

        if (verificarSeContemMaisDeDoisTermos(elementosDaExpressao)) {
            String registradorOperando1 = null;
            String registradorOperando2;
            String registradorParaImediato;
            String nomeDaVariavel = "";

            for (String valorOuVariavelDaExpressao : elementosDaExpressao) {
                if (IntegerUtils.verificaSeEhInteiro(valorOuVariavelDaExpressao)) {
                    int valorDoInteiro = Integer.parseInt(valorOuVariavelDaExpressao.trim());
                    somaDasConstantes += valorDoInteiro;

                    if (somaDasConstantes > 15) {
                        throw new IllegalArgumentException("Soma das constantes excede o valor máximo permitido (15).");
                    }

                } else {
                    if (contagemParaMontarRegs == 0) {
                        registradorOperando1 = symbolTable.get(valorOuVariavelDaExpressao.trim());
                        validateRegs(registradorOperando1, registradorOperando1);
                        nomeDaVariavel = valorOuVariavelDaExpressao.trim();
                        contagemParaMontarRegs++; // incrementa após setar o primeiro operando
                        continue;
                    }

                    if (contagemParaMontarRegs == 1 && registradorOperando1 != null) {
                        registradorOperando2 = symbolTable.get(valorOuVariavelDaExpressao.trim());
                        validateRegs(registradorOperando1, registradorOperando2);
                        codigoAsm.append(Opcode.SUM.getCode())
                                .append(registradorDaVariavelUsada)
                                .append(",")
                                .append(registradorOperando1)
                                .append(",")
                                .append(registradorOperando2)
                                .append("\n");
                        contagemParaMontarRegs = 0;
                    } else {
                        contagemParaMontarRegs++;
                    }

                }
            }
            if (somaDasConstantes > 0) {
                registradorParaImediato = "R" + (registradorAtualLivre);
                validateRegs(registradorParaImediato, registradorParaImediato);
                codigoAsm.append(Opcode.LDA.getCode())
                        .append(registradorParaImediato)
                        .append(",")
                        .append(somaDasConstantes)
                        .append("\n");
                registradorAtualLivre++;

                codigoAsm.append(Opcode.SUM.getCode())
                        .append(registradorDaVariavelUsada)
                        .append(",")
                        .append(registradorDaVariavelUsada)
                        .append(",")
                        .append(registradorParaImediato)
                        .append("\n");
            }
            if (contagemParaMontarRegs == 1 && !nomeDaVariavel.isEmpty()) {
                String registradorRestante = symbolTable.get(nomeDaVariavel);
                validateRegs(registradorRestante, registradorRestante);
                codigoAsm.append(Opcode.SUM.getCode())
                        .append(registradorDaVariavelUsada)
                        .append(",")
                        .append(registradorDaVariavelUsada)
                        .append(",")
                        .append(registradorRestante)
                        .append("\n");
            }
        } else {

            // todo: tratar soma de dois termos (variável + variável ou variável + inteiro)

            String reg1 = symbolTable.get(elementosDaExpressao[0].trim());
            String reg2 = symbolTable.get(elementosDaExpressao[1].trim());

            validateRegs(reg1, reg2);
            codigoAsm.append(Opcode.SUM.getCode())
                    .append(registradorDaVariavelUsada)
                    .append(",")
                    .append(reg1)
                    .append(",")
                    .append(reg2)
                    .append("\n");
        }
    }

    private boolean verificarSeContemMaisDeDoisTermos(String[] expressa) {
        return expressa.length > 2;
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
            throw new IllegalArgumentException("Inicialização com expressão não suportada ainda.");
        } else {
            int valor = Integer.parseInt(partes[1].trim());
            validateInt(valor);
            return valor;
        }
    }

    private boolean verificarSeContemOperacaoNaAtribuicao(String expressao) {
        return expressao.contains(CompiladorSintaxe.SOMA.getSintaxeEmString()) ||
                expressao.contains(CompiladorSintaxe.SUBTRACAO.getSintaxeEmString()) ||
                expressao.contains(CompiladorSintaxe.MULTIPLICACAO.getSintaxeEmString());
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
