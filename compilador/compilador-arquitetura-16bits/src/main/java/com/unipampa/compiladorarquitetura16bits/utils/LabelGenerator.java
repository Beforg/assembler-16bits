package com.unipampa.compiladorarquitetura16bits.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelGenerator {
    private static int labelCount = 0;

    public static String generateLabel(String prefix) {
        return prefix + "_" + (labelCount++);
    }

    public static String replaceLabel(String code) {
        String[] linhas = code.split("\\n");
        Map<String, Integer> labelsTable = new HashMap<>();
        List<String> noLabels = new ArrayList<>();

        int count = 0;

        for (String linha : linhas) {
            if (linha.endsWith(":")) {
                String label = linha.replace(":", "").trim();
                labelsTable.put(label, count);
            } else {
                noLabels.add(linha);
                count++;
            }
        }
        List<String> result = new ArrayList<>();
        for (String linha : noLabels) {
            for (String label : labelsTable.keySet()) {
                if (linha.contains(label)) {
                    linha = linha.replace(label, labelsTable.get(label).toString());

                }
            }
            result.add(linha);
        }
        return String.join("\n", result);
    }
}
