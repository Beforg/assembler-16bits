package com.unipampa.compiladorarquitetura16bits;

import com.unipampa.compiladorarquitetura16bits.memory.AllocationResult;
import com.unipampa.compiladorarquitetura16bits.memory.MemoryManager;
import com.unipampa.compiladorarquitetura16bits.memory.VariableLocationTracker;
import com.unipampa.compiladorarquitetura16bits.model.CompiladorSintaxe;
import com.unipampa.compiladorarquitetura16bits.model.Opcode;
import com.unipampa.compiladorarquitetura16bits.model.Registrador;
import com.unipampa.compiladorarquitetura16bits.model.Variavel;
import com.unipampa.compiladorarquitetura16bits.utils.CondicionalUtils;
import com.unipampa.compiladorarquitetura16bits.utils.IntegerUtils;
import com.unipampa.compiladorarquitetura16bits.utils.LabelGenerator;
import com.unipampa.compiladorarquitetura16bits.utils.LabelsCompilador;
import com.unipampa.compiladorarquitetura16bits.utils.RegisterAllocator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

public class Parser {
    private final Map<Variavel, Registrador> symbolTable;
    private final int MAX_INSTRUCTIONS = 15;
    private final Deque<IfLabels> ifLabelStack = new ArrayDeque<>();

    // Componentes de gerenciamento de memória e registradores
    private final MemoryManager memoryManager;
    private final VariableLocationTracker locationTracker;
    private final RegisterAllocator allocator;

    public Parser(Map<Variavel, Registrador> symbolTable) {
        this.symbolTable = symbolTable;
        this.memoryManager = new MemoryManager();
        this.locationTracker = new VariableLocationTracker();
        this.allocator = new RegisterAllocator(symbolTable, memoryManager, locationTracker);
    }

    private static class IfLabels {
        final String thenLabel;
        final String elseLabel;
        final String endLabel;
        final boolean hasElse;
        final int endLineIndex;

        IfLabels(String thenLabel, String elseLabel, String endLabel) {
            this.thenLabel = thenLabel;
            this.elseLabel = elseLabel;
            this.endLabel = endLabel;
            this.hasElse = (elseLabel != null);
            this.endLineIndex = -1;
        }

        IfLabels(String thenLabel, String endLabel, int endLineIndex) {
            this.thenLabel = thenLabel;
            this.elseLabel = null;
            this.endLabel = endLabel;
            this.hasElse = false;
            this.endLineIndex = endLineIndex;
        }
    }

    private int registradorAtualLivre = 0;

    // Método auxiliar para buscar Registrador por nome de variável
    private Registrador findRegistradorByVarName(String varName) {
        for (Map.Entry<Variavel, Registrador> entry : symbolTable.entrySet()) {
            if (entry.getKey() != null && varName.equals(entry.getKey().getNome())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Busca ou aloca registrador para uma variável
     * Se a variável está na memória, faz reload automático
     *
     * @param varName Nome da variável
     * @param codigoAsm StringBuilder para adicionar instruções de reload
     * @return Registrador com a variável
     */
    private Registrador getOrLoadRegistrador(String varName, StringBuilder codigoAsm) {
        // 1. Verificar se já está em registrador
        Registrador reg = findRegistradorByVarName(varName);
        if (reg != null) {
            return reg;
        }

        // 2. Verificar se está na memória
        VariableLocationTracker.LocationInfo location = locationTracker.locate(varName);
        if (location.location == VariableLocationTracker.Location.MEMORY) {
            // Fazer reload da memória
            AllocationResult result = allocator.allocate(varName, 0);

            // Adicionar instruções de reload (pode incluir STA de spill + LDA de reload)
            result.getInstructions().forEach(codigoAsm::append);

            return result.getRegistrador();
        }

        // 3. Variável não declarada
        return null;
    }

    public String parse(String codigoFonte) {
        StringBuilder codigoAsm = new StringBuilder();
        String[] linhas = codigoFonte.split("\\n");

        for (int i = 0; i < linhas.length; i++) {
            String linha = linhas[i].trim();

            if (linha.equals("}") || linha.equals("};")) {
                if (!ifLabelStack.isEmpty()) {
                    IfLabels top = ifLabelStack.peek();
                    if (!top.hasElse && top.endLineIndex == i) {
                        ifLabelStack.pop();
                        codigoAsm.append(top.endLabel).append(":\n");
                        continue;
                    }
                }
            }

            if (linha.isEmpty() || linha.startsWith("//")) {
                continue;
            }

            if (linha.startsWith(CompiladorSintaxe.INTEIRO.getSintaxeEmString())) {
                // Usar RegisterAllocator com LRU ao invés de alocação manual
                String[] partes = linha.replace(";", "").split("=");
                String nomeVariavel = partes[0].replace(CompiladorSintaxe.INTEIRO.getSintaxeEmString(), "").trim();

                int valorInicial = 0;
                if (partes.length == 2) {
                    valorInicial = Integer.parseInt(partes[1].trim());
                    validateInt(valorInicial);
                }

                // Alocar registrador com LRU automático
                AllocationResult result = allocator.allocate(nomeVariavel, valorInicial);

                // Adicionar instruções de reload se necessário (quando variável estava na memória)
                result.getInstructions().forEach(codigoAsm::append);

                // Gerar instrução LDA
                Registrador reg = result.getRegistrador();
                codigoAsm.append(Opcode.LDA.getCode())
                        .append(reg.getNome())
                        .append(",")
                        .append(valorInicial)
                        .append("\n");

            } else if (linha.startsWith(CompiladorSintaxe.SE.getSintaxeEmString()) && !linha.contains(CompiladorSintaxe.SENAO.getSintaxeEmString())) {
                processarInstrucaoSe(linha, linhas, i, codigoAsm);
            } else if (linha.trim().contains(CompiladorSintaxe.SENAO.getSintaxeEmString())) {
                if (ifLabelStack.isEmpty()) {
                    throw new IllegalStateException("Encontrado 'senao' sem 'se' correspondente.");
                }
                IfLabels labels = ifLabelStack.pop();
                // salta do bloco then para o fim usando a mesma label
                codigoAsm.append(Opcode.JMP.getCode()).append(labels.endLabel).append("\n");
                codigoAsm.append(labels.elseLabel).append(":\n");
                i = montarInstrucaoSenao(i, linhas, codigoAsm, labels.endLabel);
                codigoAsm.append(labels.endLabel).append(":\n");

            } else if (linha.contains(CompiladorSintaxe.ENQUANTO.getSintaxeEmString())) {
                String verificarSeLike = linha.replaceFirst("enquanto", "se");
                CondicionalUtils.verificarEstruturaDaCondicao(verificarSeLike);

                String operandoEsquerdo = CondicionalUtils.extrairOperandoEsquerdo(verificarSeLike);
                String operandoDireito = CondicionalUtils.extrairOperandoDireito(verificarSeLike);
                String operadorCondicionalOpcode = CondicionalUtils.extrairOperadorCondicional(verificarSeLike);
                String labelInicio = LabelGenerator.gerarLabel(LabelsCompilador.ENQUANTO_INICIO);
                String labelFim = LabelGenerator.gerarLabel(LabelsCompilador.FIM_ENQUANTO);

                // label de verificação
                codigoAsm.append(labelInicio).append(":\n");

                // inverter BEQ <-> BNE para saltar ao fim quando a condição for falsa
                String opcodeParaSaltarAoFim;
                if (Opcode.BEQ.getCode().equals(operadorCondicionalOpcode)) {
                    opcodeParaSaltarAoFim = Opcode.BNE.getCode();
                } else {
                    opcodeParaSaltarAoFim = Opcode.BEQ.getCode();
                }

                operandoEsquerdo = montarOperador(operandoEsquerdo, codigoAsm, registradorAtualLivre);
                operandoDireito = montarOperador(operandoDireito, codigoAsm, registradorAtualLivre);

                // se condição falsa -> salta para labelFim
                montarInstrucaoSe(codigoAsm, operandoEsquerdo, operandoDireito, opcodeParaSaltarAoFim, labelFim);

                // processa corpo do enquanto (linhas entre i+1 e fechamento)
                int endLineIndex = buscarFechamentoDeChaves(linhas, i);
                int j = i + 1;
                while (j < endLineIndex) {
                    String atual = linhas[j].trim();
                    if (atual.isEmpty() || atual.startsWith("//")) { j++; continue; }
                    processLinhaBasica(atual, codigoAsm);
                    j++;
                }

                // volta para verificação
                montarInstrucaoJump(codigoAsm, labelInicio);
                codigoAsm.append(labelFim).append(":\n");

                // pular para a linha depois do fechamento do bloco
                i = endLineIndex;

            } else if (linha.contains(CompiladorSintaxe.PARA.getSintaxeEmString())) {
                throw new UnsupportedOperationException("Estrutura 'para' não implementada ainda.");

            } else if (linha.contains(CompiladorSintaxe.ATRIBUICAO.getSintaxeEmString()) ) {

                String[] partes = linha.replace(";", "").split("=");
                String variavelUsada = partes[0].trim();
                String expressao = partes[1].trim();

                // Usar getOrLoadRegistrador para suportar variáveis na memória
                Registrador registradorDaVariavelUsada = getOrLoadRegistrador(variavelUsada, codigoAsm);

                if (registradorDaVariavelUsada == null) {
                    throw new IllegalArgumentException("Variável " + variavelUsada + " não declarada.");
                }

                String nomeRegistradorUsado = registradorDaVariavelUsada.getNome();

                if (expressao.contains(CompiladorSintaxe.SOMA.getSintaxeEmString())) {
                    montarInstrucaoSoma(expressao, codigoAsm, nomeRegistradorUsado, registradorAtualLivre);
                }

                else if (expressao.contains(CompiladorSintaxe.SUBTRACAO.getSintaxeEmString())) {
                    montarInstrucaoSubtracao(expressao, "-", codigoAsm, Opcode.SUB, nomeRegistradorUsado);
                }

                else if (expressao.contains(CompiladorSintaxe.MULTIPLICACAO.getSintaxeEmString())) {
                    montarInstrucaoMultiplicacao(expressao, "\\*", codigoAsm, Opcode.MUL, nomeRegistradorUsado);
                }

            } else {
                throw new IllegalArgumentException("Linha não reconhecida: " + linha);
            }


        }
        return codigoAsm.toString();
    }

    private void processarInstrucaoSe(String linha, String[] linhas, int i, StringBuilder codigoAsm) {
        String operandoEsquerdo = CondicionalUtils.extrairOperandoEsquerdo(linha);
        String operandoDireito = CondicionalUtils.extrairOperandoDireito(linha);
        String operadorCondicionalOpcode = CondicionalUtils.extrairOperadorCondicional(linha);

        if (sePosuiSenao(linhas, linha, i)) {
            String labelElse = LabelGenerator.gerarLabel(LabelsCompilador.SENAO_INICIO);
            String labelThen = LabelGenerator.gerarLabel(LabelsCompilador.SE_INICIO);
            String labelEnd = LabelGenerator.gerarLabel(LabelsCompilador.FIM_SE);
            ifLabelStack.push(new IfLabels(labelThen, labelElse, labelEnd));

            operandoEsquerdo = montarOperador(operandoEsquerdo, codigoAsm, registradorAtualLivre);
            operandoDireito = montarOperador(operandoDireito, codigoAsm, registradorAtualLivre);

            montarInstrucaoSe(codigoAsm, operandoEsquerdo, operandoDireito, operadorCondicionalOpcode, labelThen);
            montarInstrucaoJump(codigoAsm, labelElse);
            codigoAsm.append(labelThen).append(":\n");

        } else {
            String labelThen = LabelGenerator.gerarLabel(LabelsCompilador.SE_INICIO);
            String labelEnd = LabelGenerator.gerarLabel(LabelsCompilador.FIM_SE);

            // find the closing brace line index for this then-block so we can emit the end label later
            int endLineIndex = buscarFechamentoDeChaves(linhas, i);

            // push a marker so when we reach the closing brace we know to write the end label
            ifLabelStack.push(new IfLabels(labelThen, labelEnd, endLineIndex));

            operandoEsquerdo = montarOperador(operandoEsquerdo, codigoAsm, registradorAtualLivre);
            operandoDireito = montarOperador(operandoDireito, codigoAsm, registradorAtualLivre);

            montarInstrucaoSe(codigoAsm, operandoEsquerdo, operandoDireito, operadorCondicionalOpcode, labelThen);
            montarInstrucaoJump(codigoAsm, labelEnd);
            codigoAsm.append(labelThen).append(":\n");
        }
    }

    private int buscarFechamentoDeChaves(String[] linhas, int startIndex) {
        int depth = 0;
        boolean foundOpening = false;
        for (int j = startIndex; j < linhas.length; j++) {
            String l = linhas[j];
            // count opens and closes on the line
            int opens = countChar(l, '{');
            int closes = countChar(l, '}');
            if (opens > 0) foundOpening = true;
            depth += opens - closes;
            if (foundOpening && depth == 0) {
                return j; // line index of the closing brace
            }
        }
        // if not found, return startIndex (fallback)
        return startIndex;
    }

    private int countChar(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) count++;
        }
        return count;
    }

    private String montarOperador(String operador, StringBuilder codigoAsm, int registradorAtualLivre) {
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
            // Usar getOrLoadRegistrador para suportar variáveis na memória
            Registrador registradorDaVariavel = getOrLoadRegistrador(operador, codigoAsm);
            if (registradorDaVariavel == null) {
                throw new IllegalArgumentException("Variável " + operador + " não declarada.");
            }
            return registradorDaVariavel.getNome();
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
            // Usar RegisterAllocator com LRU
            String[] partes = linha.replace(";", "").split("=");
            String nomeVariavel = partes[0].replace(CompiladorSintaxe.INTEIRO.getSintaxeEmString(), "").trim();

            int valorInicial = 0;
            if (partes.length == 2) {
                valorInicial = Integer.parseInt(partes[1].trim());
                validateInt(valorInicial);
            }

            // Alocar com LRU automático
            AllocationResult result = allocator.allocate(nomeVariavel, valorInicial);
            result.getInstructions().forEach(codigoAsm::append);

            Registrador reg = result.getRegistrador();
            codigoAsm.append(Opcode.LDA.getCode())
                    .append(reg.getNome())
                    .append(",")
                    .append(valorInicial)
                    .append("\n");

        } else if (linha.contains(CompiladorSintaxe.ATRIBUICAO.getSintaxeEmString())) {
            String[] partes = linha.replace(";", "").split("=");
            String variavelUsada = partes[0].trim();
            String expressao = partes[1].trim();

            // Usar getOrLoadRegistrador para suportar variáveis na memória
            Registrador registradorDaVariavelUsada = getOrLoadRegistrador(variavelUsada, codigoAsm);

            if (registradorDaVariavelUsada == null) {
                throw new IllegalArgumentException("Variável " + variavelUsada + " não declarada.");
            }
            String nomeRegistradorUsado = registradorDaVariavelUsada.getNome();
            if (expressao.contains(CompiladorSintaxe.SOMA.getSintaxeEmString())) {
                montarInstrucaoSoma(expressao, codigoAsm, nomeRegistradorUsado, registradorAtualLivre);
            } else if (expressao.contains(CompiladorSintaxe.SUBTRACAO.getSintaxeEmString())) {
                montarInstrucaoSubtracao(expressao, "-", codigoAsm, Opcode.SUB, nomeRegistradorUsado);
            } else if (expressao.contains(CompiladorSintaxe.MULTIPLICACAO.getSintaxeEmString())) {
                montarInstrucaoMultiplicacao(expressao, "\\*", codigoAsm, Opcode.MUL, nomeRegistradorUsado);
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

        // Usar getOrLoadRegistrador para suportar variáveis na memória
        Registrador reg1Obj = getOrLoadRegistrador(ops[0].trim(), codigoAsm);
        Registrador reg2Obj = getOrLoadRegistrador(ops[1].trim(), codigoAsm);

        if (reg1Obj == null || reg2Obj == null) {
            throw new IllegalArgumentException("Variável não declarada na expressão.");
        }

        String reg1 = reg1Obj.getNome();
        String reg2 = reg2Obj.getNome();

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

        // Usar getOrLoadRegistrador para suportar variáveis na memória
        Registrador reg1Obj = getOrLoadRegistrador(ops[0].trim(), codigoAsm);
        Registrador reg2Obj = getOrLoadRegistrador(ops[1].trim(), codigoAsm);

        if (reg1Obj == null || reg2Obj == null) {
            throw new IllegalArgumentException("Variável não declarada na expressão.");
        }

        String reg1 = reg1Obj.getNome();
        String reg2 = reg2Obj.getNome();

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
                        // Usar getOrLoadRegistrador para suportar variáveis na memória
                        Registrador reg1Obj = getOrLoadRegistrador(valorOuVariavelDaExpressao.trim(), codigoAsm);
                        if (reg1Obj == null) {
                            throw new IllegalArgumentException("Variável " + valorOuVariavelDaExpressao.trim() + " não declarada.");
                        }
                        registradorOperando1 = reg1Obj.getNome();
                        nomeDaVariavel = valorOuVariavelDaExpressao.trim();
                        contagemParaMontarRegs++; // incrementa após setar o primeiro operando
                        continue;
                    }

                    if (contagemParaMontarRegs == 1 && registradorOperando1 != null) {
                        // Usar getOrLoadRegistrador para suportar variáveis na memória
                        Registrador reg2Obj = getOrLoadRegistrador(valorOuVariavelDaExpressao.trim(), codigoAsm);
                        if (reg2Obj == null) {
                            throw new IllegalArgumentException("Variável " + valorOuVariavelDaExpressao.trim() + " não declarada.");
                        }
                        registradorOperando2 = reg2Obj.getNome();
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
                // Usar getOrLoadRegistrador para suportar variáveis na memória
                Registrador regRestanteObj = getOrLoadRegistrador(nomeDaVariavel, codigoAsm);
                if (regRestanteObj == null) {
                    throw new IllegalArgumentException("Variável " + nomeDaVariavel + " não declarada.");
                }
                String registradorRestante = regRestanteObj.getNome();
                codigoAsm.append(Opcode.SUM.getCode())
                        .append(registradorDaVariavelUsada)
                        .append(",")
                        .append(registradorDaVariavelUsada)
                        .append(",")
                        .append(registradorRestante)
                        .append("\n");
            }
        } else {
            // Tratar soma de dois termos (variável + variável ou variável + inteiro)

            String termo1 = elementosDaExpressao[0].trim();
            String termo2 = elementosDaExpressao[1].trim();

            String reg1Nome;
            String reg2Nome;

            // Processar primeiro termo
            if (IntegerUtils.verificaSeEhInteiro(termo1)) {
                // É um inteiro - carregar em registrador temporário
                int valor1 = Integer.parseInt(termo1);
                validateInt(valor1);
                reg1Nome = "R" + registradorAtualLivre;
                codigoAsm.append(Opcode.LDA.getCode())
                        .append(reg1Nome)
                        .append(",")
                        .append(valor1)
                        .append("\n");
            } else {
                // É uma variável - buscar registrador ou carregar da memória
                Registrador reg1 = getOrLoadRegistrador(termo1, codigoAsm);
                if (reg1 == null) {
                    throw new IllegalArgumentException("Variável '" + termo1 + "' não declarada na expressão.");
                }
                reg1Nome = reg1.getNome();
            }

            // Processar segundo termo
            if (IntegerUtils.verificaSeEhInteiro(termo2)) {
                // É um inteiro - carregar em registrador temporário
                int valor2 = Integer.parseInt(termo2);
                validateInt(valor2);
                reg2Nome = "R" + registradorAtualLivre;
                codigoAsm.append(Opcode.LDA.getCode())
                        .append(reg2Nome)
                        .append(",")
                        .append(valor2)
                        .append("\n");
            } else {
                // É uma variável - buscar registrador ou carregar da memória
                Registrador reg2 = getOrLoadRegistrador(termo2, codigoAsm);
                if (reg2 == null) {
                    throw new IllegalArgumentException("Variável '" + termo2 + "' não declarada na expressão.");
                }
                reg2Nome = reg2.getNome();
            }

            // Gerar instrução SUM
            codigoAsm.append(Opcode.SUM.getCode())
                    .append(registradorDaVariavelUsada)
                    .append(",")
                    .append(reg1Nome)
                    .append(",")
                    .append(reg2Nome)
                    .append("\n");
        }
    }

    private boolean verificarSeContemMaisDeDoisTermos(String[] expressa) {
        return expressa.length > 2;
    }

    private void montarInstrucaoLoadInteiro(
            String linha, Map<Variavel, Registrador> symbolTable,
            int regAtual,
            StringBuilder codigoAsm) {

        int valor;
        String nomeDaNovaVariavel;
        String registradorDaVariavel;

        String[] partes = linha.replace(";", "").split("=");
        if (variavelEstaInicializada(partes)) {
            nomeDaNovaVariavel = extrairNomeVariavel(partes);
            valor = extrairValorInteiro(partes);

            // MUDAR

            registradorDaVariavel = montarRegistradorParaVariavel(regAtual);
            Registrador registrador = new Registrador(registradorDaVariavel, valor);
            Variavel variavelNova = new Variavel(nomeDaNovaVariavel, registrador);

            symbolTable.put(variavelNova, registrador);
            codigoAsm.append(Opcode.LDA.getCode()).append(registradorDaVariavel).append(",").append(valor).append("\n");
        } else {

            nomeDaNovaVariavel = extrairNomeVariavel(partes);
            registradorDaVariavel = montarRegistradorParaVariavel(regAtual);
            Registrador registrador = new Registrador(registradorDaVariavel, 0);
            Variavel variavelNova = new Variavel(nomeDaNovaVariavel, registrador);

            symbolTable.put(variavelNova, registrador);
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


    private void validateInt(int value) {
        if (value < 0 || value > 15) {
            throw new IllegalArgumentException("Valor inteiro fora do intervalo permitido (0-15).");
        }
    }

    /**
     * Reseta o estado do parser para uma nova compilação
     */
    public void reset() {
        registradorAtualLivre = 0;
        ifLabelStack.clear();
        LabelGenerator.reset();
        allocator.reset();  // Reseta RegisterAllocator (que reseta MemoryManager e LocationTracker)
    }

    /**
     * Retorna o MemoryManager para acesso pela UI
     */
    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    /**
     * Retorna o RegisterAllocator para acesso pela UI
     */
    public RegisterAllocator getAllocator() {
        return allocator;
    }
}
