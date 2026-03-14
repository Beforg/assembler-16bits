package com.unipampa.compiladorarquitetura16bits.utils;

public enum LabelsCompilador {
    SE_INICIO("SE_INICIO"),
    FIM_SE("FIM_SE"),
    SENAO_INICIO("SENAO_INICIO"),
    ENQUANTO_INICIO("ENQUANTO_INICIO"),
    FIM_ENQUANTO("FIM_ENQUANTO"),
    PARA_INICIO("PARA_INICIO"),
    FIM_PARA("FIM_PARA");

    public final String label;

    LabelsCompilador(String label) {
        this.label = label;
    }

    public String getLabelEmString() {
        return label;
    }
}
