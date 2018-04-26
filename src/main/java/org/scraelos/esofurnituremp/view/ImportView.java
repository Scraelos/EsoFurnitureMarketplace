/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.CustomComponent;
import com.vaadin.v7.ui.Upload;
import com.vaadin.v7.ui.VerticalLayout;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;
import org.scraelos.esofurnituremp.Bundle;
import org.scraelos.esofurnituremp.model.ITEM_QUALITY;
import org.scraelos.esofurnituremp.model.RECIPE_TYPE;
import org.scraelos.esofurnituremp.model.tools.LuaDecoder;
import org.scraelos.esofurnituremp.service.DBService;
import org.scraelos.esofurnituremp.service.InsertExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@SpringView(name = ImportView.NAME)
@Secured({"ROLE_ADMIN"})
public class ImportView extends CustomComponent implements View {

    private static final Logger LOG = Logger.getLogger(ImportView.class.getName());

    public static final String NAME = "import";
    private Header header;

    @Autowired
    private DBService dBService;

    @Autowired
    private InsertExecutor executor;

    private Bundle i18n = new Bundle();

    public ImportView() {
        header = new Header();
        DatamineUploadHandler datamineUploadHandler = new DatamineUploadHandler();
        Upload dataminexlsxUpload = new Upload(i18n.uploadDatamineXlsx(), datamineUploadHandler);
        dataminexlsxUpload.addSucceededListener(datamineUploadHandler);
        EsoRawStringRecipeUploadHandler esoRawStringRecipeUploadHandler = new EsoRawStringRecipeUploadHandler();
        Upload esoRawStringRecipeUpload = new Upload(i18n.uploadEsoRawRecipeData(), esoRawStringRecipeUploadHandler);
        esoRawStringRecipeUpload.addSucceededListener(esoRawStringRecipeUploadHandler);
        EsoRawStringUploadHandler esoRawStringUploadHandler = new EsoRawStringUploadHandler();
        Upload esoRawStringUpload = new Upload("Update item translations", esoRawStringUploadHandler);
        esoRawStringUpload.addSucceededListener(esoRawStringUploadHandler);
        EsoRawCatStringUploadHandler esoRawCatStringUploadHandler = new EsoRawCatStringUploadHandler();
        Upload esoRawCatStringUpload = new Upload("Update cat translations", esoRawCatStringUploadHandler);
        esoRawCatStringUpload.addSucceededListener(esoRawCatStringUploadHandler);
        FurnitureDumpUploadHandler furnitureDumpUploadHandler = new FurnitureDumpUploadHandler();
        Upload uploadNewDatamine = new Upload("Update new datamine", furnitureDumpUploadHandler);
        uploadNewDatamine.addSucceededListener(furnitureDumpUploadHandler);
        VerticalLayout vl = new VerticalLayout(header, dataminexlsxUpload, esoRawStringRecipeUpload, esoRawStringUpload, uploadNewDatamine, esoRawCatStringUpload);
        setCompositionRoot(vl);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        header.build();
    }

    private class EsoRawStringRecipeUploadHandler implements Upload.Receiver, Upload.SucceededListener {

        private ByteArrayOutputStream baos;

        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            baos = new ByteArrayOutputStream();
            return baos;
        }

        @Override
        public void uploadSucceeded(Upload.SucceededEvent event) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                XSSFWorkbook wb = new XSSFWorkbook(bais);
                Iterator<Sheet> sheetIterator = wb.sheetIterator();
                while (sheetIterator.hasNext()) {

                    Sheet s = sheetIterator.next();
                    Iterator<Row> rowIterator = s.rowIterator();
                    rowIterator.next();
                    while (rowIterator.hasNext()) {
                        Row r = rowIterator.next();
                        Cell idCell = r.getCell(3);
                        Cell textEnCell = r.getCell(4);
                        Cell textDeCell = r.getCell(5);
                        Cell textFrCell = r.getCell(6);
                        Cell textRuCell = r.getCell(7);
                        Long id = getLongFromCell(idCell);
                        String textEn = getStringFromCell(textEnCell).replace("Diagram: ", "").replace("Design: ", "").replace("Pattern: ", "").replace("Blueprint: ", "").replace("Praxis: ", "").replace("Formula: ", "");
                        String textDe = getStringFromCell(textDeCell).replace("Skizze: ", "").replace("Entwurf: ", "").replace("Vorlage: ", "").replace("Blaupause: ", "").replace("Anleitung: ", "").replace("Formel: ", "").replace("^f", "").replace("^m", "").replace(":m", "").replace(":n", "").replace(":f", "").replace(":p", "");
                        String textFr = getStringFromCell(textFrCell).replace("Diagramme : ", "").replace("Croquis : ", "").replace("Préparation : ", "").replace("Plan : ", "").replace("Praxis : ", "").replace("Formule : ", "").replace("^f", "").replace("^m", "");
                        String textRu = getStringFromCell(textRuCell).replace("диаграмма: ", "").replace("проект: ", "").replace("шаблон: ", "").replace("чертеж: ", "").replace("схема: ", "").replace("формула: ", "").replace("Diagram: ", "").replace("Design: ", "").replace("Pattern: ", "").replace("Blueprint: ", "").replace("Praxis: ", "").replace("Formula: ", "").replace("^f", "").replace("^m", "");
                        dBService.addItemRecipe(id, textEn, textDe, textFr, textRu);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private class EsoRawStringUploadHandler implements Upload.Receiver, Upload.SucceededListener {

        private ByteArrayOutputStream baos;

        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            baos = new ByteArrayOutputStream();
            return baos;
        }

        @Override
        public void uploadSucceeded(Upload.SucceededEvent event) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                XSSFWorkbook wb = new XSSFWorkbook(bais);
                Iterator<Sheet> sheetIterator = wb.sheetIterator();
                while (sheetIterator.hasNext()) {

                    Sheet s = sheetIterator.next();
                    Iterator<Row> rowIterator = s.rowIterator();
                    rowIterator.next();
                    while (rowIterator.hasNext()) {
                        Row r = rowIterator.next();
                        Cell idCell = r.getCell(3);
                        Cell textEnCell = r.getCell(4);
                        Cell textDeCell = r.getCell(5);
                        Cell textFrCell = r.getCell(6);
                        Cell textRuCell = r.getCell(7);
                        Long id = getLongFromCell(idCell);
                        //String textEn = getStringFromCell(textEnCell).replace("Diagram: ", "").replace("Design: ", "").replace("Pattern: ", "").replace("Blueprint: ", "").replace("Praxis: ", "").replace("Formula: ", "").replace("^f", "").replace("^m", "").replace(":m", "").replace(":n", "").replace(":f", "").replace(":p", "").replace("^n", "");
                        //String textDe = getStringFromCell(textDeCell).replace("Skizze: ", "").replace("Entwurf: ", "").replace("Vorlage: ", "").replace("Blaupause: ", "").replace("Anleitung: ", "").replace("Formel: ", "").replace("^f", "").replace("^m", "").replace(":m", "").replace(":n", "").replace(":f", "").replace(":p", "").replace("^n", "");
                        //String textFr = getStringFromCell(textFrCell).replace("Diagramme : ", "").replace("Croquis : ", "").replace("Préparation : ", "").replace("Plan : ", "").replace("Praxis : ", "").replace("Formule : ", "").replace("^f", "").replace("^m", "");
                        //String textRu = getStringFromCell(textRuCell).replace("диаграмма: ", "").replace("проект: ", "").replace("шаблон: ", "").replace("чертеж: ", "").replace("схема: ", "").replace("формула: ", "").replace("^f", "").replace("^m", "");
                        String textEn = getStringFromCell(textEnCell).replace("^f", "").replace("^m", "").replace(":m", "").replace(":n", "").replace(":f", "").replace(":p", "").replace("^n", "");
                        String textDe = getStringFromCell(textDeCell).replace("^f", "").replace("^m", "").replace(":m", "").replace(":n", "").replace(":f", "").replace(":p", "").replace("^n", "");
                        String textFr = getStringFromCell(textFrCell).replace("^f", "").replace("^m", "");
                        String textRu = getStringFromCell(textRuCell).replace("^f", "").replace("^m", "");
                        TransImportTask task = new TransImportTask(id, textEn, textDe, textFr, textRu);
                        executor.execute(task);
                        //dBService.setItemTranslation(id, textEn, textDe, textFr, textRu);
                    }
                    for (;;) {
                        int count = executor.getActiveCount();
                        LOG.log(Level.INFO, "Active Threads : {0} Queue size:{1}", new Object[]{count, executor.getThreadPoolExecutor().getQueue().size()});
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                        if (count == 0) {
                            break;
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Component
        @Scope("prototype")
        private class TransImportTask implements Runnable {

            private final Long id;
            private final String textEn;
            private final String textDe;
            private final String textFr;
            private final String textRu;

            public TransImportTask(Long id, String textEn, String textDe, String textFr, String textRu) {
                this.id = id;
                this.textEn = textEn;
                this.textDe = textDe;
                this.textFr = textFr;
                this.textRu = textRu;
            }

            @Override
            public void run() {
                dBService.setItemTranslation(id, textEn, textDe, textFr, textRu);
            }

        }

    }

    private class EsoRawCatStringUploadHandler implements Upload.Receiver, Upload.SucceededListener {

        private ByteArrayOutputStream baos;

        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            baos = new ByteArrayOutputStream();
            return baos;
        }

        @Override
        public void uploadSucceeded(Upload.SucceededEvent event) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                XSSFWorkbook wb = new XSSFWorkbook(bais);
                Iterator<Sheet> sheetIterator = wb.sheetIterator();
                while (sheetIterator.hasNext()) {

                    Sheet s = sheetIterator.next();
                    Iterator<Row> rowIterator = s.rowIterator();
                    rowIterator.next();
                    while (rowIterator.hasNext()) {
                        Row r = rowIterator.next();
                        Cell idCell = r.getCell(3);
                        Cell textEnCell = r.getCell(4);
                        Cell textDeCell = r.getCell(5);
                        Cell textFrCell = r.getCell(6);
                        Cell textRuCell = r.getCell(7);
                        Long id = getLongFromCell(idCell);
                        String textEn = getStringFromCell(textEnCell).replace("^f", "").replace("^m", "").replace(":m", "").replace(":n", "").replace(":f", "").replace(":p", "").replace("^n", "");
                        String textDe = getStringFromCell(textDeCell).replace("^f", "").replace("^m", "").replace(":m", "").replace(":n", "").replace(":f", "").replace(":p", "").replace("^n", "");
                        String textFr = getStringFromCell(textFrCell).replace("^f", "").replace("^m", "");
                        String textRu = getStringFromCell(textRuCell).replace("^f", "").replace("^m", "");
                        dBService.setCatTranslation(id, textEn, textDe, textFr, textRu);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private class DatamineUploadHandler implements Upload.Receiver, Upload.SucceededListener {

        private ByteArrayOutputStream baos;

        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            baos = new ByteArrayOutputStream();
            return baos;
        }

        @Override
        public void uploadSucceeded(Upload.SucceededEvent event) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                XSSFWorkbook wb = new XSSFWorkbook(bais);
                Iterator<Sheet> sheetIterator = wb.sheetIterator();
                while (sheetIterator.hasNext()) {
                    Sheet s = sheetIterator.next();
                    if (s.getSheetName().equals("Furniture List 3.1.2")) {
                        Iterator<Row> rowIterator = s.rowIterator();
                        rowIterator.next();
                        while (rowIterator.hasNext()) {
                            Row r = rowIterator.next();
                            Cell idCell = r.getCell(0);
                            Cell nameCell = r.getCell(1);
                            Cell catCell = r.getCell(2);
                            Cell subCatCell = r.getCell(3);
                            Cell qualityCell = r.getCell(4);
                            Cell linkCell = r.getCell(12);
                            Long id = getLongFromCell(idCell);
                            String name = getStringFromCell(nameCell);
                            String cat = getStringFromCell(catCell);
                            if (cat == null) {
                                cat = "no category";
                            }

                            String subCat = getStringFromCell(subCatCell);
                            if (subCat == null) {
                                subCat = "no subcategory";
                            }
                            String qualityString = getStringFromCell(qualityCell);
                            ITEM_QUALITY quality = ITEM_QUALITY.valueOf(qualityString);
                            String itemLink = getStringFromCell(linkCell);
                            dBService.addFurnitureItem(id, name, cat, subCat, quality, itemLink);

                        }
                    } else if (s.getSheetName().equals("Plans 3.1.2")) {
                        Pattern p = Pattern.compile("^(.*)\\((\\d*)");
                        Iterator<Row> rowIterator = s.rowIterator();
                        rowIterator.next();
                        while (rowIterator.hasNext()) {
                            Row r = rowIterator.next();
                            Cell idCell = r.getCell(0);
                            Cell nameCell = r.getCell(1);
                            Cell typeCell = r.getCell(2);
                            Cell qualityCell = r.getCell(6);
                            Long id = getLongFromCell(idCell);
                            String name = getStringFromCell(nameCell);
                            String typeString = getStringFromCell(typeCell);
                            RECIPE_TYPE recipeType = RECIPE_TYPE.valueOf(typeString);
                            String qualityString = getStringFromCell(qualityCell);
                            ITEM_QUALITY itemQuality = ITEM_QUALITY.valueOf(qualityString);
                            Map<String, Integer> ingredientMap = new HashMap<String, Integer>();
                            for (int i = 8; i < 15; i++) {
                                Cell iCell = r.getCell(i);
                                if (iCell != null) {
                                    String ingredient = getStringFromCell(iCell);
                                    if (ingredient != null) {
                                        Matcher matcher = p.matcher(ingredient.trim());
                                        if (matcher.find()) {
                                            String group1 = matcher.group(1);
                                            String group2 = matcher.group(2);
                                            Integer count = new Integer(group2);
                                            ingredientMap.put(group1, count);
                                        }
                                    }
                                }

                            }
                            dBService.addFurnitureRecipe(id, name, recipeType, itemQuality, ingredientMap);

                        }

                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private class FurnitureDumpUploadHandler implements Upload.Receiver, Upload.SucceededListener {

        private ByteArrayOutputStream baos;

        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            baos = new ByteArrayOutputStream();
            return baos;
        }

        @Override
        public void uploadSucceeded(Upload.SucceededEvent event) {
            byte[] toByteArray = baos.toByteArray();
            String text = new String(toByteArray);
            JSONObject jsonFromLua = LuaDecoder.getJsonFromLua(text);
            Iterator keys = jsonFromLua.keys();
            while (keys.hasNext()) {
                String furnitureIdString = (String) keys.next();
                Long furnitureId = Long.valueOf(furnitureIdString);
                JSONObject furnitureObject = jsonFromLua.getJSONObject(furnitureIdString);
                Long furnitureTheme = null;
                String itemLink = null;
                Long quality = null;
                Long categoryId = null;
                Long subcategoryId = null;
                Long recipeId = null;
                String icon = null;
                String recipeIcon = null;
                String recipeLink = null;
                List<Long[]> ingredients = new ArrayList<>();
                try {
                    furnitureTheme = furnitureObject.getLong("furnitureTheme");
                } catch (JSONException ex) {

                }
                try {
                    itemLink = furnitureObject.getString("link");
                } catch (JSONException ex) {

                }
                try {
                    quality = furnitureObject.getLong("quality");
                } catch (JSONException ex) {

                }
                try {
                    categoryId = furnitureObject.getLong("categoryId");
                } catch (JSONException ex) {

                }
                try {
                    subcategoryId = furnitureObject.getLong("subcategoryId");
                } catch (JSONException ex) {

                }
                try {
                    icon = furnitureObject.getString("icon");
                } catch (JSONException ex) {

                }
                try {
                    JSONObject recipeObject = furnitureObject.getJSONObject("recipe");
                    try {
                        recipeId = recipeObject.getLong("id");
                    } catch (JSONException ex) {

                    }
                    try {
                        recipeIcon = recipeObject.getString("icon");
                    } catch (JSONException ex) {

                    }
                    try {
                        recipeLink = recipeObject.getString("link");
                    } catch (JSONException ex) {

                    }
                    try {
                        JSONObject ingredientsObject = recipeObject.getJSONObject("ingredients");
                        Iterator<String> ingredientKeys = ingredientsObject.keys();
                        while (ingredientKeys.hasNext()) {
                            String nextIngredientKey = ingredientKeys.next();
                            JSONObject ingredientObject = ingredientsObject.getJSONObject(nextIngredientKey);
                            Long ingredientId = ingredientObject.getLong("id");
                            Long amount = ingredientObject.getLong("amountRequired");
                            ingredients.add(new Long[]{ingredientId, amount});
                        }
                    } catch (JSONException ex) {

                    }
                } catch (JSONException ex) {

                }
                ImportTask task = new ImportTask(furnitureId, furnitureTheme, itemLink, quality, categoryId, subcategoryId, recipeId, icon, recipeIcon, recipeLink, ingredients);
                executor.execute(task);
                LOG.log(Level.INFO, "{0};{1};{2};{3};{4};{5};{6};{7};{8};{9};{10}", new Object[]{furnitureId, furnitureTheme, itemLink, icon, quality, categoryId, subcategoryId, recipeId, recipeIcon, recipeLink, ingredients.size()});

            }
            for (;;) {
                int count = executor.getActiveCount();
                LOG.log(Level.INFO, "Active Threads : {0} Queue size:{1}", new Object[]{count, executor.getThreadPoolExecutor().getQueue().size()});
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
                if (count == 0) {
                    break;
                }
            }
        }

        @Component
        @Scope("prototype")
        private class ImportTask implements Runnable {

            private final Long furnitureId;
            private final Long furnitureTheme;
            private final String itemLink;
            private final Long quality;
            private final Long categoryId;
            private final Long subcategoryId;
            private final Long recipeId;
            private final String icon;
            private final String recipeIcon;
            private final String recipeLink;
            private final List<Long[]> ingredients;

            public ImportTask(Long furnitureId, Long furnitureTheme, String itemLink, Long quality, Long categoryId, Long subcategoryId, Long recipeId, String icon, String recipeIcon, String recipeLink, List<Long[]> ingredients) {
                this.furnitureId = furnitureId;
                this.furnitureTheme = furnitureTheme;
                this.itemLink = itemLink;
                this.quality = quality;
                this.categoryId = categoryId;
                this.subcategoryId = subcategoryId;
                this.recipeId = recipeId;
                this.icon = icon;
                this.recipeIcon = recipeIcon;
                this.recipeLink = recipeLink;
                this.ingredients = ingredients;
            }

            @Override
            public void run() {
                dBService.importFurnitureItem(furnitureId, furnitureTheme, itemLink, quality, categoryId, subcategoryId, recipeId, icon, recipeIcon, recipeLink, ingredients);
            }

        }

    }

    private String getStringFromCell(Cell c) {
        String result = null;
        if (c != null) {
            switch (c.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    result = c.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    Double numValue = c.getNumericCellValue();
                    result = Integer.toString(numValue.intValue());
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    switch (c.getCachedFormulaResultType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            Double nnumValue = c.getNumericCellValue();
                            result = Integer.toString(nnumValue.intValue());
                            break;
                        case Cell.CELL_TYPE_STRING:
                            result = c.getStringCellValue();
                            break;
                    }
            }
        }

        return result;
    }

    private Long getLongFromCell(Cell c) {
        Long result = null;
        if (c != null) {
            switch (c.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    result = Long.valueOf(c.getStringCellValue());
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    Double d = c.getNumericCellValue();
                    result = d.longValue();
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    switch (c.getCachedFormulaResultType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            Double dd = c.getNumericCellValue();
                            result = dd.longValue();
                            break;
                        case Cell.CELL_TYPE_STRING:
                            result = Long.valueOf(c.getStringCellValue());
                            break;
                    }
                    break;
            }
        }
        return result;
    }

}
