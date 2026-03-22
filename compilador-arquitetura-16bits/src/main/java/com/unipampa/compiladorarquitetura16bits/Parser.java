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
import com.unipampa.compiladorarquitetura16bits.utils.exceptions.CompilationException;

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
        // 1. Verificar se já está em registrador - usar allocator.findByVarName que atualiza LRU
        Registrador reg = allocator.findByVarName(varName);
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


            if (linha.isEmpty() || linha.startsWith("//")) {
                continue;
            }

            if (linha.contains("}") && linha.contains(CompiladorSintaxe.SENAO.getSintaxeEmString())) {

                if (!ifLabelStack.isEmpty()) {
                    IfLabels top = ifLabelStack.peek();


                    if (top.hasElse) {
                        // Adicionar JMP para fim e label SENAO_INICIO
                        codigoAsm.append(Opcode.JMP.getCode()).append(top.endLabel).append("\n");
                        codigoAsm.append(top.elseLabel).append(":\n");



                        // Processar corpo do SENAO
                        i = montarInstrucaoSenao(i, linhas, codigoAsm, top.endLabel);

                        // Adicionar label de fim
                        codigoAsm.append(top.endLabel).append(":\n");

                        ifLabelStack.pop();
                        continue;
                    }
                }
            }

            // Caso 2: Apenas "}" (fechamento de bloco)
            if (linha.equals("}") || linha.equals("};")) {

                if (!ifLabelStack.isEmpty()) {
                    IfLabels top = ifLabelStack.peek();

                    // Verificar se a próxima linha contém SENAO
                    boolean proximaLinhaESenao = false;
                    if (i + 1 < linhas.length) {
                        String proximaLinha = linhas[i + 1].trim();

                        if (proximaLinha.startsWith(CompiladorSintaxe.SENAO.getSintaxeEmString())) {
                            proximaLinhaESenao = true;
                        }

                    }


                    // SE com SENAO (senao na próxima linha)
                    if (top.hasElse && proximaLinhaESenao) {

                        // Adicionar JMP para fim e label SENAO_INICIO
                        codigoAsm.append(Opcode.JMP.getCode()).append(top.endLabel).append("\n");
                        codigoAsm.append(top.elseLabel).append(":\n");


                        // Pular para a próxima linha (que é o SENAO)
                        i++;

                        // Processar corpo do SENAO
                        i = montarInstrucaoSenao(i, linhas, codigoAsm, top.endLabel);

                        // Adicionar label de fim
                        codigoAsm.append(top.endLabel).append(":\n");

                        // Remover da stack
                        ifLabelStack.pop();
                        continue;
                    }

                    // SE sem SENAO
                    if (!top.hasElse && top.endLineIndex == i) {
                        ifLabelStack.pop();
                        codigoAsm.append(top.endLabel).append(":\n");
                    }
                }
                continue; // SEMPRE pular chaves
            }

            // Caso 3: Abertura de bloco "{"
            if (linha.equals("{")) {
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

            } else if (linha.contains(CompiladorSintaxe.ENQUANTO.getSintaxeEmString())) {
                String verificarSeLike = linha.replaceFirst("enquanto", "se");
                CondicionalUtils.verificarEstruturaDaCondicao(verificarSeLike);

                String operandoEsquerdo = CondicionalUtils.extrairOperandoEsquerdo(verificarSeLike);
                String operandoDireito = CondicionalUtils.extrairOperandoDireito(verificarSeLike);

                operandoEsquerdo = montarOperador(operandoEsquerdo, codigoAsm, registradorAtualLivre);
                operandoDireito = montarOperador(operandoDireito, codigoAsm, registradorAtualLivre);

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


                montarInstrucaoJump(codigoAsm, labelInicio);
                codigoAsm.append(labelFim).append(":\n");


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
            int endLineIndex = buscarFechamentoDeChaves(linhas, i);
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
                return j;
            }
        }

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

            // Alocar registrador temporário para a constante usando LRU
            String nomeTemporario = "_const_" + valorDoInteiro + "_" + System.nanoTime();
            AllocationResult result = allocator.allocate(nomeTemporario, valorDoInteiro);

            // Adicionar instruções de spill se necessário
            result.getInstructions().forEach(codigoAsm::append);

            registradorFonte = result.getRegistrador().getNome();
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
                throw new CompilationException(
                    "Variável '" + variavelUsada + "' não foi declarada.",
                    CompilationException.ErrorType.SEMANTIC_ERROR
                );
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

//        System.out.println("\n=== DEBUG sePosuiSenao() ===");
//        System.out.println("Verificando SE na linha " + i + ": " + linha);

        while (j < linhas.length) {
            String proximaLinha = linhas[j].trim();

            if (proximaLinha.isEmpty() || proximaLinha.startsWith("//")) {
                j++;
                continue;
            }

            // Atualizar profundidade
            if (proximaLinha.contains("{") && !proximaLinha.contains(CompiladorSintaxe.SENAO.getSintaxeEmString())) {
                profundidadeDoBloco++;
            }
            if (proximaLinha.contains("}")) {
                profundidadeDoBloco = Math.max(0, profundidadeDoBloco - 1);
            }

//            System.out.println("  Linha " + j + " (prof=" + profundidadeDoBloco + "): " + proximaLinha);

            // Se encontrou SENAO na profundidade 0, este SE tem SENAO
            if (profundidadeDoBloco == 0 && proximaLinha.contains(CompiladorSintaxe.SENAO.getSintaxeEmString())) {
                possuiSenao = true;
//                System.out.println("  -> Encontrou SENAO! Retorna true");
                break;
            }

            // Se encontrou outro SE na profundidade 0, para a busca (este SE não tem SENAO)
            if (profundidadeDoBloco == 0 && proximaLinha.startsWith(CompiladorSintaxe.SE.getSintaxeEmString())) {
//                System.out.println("  -> Encontrou outro SE na mesma profundidade! Retorna false");
                break;
            }

            // Se voltou para profundidade 0 e encontrou }, o bloco SE terminou sem SENAO
            if (profundidadeDoBloco == 0 && proximaLinha.equals("}")) {
//                System.out.println("  -> Encontrou } na profundidade 0 (fim do SE)! Retorna false");
                break;
            }

            j++;
        }

//        System.out.println("Resultado: " + possuiSenao);
        return possuiSenao;
    }



    private void montarInstrucaoMultiplicacao(String expressao, String regex, StringBuilder codigoAsm, Opcode mul, String registradorDaVariavelUsada) {
        String[] ops = expressao.split(regex);

        // Usar getOrLoadRegistrador para suportar variáveis na memória
        Registrador reg1Obj = getOrLoadRegistrador(ops[0].trim(), codigoAsm);
        Registrador reg2Obj = getOrLoadRegistrador(ops[1].trim(), codigoAsm);

        if (reg1Obj == null || reg2Obj == null) {
            throw new CompilationException(
                "Variável não declarada na expressão de multiplicação.",
                CompilationException.ErrorType.SEMANTIC_ERROR
            );
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
        String[] elementosDaExpressao = expressao.split(regex);

        if (verificarSeContemMaisDeDoisTermos(elementosDaExpressao)) {
            // Para subtração, a ordem importa (não é comutativa).
            // Processamos estritamente da esquerda para a direita: (A - B) - C
            String regOperandoEsq = null;

            for (int i = 0; i < elementosDaExpressao.length; i++) {
                String termo = elementosDaExpressao[i].trim();
                String regTermoAtual;

                // Processa o termo atual (Inteiro ou Variável)
                if (IntegerUtils.verificaSeEhInteiro(termo)) {
                    int valor = Integer.parseInt(termo);
                    validateInt(valor); // Validação de limite aproveitada da sua lógica

                    String nomeTemporario = "_const_sub_" + valor + "_" + System.nanoTime();
                    AllocationResult result = allocator.allocate(nomeTemporario, valor);
                    result.getInstructions().forEach(codigoAsm::append);

                    regTermoAtual = result.getRegistrador().getNome();
                    codigoAsm.append(Opcode.LDA.getCode())
                            .append(regTermoAtual)
                            .append(",")
                            .append(valor)
                            .append("\n");
                } else {
                    Registrador regObj = getOrLoadRegistrador(termo, codigoAsm);
                    if (regObj == null) {
                        throw new CompilationException(
                                "Variável '" + termo + "' não foi declarada.",
                                CompilationException.ErrorType.SEMANTIC_ERROR
                        );
                    }
                    regTermoAtual = regObj.getNome();
                }

                if (i == 0) {
                    // Sendo o primeiro termo (minuendo), apenas guardamos o registrador
                    regOperandoEsq = regTermoAtual;
                } else {
                    // Sendo o segundo termo ou além, executamos a subtração: DESTINO = ESQUERDA - DIREITA
                    codigoAsm.append(sub.getCode())
                            .append(registradorDaVariavelUsada)
                            .append(",")
                            .append(regOperandoEsq)
                            .append(",")
                            .append(regTermoAtual)
                            .append("\n");

                    // O resultado agora está no registrador de destino.
                    // Ele vira o operando esquerdo para a próxima subtração do loop.
                    regOperandoEsq = registradorDaVariavelUsada;
                }
            }

        } else {
            // Tratar subtração de dois termos (variável - variável, variável - inteiro, etc)
            String termo1 = elementosDaExpressao[0].trim();
            String termo2 = elementosDaExpressao[1].trim();

            String reg1Nome;
            String reg2Nome;

            // Processar primeiro termo (Minuendo)
            if (IntegerUtils.verificaSeEhInteiro(termo1)) {
                int valor1 = Integer.parseInt(termo1);
                validateInt(valor1);

                String nomeTemporario = "_const_" + valor1 + "_" + System.nanoTime();
                AllocationResult result = allocator.allocate(nomeTemporario, valor1);
                result.getInstructions().forEach(codigoAsm::append);

                reg1Nome = result.getRegistrador().getNome();
                codigoAsm.append(Opcode.LDA.getCode())
                        .append(reg1Nome)
                        .append(",")
                        .append(valor1)
                        .append("\n");
            } else {
                Registrador reg1 = getOrLoadRegistrador(termo1, codigoAsm);
                if (reg1 == null) {
                    throw new CompilationException(
                            "Variável '" + termo1 + "' não foi declarada na expressão.",
                            CompilationException.ErrorType.SEMANTIC_ERROR
                    );
                }
                reg1Nome = reg1.getNome();
            }

            // Processar segundo termo (Subtraendo)
            if (IntegerUtils.verificaSeEhInteiro(termo2)) {
                int valor2 = Integer.parseInt(termo2);
                validateInt(valor2);

                String nomeTemporario = "_const_" + valor2 + "_" + System.nanoTime();
                AllocationResult result = allocator.allocate(nomeTemporario, valor2);
                result.getInstructions().forEach(codigoAsm::append);

                reg2Nome = result.getRegistrador().getNome();
                codigoAsm.append(Opcode.LDA.getCode())
                        .append(reg2Nome)
                        .append(",")
                        .append(valor2)
                        .append("\n");
            } else {
                Registrador reg2 = getOrLoadRegistrador(termo2, codigoAsm);
                if (reg2 == null) {
                    throw new CompilationException(
                            "Variável '" + termo2 + "' não foi declarada na expressão.",
                            CompilationException.ErrorType.SEMANTIC_ERROR
                    );
                }
                reg2Nome = reg2.getNome();
            }

            // Gerar instrução da Subtração (SUB)
            codigoAsm.append(sub.getCode())
                    .append(registradorDaVariavelUsada)
                    .append(",")
                    .append(reg1Nome)
                    .append(",")
                    .append(reg2Nome)
                    .append("\n");
        }
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
                        throw new CompilationException(
                            "Soma das constantes (" + somaDasConstantes + ") excede o valor máximo permitido (15).",
                            CompilationException.ErrorType.VALUE_OUT_OF_RANGE
                        );
                    }

                } else {
                    if (contagemParaMontarRegs == 0) {
                        // Usar getOrLoadRegistrador para suportar variáveis na memória
                        Registrador reg1Obj = getOrLoadRegistrador(valorOuVariavelDaExpressao.trim(), codigoAsm);
                        if (reg1Obj == null) {
                            throw new CompilationException(
                                "Variável '" + valorOuVariavelDaExpressao.trim() + "' não foi declarada.",
                                CompilationException.ErrorType.SEMANTIC_ERROR
                            );
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
                            throw new CompilationException(
                                "Variável '" + valorOuVariavelDaExpressao.trim() + "' não foi declarada.",
                                CompilationException.ErrorType.SEMANTIC_ERROR
                            );
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
                // Alocar registrador temporário para a soma das constantes usando LRU
                String nomeTemporario = "_const_sum_" + somaDasConstantes + "_" + System.nanoTime();
                AllocationResult result = allocator.allocate(nomeTemporario, somaDasConstantes);
                result.getInstructions().forEach(codigoAsm::append);

                registradorParaImediato = result.getRegistrador().getNome();
                codigoAsm.append(Opcode.LDA.getCode())
                        .append(registradorParaImediato)
                        .append(",")
                        .append(somaDasConstantes)
                        .append("\n");

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
                    throw new CompilationException(
                        "Variável '" + nomeDaVariavel + "' não foi declarada.",
                        CompilationException.ErrorType.SEMANTIC_ERROR
                    );
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
                // É um inteiro - carregar em registrador temporário usando LRU
                int valor1 = Integer.parseInt(termo1);
                validateInt(valor1);

                String nomeTemporario = "_const_" + valor1 + "_" + System.nanoTime();
                AllocationResult result = allocator.allocate(nomeTemporario, valor1);
                result.getInstructions().forEach(codigoAsm::append);

                reg1Nome = result.getRegistrador().getNome();
                codigoAsm.append(Opcode.LDA.getCode())
                        .append(reg1Nome)
                        .append(",")
                        .append(valor1)
                        .append("\n");
            } else {
                // É uma variável - buscar registrador ou carregar da memória
                Registrador reg1 = getOrLoadRegistrador(termo1, codigoAsm);
                if (reg1 == null) {
                    throw new CompilationException(
                        "Variável '" + termo1 + "' não foi declarada na expressão.",
                        CompilationException.ErrorType.SEMANTIC_ERROR
                    );
                }
                reg1Nome = reg1.getNome();
            }

            // Processar segundo termo
            if (IntegerUtils.verificaSeEhInteiro(termo2)) {
                // É um inteiro - carregar em registrador temporário usando LRU
                int valor2 = Integer.parseInt(termo2);
                validateInt(valor2);

                String nomeTemporario = "_const_" + valor2 + "_" + System.nanoTime();
                AllocationResult result = allocator.allocate(nomeTemporario, valor2);
                result.getInstructions().forEach(codigoAsm::append);

                reg2Nome = result.getRegistrador().getNome();
                codigoAsm.append(Opcode.LDA.getCode())
                        .append(reg2Nome)
                        .append(",")
                        .append(valor2)
                        .append("\n");
            } else {
                // É uma variável - buscar registrador ou carregar da memória
                Registrador reg2 = getOrLoadRegistrador(termo2, codigoAsm);
                if (reg2 == null) {
                    throw new CompilationException(
                        "Variável '" + termo2 + "' não foi declarada na expressão.",
                        CompilationException.ErrorType.SEMANTIC_ERROR
                    );
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

        String[] partes = linha.replace(";", "").split("=");
        String nomeDaNovaVariavel = partes[0].replace("inteiro", "").trim();

        if (variavelEstaInicializada(partes)) {
            int valor = extrairValorInteiro(partes);

            // Alocar usando RegisterAllocator para respeitar LRU/spill
            AllocationResult result = allocator.allocate(nomeDaNovaVariavel, valor);
            result.getInstructions().forEach(codigoAsm::append);

            Registrador reg = result.getRegistrador();
            codigoAsm.append(Opcode.LDA.getCode())
                    .append(reg.getNome())
                    .append(",")
                    .append(valor)
                    .append("\n");

        } else {
            // Declaração sem inicialização: alocar com valor 0
            AllocationResult result = allocator.allocate(nomeDaNovaVariavel, 0);
            result.getInstructions().forEach(codigoAsm::append);

            Registrador reg = result.getRegistrador();
            codigoAsm.append(Opcode.LDA.getCode())
                    .append(reg.getNome())
                    .append(",0\n");
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
            throw new CompilationException(
                "Inicialização com expressão não suportada ainda.",
                CompilationException.ErrorType.UNSUPPORTED_OPERATION
            );
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
