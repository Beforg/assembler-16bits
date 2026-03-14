package com.unipampa.compiladorarquitetura16bits.memory;

import com.unipampa.compiladorarquitetura16bits.model.Registrador;

import java.util.HashMap;
import java.util.Map;

/**
 * Rastreia a localização de cada variável: REGISTER, MEMORY ou NOT_FOUND
 * Fornece API unificada para descobrir onde está cada variável
 */
public class VariableLocationTracker {

    /**
     * Localização possível de uma variável
     */
    public enum Location {
        REGISTER,   // Variável está em um registrador
        MEMORY,     // Variável está na memória
        NOT_FOUND   // Variável não foi declarada
    }

    /**
     * Informações sobre localização de uma variável
     */
    public static class LocationInfo {
        public final Location location;
        public final Registrador registrador;  // null se não estiver em registrador
        public final int memoryAddress;        // -1 se não estiver na memória

        public LocationInfo(Location location, Registrador registrador, int memoryAddress) {
            this.location = location;
            this.registrador = registrador;
            this.memoryAddress = memoryAddress;
        }

        @Override
        public String toString() {
            switch (location) {
                case REGISTER:
                    return "REGISTER[" + registrador.getNome() + "]";
                case MEMORY:
                    return "MEMORY[addr=" + memoryAddress + "]";
                default:
                    return "NOT_FOUND";
            }
        }
    }

    // Mapa de variável → informações de localização
    private final Map<String, LocationInfo> locations = new HashMap<>();

    /**
     * Atualiza localização de uma variável para REGISTER
     */
    public void setInRegister(String varName, Registrador registrador) {
        LocationInfo info = new LocationInfo(Location.REGISTER, registrador, -1);
        locations.put(varName, info);
    }

    /**
     * Atualiza localização de uma variável para MEMORY
     */
    public void setInMemory(String varName, int memoryAddress) {
        LocationInfo info = new LocationInfo(Location.MEMORY, null, memoryAddress);
        locations.put(varName, info);
    }

    /**
     * Remove variável do rastreamento
     */
    public void remove(String varName) {
        locations.remove(varName);
    }

    /**
     * Localiza onde uma variável está
     */
    public LocationInfo locate(String varName) {
        return locations.getOrDefault(varName,
            new LocationInfo(Location.NOT_FOUND, null, -1));
    }

    /**
     * Verifica se variável existe (em qualquer lugar)
     */
    public boolean exists(String varName) {
        return locations.containsKey(varName);
    }

    /**
     * Lista todas as variáveis em registradores
     */
    public Map<String, LocationInfo> getVariablesInRegisters() {
        Map<String, LocationInfo> result = new HashMap<>();
        for (Map.Entry<String, LocationInfo> entry : locations.entrySet()) {
            if (entry.getValue().location == Location.REGISTER) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Lista todas as variáveis na memória
     */
    public Map<String, LocationInfo> getVariablesInMemory() {
        Map<String, LocationInfo> result = new HashMap<>();
        for (Map.Entry<String, LocationInfo> entry : locations.entrySet()) {
            if (entry.getValue().location == Location.MEMORY) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Reseta o rastreador
     */
    public void reset() {
        locations.clear();
    }

    /**
     * Retorna estatísticas
     */
    public String getStatistics() {
        int inRegs = (int) locations.values().stream()
            .filter(loc -> loc.location == Location.REGISTER)
            .count();
        int inMem = (int) locations.values().stream()
            .filter(loc -> loc.location == Location.MEMORY)
            .count();

        return String.format("Variáveis: %d em registradores, %d na memória", inRegs, inMem);
    }
}

