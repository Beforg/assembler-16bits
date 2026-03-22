package com.unipampa.compiladorarquitetura16bits.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelGenerator {
    private static int labelCount = 0;

    public static String gerarLabel(LabelsCompilador prefix) {
        return prefix.getLabelEmString() + "_" + (labelCount++);
    }

    public static String substituirLabel(String code) {
        String[] linhas = code.split("\\n");
        Map<String, Integer> labelsTable = new HashMap<>();
        List<String> noLabels = new ArrayList<>();

        int count = 0;
        for (String linha : linhas) {
            String t = linha.trim();
            if (t.endsWith(":")) {
                String label = t.substring(0, t.length() - 1);
                labelsTable.put(label, count);
            } else {
                noLabels.add(t);
                count++;
            }
        }
        List<String> result = new ArrayList<>();
        for (String linha : noLabels) {
            String out = linha;
            for (String label : labelsTable.keySet()) {
                if (out.contains(label)) {
                    out = out.replace(label, labelsTable.get(label).toString());
                }
            }
            result.add(out);
        }
        return String.join("\n", result);
    }

    /**
     * Reseta o contador de labels para uma nova compilação
     */
    public static void reset() {
        labelCount = 0;
    }
}
