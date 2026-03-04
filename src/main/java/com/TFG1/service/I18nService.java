package com.TFG1.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class I18nService {

    // Mapeo
    private static final Map<String, Map<String, String>> translations = new HashMap<>();

    static {
        loadLanguage("ES");
        loadLanguage("EN");
    }

    private static void loadLanguage(String lang) {

        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = I18nService.class.getClassLoader().getResourceAsStream("lang/" + lang + ".json")) {

            if (is != null) {
                // Leemos el JSON y se convierte en un diccionario
                Map<String, String> map = mapper.readValue(is, new TypeReference<Map<String, String>>() {
                });
                translations.put(lang, map);
            }

        } catch (Exception e) {
            System.err.println("Error al cargar idioma: " + lang + ".json");
        }
    }

    public static String get(String lang, String key) {
        // Español por defecto
        Map<String, String> langMap = translations.getOrDefault(lang, translations.get("ES"));

        // Devolvemos la traducción
        return langMap.getOrDefault(key, "[" + key + "]");
    }
}