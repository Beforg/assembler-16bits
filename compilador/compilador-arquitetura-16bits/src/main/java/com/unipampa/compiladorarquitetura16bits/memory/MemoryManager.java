package com.unipampa.compiladorarquitetura16bits.memory;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Gerenciador de alocação de memória (endereços 0-15)
 * Aplica política LRU quando memória está cheia
 *
 * Nota: Como não há instrução de LOAD da memória, os valores são tratados como imediatos
 * Ex: Variável 'x' com valor 5 na memória → LDA,R0,5
 */
public class MemoryManager {
    private static final int MAX_MEMORY_SLOTS = 16; // 0-15

    // Mapa variável → slot de memória
    private final Map<String, MemorySlot> varToSlot = new HashMap<>();

    // Slots disponíveis (endereços livres)
    private final Queue<Integer> availableSlots = new LinkedList<>();

    // Rastreamento LRU para memória
    private final Map<String, LocalDateTime> lastAccessTime = new HashMap<>();

    public MemoryManager() {
        // Inicializar slots disponíveis (0-15)
        for (int i = 0; i < MAX_MEMORY_SLOTS; i++) {
            availableSlots.offer(i);
        }
    }

    /**
     * Aloca espaço na memória para uma variável
     * @param varName Nome da variável
     * @param value Valor a armazenar
     * @return Endereço alocado (0-15)
     * @throws IllegalStateException se memória estiver cheia e não puder aplicar LRU
     */
    public int allocate(String varName, int value) {
        // Se já existe, atualizar
        if (varToSlot.containsKey(varName)) {
            MemorySlot slot = varToSlot.get(varName);
            slot.setValue(value);
            updateAccessTime(varName);
            return slot.getAddress();
        }

        // Tentar alocar em slot disponível
        Integer address;
        if (!availableSlots.isEmpty()) {
            address = availableSlots.poll();
        } else {
            // Aplicar LRU: liberar slot menos recentemente usado
            address = freeLeastRecentlyUsed();
        }

        // Criar e armazenar slot
        MemorySlot slot = new MemorySlot(address, value, varName);
        varToSlot.put(varName, slot);
        updateAccessTime(varName);

        return address;
    }

    /**
     * Libera slot de memória de uma variável
     */
    public void free(String varName) {
        MemorySlot slot = varToSlot.remove(varName);
        if (slot != null) {
            availableSlots.offer(slot.getAddress());
            lastAccessTime.remove(varName);
        }
    }

    /**
     * Busca o slot de uma variável
     */
    public Optional<MemorySlot> getSlot(String varName) {
        MemorySlot slot = varToSlot.get(varName);
        if (slot != null) {
            updateAccessTime(varName);
        }
        return Optional.ofNullable(slot);
    }

    /**
     * Gera instrução para armazenar registrador na memória
     * Como não há STA real, apenas registramos o valor
     * @return Endereço onde foi salvo
     */
    public int storeRegister(String varName, int value) {
        return allocate(varName, value);
    }

    /**
     * Gera instrução LDA para carregar valor da memória
     * @param regName Nome do registrador destino (ex: "R0")
     * @param varName Nome da variável na memória
     * @return Instrução LDA formatada
     */
    public String generateLoad(String regName, String varName) {
        MemorySlot slot = varToSlot.get(varName);
        if (slot == null) {
            throw new IllegalArgumentException("Variável " + varName + " não está na memória");
        }
        updateAccessTime(varName);

        // Carregar valor imediato (simula load da memória)
        return String.format("LDA,%s,%d\n", regName, slot.getValue());
    }

    /**
     * Atualiza timestamp de último acesso
     */
    private void updateAccessTime(String varName) {
        lastAccessTime.put(varName, LocalDateTime.now());
    }

    /**
     * Aplica LRU: libera slot menos recentemente usado
     */
    private int freeLeastRecentlyUsed() {
        if (varToSlot.isEmpty()) {
            throw new IllegalStateException("Memória cheia e nenhuma variável para liberar");
        }

        // Encontrar variável menos recentemente usada
        String lruVar = null;
        LocalDateTime oldestTime = LocalDateTime.now();

        for (Map.Entry<String, MemorySlot> entry : varToSlot.entrySet()) {
            String varName = entry.getKey();
            LocalDateTime accessTime = lastAccessTime.getOrDefault(varName, LocalDateTime.MIN);

            if (accessTime.isBefore(oldestTime)) {
                oldestTime = accessTime;
                lruVar = varName;
            }
        }

        if (lruVar == null) {
            throw new IllegalStateException("Não foi possível encontrar variável para liberar");
        }

        // Liberar e retornar endereço
        MemorySlot freedSlot = varToSlot.remove(lruVar);
        lastAccessTime.remove(lruVar);

//        System.out.println("[MemoryManager] LRU: Liberando slot " + freedSlot.getAddress() +
//                          " (variável: " + lruVar + ")");

        return freedSlot.getAddress();
    }

    /**
     * Verifica se variável está na memória
     */
    public boolean contains(String varName) {
        return varToSlot.containsKey(varName);
    }

    /**
     * Retorna número de slots ocupados
     */
    public int getUsedSlots() {
        return varToSlot.size();
    }

    /**
     * Retorna número de slots disponíveis
     */
    public int getAvailableSlots() {
        return availableSlots.size();
    }

    /**
     * Reseta o gerenciador (útil para testes)
     */
    public void reset() {
        varToSlot.clear();
        availableSlots.clear();
        lastAccessTime.clear();

        for (int i = 0; i < MAX_MEMORY_SLOTS; i++) {
            availableSlots.offer(i);
        }
    }

    /**
     * Retorna estatísticas de uso
     */
    public String getStatistics() {
        return String.format("Memória: %d/%d slots ocupados",
            getUsedSlots(), MAX_MEMORY_SLOTS);
    }

    /**
     * Retorna mapa de variáveis na memória (debug)
     */
    public Map<String, MemorySlot> getMemoryMap() {
        return new HashMap<>(varToSlot);
    }
}

