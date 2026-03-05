package com.TFG1.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class I18nService {

    // Mapeo
    private static final Map<String, Map<String, String>> translations = new HashMap<>();

    static {
        loadTranslations();
    }

    private static void loadTranslations() {
        try (InputStream is = I18nService.class.getClassLoader().getResourceAsStream("lang/translations.xlsx")) {
            if (is != null) {
                Workbook workbook = WorkbookFactory.create(is);
                Sheet sheet = workbook.getSheetAt(0);

                Row headerRow = sheet.getRow(0);
                Map<Integer, String> langCols = new HashMap<>();
                if (headerRow != null) {
                    for (int i = 1; i < headerRow.getLastCellNum(); i++) {
                        Cell cell = headerRow.getCell(i);
                        if (cell != null && cell.getCellType() == CellType.STRING) {
                            String lang = cell.getStringCellValue().trim().toUpperCase();
                            langCols.put(i, lang);
                            translations.put(lang, new HashMap<>());
                        }
                    }
                }

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell keyCell = row.getCell(0);
                        if (keyCell != null && keyCell.getCellType() == CellType.STRING) {
                            String key = keyCell.getStringCellValue().trim();
                            if (!key.isEmpty()) {
                                for (Map.Entry<Integer, String> entry : langCols.entrySet()) {
                                    Cell valCell = row.getCell(entry.getKey());
                                    if (valCell != null && valCell.getCellType() == CellType.STRING) {
                                        String value = valCell.getStringCellValue();
                                        translations.get(entry.getValue()).put(key, value);
                                    }
                                }
                            }
                        }
                    }
                }
                workbook.close();
            } else {
                System.err.println("No se encontro archivo: lang/translations.xlsx");
            }
        } catch (Exception e) {
            System.err.println("Error al cargar diccionario de idiomas translations.xlsx");
            e.printStackTrace();
        }
    }

    public static String get(String lang, String key) {
        // Español por defecto
        Map<String, String> langMap = translations.getOrDefault(lang, translations.get("ES"));

        if (langMap == null || !langMap.containsKey(key)) {
            return "[" + key + "]";
        }

        // Devolvemos la traducción
        return langMap.get(key);
    }
}