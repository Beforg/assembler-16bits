package com.unipampa.compiladorarquitetura16bits.utils;

import com.unipampa.compiladorarquitetura16bits.memory.AllocationResult;
import com.unipampa.compiladorarquitetura16bits.memory.MemoryManager;
import com.unipampa.compiladorarquitetura16bits.memory.VariableLocationTracker;
import com.unipampa.compiladorarquitetura16bits.memory.VariableLocationTracker.Location;
import com.unipampa.compiladorarquitetura16bits.memory.VariableLocationTracker.LocationInfo;
import com.unipampa.compiladorarquitetura16bits.model.Registrador;
import com.unipampa.compiladorarquitetura16bits.model.Variavel;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Gerenciador de alocação de registradores com política de substituição LRU (Least Recently Used)
 * Limita o uso a 8 registradores (R0 a R7) conforme a arquitetura
 * Integra com MemoryManager para spill automático
 */
public class RegisterAllocator {
    private static final int MAX_REGISTERS = 8;

    // Mapa de variável -> registrador
    private final Map<Variavel, Registrador> symbolTable;

    // Rastreamento de uso dos registradores (para política LRU)
    private final Map<Registrador, LocalDateTime> lastUsedTime;

    // Registradores disponíveis
    private final Queue<Integer> availableRegisters;

    // Registradores em uso
    private final Set<Integer> usedRegisters;

    // Instruções de spill (STA) para salvar registradores na memória
    private final List<String> spillInstructions;

    // Gerenciador de memória (integrado)
    private final MemoryManager memoryManager;

    // Rastreador de localização de variáveis
    private final VariableLocationTracker locationTracker;

    public RegisterAllocator(Map<Variavel, Registrador> symbolTable,
                            MemoryManager memoryManager,
                            VariableLocationTracker locationTracker) {
        this.symbolTable = symbolTable;
        this.memoryManager = memoryManager;
        this.locationTracker = locationTracker;
        this.lastUsedTime = new HashMap<>();
        this.availableRegisters = new LinkedList<>();
        this.usedRegisters = new HashSet<>();
        this.spillInstructions = new ArrayList<>();

        // Inicializar registradores disponíveis (R0 a R7)
        for (int i = 0; i < MAX_REGISTERS; i++) {
            availableRegisters.offer(i);
        }
    }

    /**
     * Aloca um registrador para uma variável com suporte a spill/reload
     * Retorna AllocationResult contendo registrador + instruções geradas
     *
     * Fluxo:
     * 1. Verifica se já está em registrador → retorna direto
     * 2. Verifica se está na memória → gera LDA para reload
     * 3. Nova variável → aloca registrador (aplicando LRU se necessário)
     *
     * @param nomeVariavel Nome da variável
     * @param valorInicial Valor inicial da variável
     * @return AllocationResult com registrador e instruções
     */
    public AllocationResult allocate(String nomeVariavel, int valorInicial) {
        List<String> instructions = new ArrayList<>();

        // 1. Verificar se variável já está em registrador
        LocationInfo location = locationTracker.locate(nomeVariavel);

        if (location.location == Location.REGISTER) {
            // Já tem registrador - apenas atualizar timestamp
            Registrador reg = location.registrador;
            updateLastUsedTime(reg);
            return new AllocationResult(reg, instructions);
        }

        // 2. Verificar se está na memória (precisa reload)
        if (location.location == Location.MEMORY) {
            // Alocar registrador para fazer reload (pode gerar spill)
            AllocationResult allocResult = allocateRegisterSlot();
            Registrador reg = allocResult.getRegistrador();

            // Adicionar instruções de spill se houver
            instructions.addAll(allocResult.getInstructions());

            // Gerar instrução LDA para carregar da memória
            String loadInstr = memoryManager.generateLoad(reg.getNome(), nomeVariavel);
            instructions.add(loadInstr);

            // Atualizar localização: agora está em registrador
            Variavel var = findVariavelByName(nomeVariavel);
            if (var != null) {
                var.setRegistrador(reg);
                symbolTable.put(var, reg);
            } else {
                var = new Variavel(nomeVariavel, reg);
                symbolTable.put(var, reg);
            }

            locationTracker.setInRegister(nomeVariavel, reg);
            updateLastUsedTime(reg);

            System.out.println("[RegisterAllocator] RELOAD: " + nomeVariavel + " da memória para " + reg.getNome());

            return new AllocationResult(reg, instructions);
        }

        // 3. Nova variável - alocar registrador (pode gerar spill)
        AllocationResult allocResult = allocateRegisterSlot();
        Registrador reg = allocResult.getRegistrador();

        // Adicionar instruções de spill se houver
        instructions.addAll(allocResult.getInstructions());

        Variavel novaVar = new Variavel(nomeVariavel, reg);
        reg.setValor(valorInicial);

        // Adicionar à symbol table
        symbolTable.put(novaVar, reg);
        locationTracker.setInRegister(nomeVariavel, reg);
        updateLastUsedTime(reg);

        return new AllocationResult(reg, instructions);
    }

    /**
     * Aloca um slot de registrador (R0-R7)
     * Aplica LRU se todos estiverem ocupados
     * @return Par com registrador e instruções de spill (se houver)
     */
    private AllocationResult allocateRegisterSlot() {
        Integer regNumber;
        List<String> spillInstructions = new ArrayList<>();

        if (!availableRegisters.isEmpty()) {
            // Tem registrador disponível
            regNumber = availableRegisters.poll();
        } else {
            // Todos ocupados - aplicar LRU com spill
            SpillResult spillResult = spillLeastRecentlyUsedWithInstruction();
            regNumber = spillResult.registradorLiberado;
            spillInstructions.add(spillResult.staInstruction);
        }

        usedRegisters.add(regNumber);
        Registrador reg = new Registrador("R" + regNumber, 0);
        return new AllocationResult(reg, spillInstructions);
    }

    /**
     * Busca objeto Variavel por nome (helper)
     */
    private Variavel findVariavelByName(String nome) {
        for (Variavel var : symbolTable.keySet()) {
            if (var.getNome().equals(nome)) {
                return var;
            }
        }
        return null;
    }

    /**
     * Atualiza o tempo de último uso de um registrador
     */
    public void updateLastUsedTime(Registrador reg) {
        lastUsedTime.put(reg, LocalDateTime.now());
    }

    /**
     * Classe auxiliar para retornar resultado do spill
     */
    private static class SpillResult {
        final int registradorLiberado;
        final String staInstruction;

        SpillResult(int registradorLiberado, String staInstruction) {
            this.registradorLiberado = registradorLiberado;
            this.staInstruction = staInstruction;
        }
    }

    /**
     * Libera o registrador menos recentemente usado (LRU) e retorna instrução STA
     * Salva valor na memória e gera instrução STA real
     *
     * @return SpillResult com número do registrador liberado e instrução STA
     */
    private SpillResult spillLeastRecentlyUsedWithInstruction() {
        Registrador lruReg = null;
        LocalDateTime oldestTime = LocalDateTime.now();
        Variavel lruVar = null;

        // Encontrar o registrador menos recentemente usado
        for (Map.Entry<Variavel, Registrador> entry : symbolTable.entrySet()) {
            Registrador reg = entry.getValue();
            LocalDateTime usedTime = lastUsedTime.getOrDefault(reg, LocalDateTime.MIN);

            if (usedTime.isBefore(oldestTime)) {
                oldestTime = usedTime;
                lruReg = reg;
                lruVar = entry.getKey();
            }
        }

        if (lruReg == null) {
            throw new IllegalStateException("Não foi possível encontrar registrador para spill");
        }

        // Salvar valor na memória
        int memAddr = memoryManager.storeRegister(lruVar.getNome(), lruReg.getValor());

        // Gerar instrução STA para salvar na memória
        // Formato: STA,Registrador,EnderecoMemoria\n
        String spillInstruction = String.format("STA,%s,%d\n", lruReg.getNome(), memAddr);

        // Log para debug
        System.out.println(String.format("[RegisterAllocator] SPILL: %s (valor=%d) do %s para memória[%d]",
            lruVar.getNome(), lruReg.getValor(), lruReg.getNome(), memAddr));

        // Extrair número do registrador
        int regNumber = Integer.parseInt(lruReg.getNome().replace("R", ""));

        // Atualizar localização: agora está na memória
        locationTracker.setInMemory(lruVar.getNome(), memAddr);

        // Remover da symbol table e liberar registrador
        symbolTable.remove(lruVar);
        lastUsedTime.remove(lruReg);
        usedRegisters.remove(regNumber);

        return new SpillResult(regNumber, spillInstruction);
    }

    /**
     * Método legado mantido para compatibilidade
     * @deprecated Use spillLeastRecentlyUsedWithInstruction() ao invés
     */
    @Deprecated
    private int spillLeastRecentlyUsed() {
        Registrador lruReg = null;
        LocalDateTime oldestTime = LocalDateTime.now();
        Variavel lruVar = null;

        // Encontrar o registrador menos recentemente usado
        for (Map.Entry<Variavel, Registrador> entry : symbolTable.entrySet()) {
            Registrador reg = entry.getValue();
            LocalDateTime usedTime = lastUsedTime.getOrDefault(reg, LocalDateTime.MIN);

            if (usedTime.isBefore(oldestTime)) {
                oldestTime = usedTime;
                lruReg = reg;
                lruVar = entry.getKey();
            }
        }

        if (lruReg == null) {
            throw new IllegalStateException("Não foi possível encontrar registrador para spill");
        }

        // Salvar valor na memória
        int memAddr = memoryManager.storeRegister(lruVar.getNome(), lruReg.getValor());

        // Gerar instrução STA para salvar na memória
        String spillInstruction = String.format("STA,%s,%d\n", lruReg.getNome(), memAddr);
        spillInstructions.add(spillInstruction);

        // Log para debug
        System.out.println(String.format("[RegisterAllocator] SPILL: %s (valor=%d) do %s para memória[%d]",
            lruVar.getNome(), lruReg.getValor(), lruReg.getNome(), memAddr));

        // Extrair número do registrador
        int regNumber = Integer.parseInt(lruReg.getNome().replace("R", ""));

        // Atualizar localização: agora está na memória
        locationTracker.setInMemory(lruVar.getNome(), memAddr);

        // Remover da symbol table e liberar registrador
        symbolTable.remove(lruVar);
        lastUsedTime.remove(lruReg);
        usedRegisters.remove(regNumber);

        return regNumber;
    }

    /**
     * Busca registrador por nome de variável
     */
    public Registrador findByVarName(String varName) {
        for (Map.Entry<Variavel, Registrador> entry : symbolTable.entrySet()) {
            if (entry.getKey().getNome().equals(varName)) {
                Registrador reg = entry.getValue();
                updateLastUsedTime(reg);
                return reg;
            }
        }
        return null;
    }

    /**
     * Retorna lista de instruções de spill geradas
     */
    public List<String> getSpillInstructions() {
        return new ArrayList<>(spillInstructions);
    }

    /**
     * Reseta o alocador (útil para testes)
     */
    public void reset() {
        symbolTable.clear();
        lastUsedTime.clear();
        availableRegisters.clear();
        usedRegisters.clear();
        spillInstructions.clear();

        memoryManager.reset();
        locationTracker.reset();

        for (int i = 0; i < MAX_REGISTERS; i++) {
            availableRegisters.offer(i);
        }
    }

    /**
     * Retorna estatísticas de uso
     */
    public String getStatistics() {
        return String.format(
            "Registradores em uso: %d/%d | Spills realizados: %d | %s | %s",
            usedRegisters.size(), MAX_REGISTERS, spillInstructions.size(),
            memoryManager.getStatistics(),
            locationTracker.getStatistics()
        );
    }
}

