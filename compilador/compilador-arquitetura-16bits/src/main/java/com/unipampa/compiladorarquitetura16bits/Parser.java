package com.unipampa.compiladorarquitetura16bits;

import com.unipampa.compiladorarquitetura16bits.model.Opcode;

import java.util.HashMap;
import java.util.Map;

public class Parser {
    private Map<String, String> symbolTable = new HashMap<>();
    private final int MAX_INSTRUCTIONS = 15;

    public String parse(String codigoFonte) {
        StringBuilder codigoAsm = new StringBuilder();
        String[] linhas = codigoFonte.split("\\n");
        int instructionCount = 0;
        int regAtual = 0;

        for (String linha : linhas) {
            linha = linha.trim();

            if (linha.isEmpty() || linha.startsWith("//")) {
                continue;
            }

            if (linha.startsWith("inteiro")) {
                String[] partes = linha.replace(";", "").split("=");
                String nomeVar = partes[0].replace("inteiro", "").trim();
                int valor = Integer.parseInt(partes[1].trim());
                validateInt(valor);
                String reg = "R" + regAtual;
                symbolTable.put(nomeVar, reg);
                regAtual++;
                if (valor > 15) { // todo: separar validações
                    throw new IllegalArgumentException("Valor da variável '" + nomeVar + "' excede o limite da arquitetura.");
                }
                codigoAsm.append(Opcode.LDA.getCode()).append(reg).append(",").append(valor).append("\n");
            } else if (linha.contains("=")) {

                String[] partes = linha.replace(";", "").split("=");
                String var = partes[0].trim();
                String expressao = partes[1].trim();
                String regVar = symbolTable.get(var);

                if (expressao.contains("+")) {
                    String[] ops = expressao.split("\\+");
                    String reg1 = symbolTable.get(ops[0].trim());
                    String reg2 = symbolTable.get(ops[1].trim());

                    validateRegs(reg1, reg2);
                    codigoAsm.append(Opcode.SUM.getCode())
                            .append(regVar)
                            .append(",")
                            .append(reg1)
                            .append(",")
                            .append(reg2)
                            .append("\n");
                }

                else if (expressao.contains("-")) {
                    String[] ops = expressao.split("-");
                    String reg1 = symbolTable.get(ops[0].trim());
                    String reg2 = symbolTable.get(ops[1].trim());

                    validateRegs(reg1, reg2);
                    codigoAsm.append(Opcode.SUB.getCode())
                            .append(regVar)
                            .append(",")
                            .append(reg1)
                            .append(",")
                            .append(reg2)
                            .append("\n");
                }

                else if (expressao.contains("*")) {
                    String[] ops = expressao.split("\\*");
                    String reg1 = symbolTable.get(ops[0].trim());
                    String reg2 = symbolTable.get(ops[1].trim());

                    validateRegs(reg1, reg2);
                    codigoAsm.append(Opcode.MUL.getCode())
                            .append(regVar)
                            .append(",")
                            .append(reg1)
                            .append(",")
                            .append(reg2)
                            .append("\n");
                }

            } else if (linha.startsWith("se")) {

            }


        }
        return codigoAsm.toString();
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
