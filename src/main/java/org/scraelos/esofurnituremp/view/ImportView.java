/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.github.peholmst.i18n4vaadin.annotations.Message;
import com.github.peholmst.i18n4vaadin.annotations.Messages;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.scraelos.esofurnituremp.model.ITEM_QUALITY;
import org.scraelos.esofurnituremp.model.RECIPE_TYPE;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import ru.xpoft.vaadin.VaadinView;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@VaadinView(ImportView.NAME)
@Secured({"ROLE_ADMIN"})
public class ImportView extends CustomComponent implements View {

    public static final String NAME = "import";
    private Header header;

    @Autowired
    private DBService dBService;
    private Bundle i18n=new Bundle();

    @Messages({
        @Message(key ="uploadDatamineXlsx",value = "Upload datamine xlsx"),
        @Message(key ="uploadEsoRawRecipeData",value = "Upload eso raw recipe data")
    })
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        header=new Header();
        header.build();
        DatamineUploadHandler datamineUploadHandler = new DatamineUploadHandler();
        Upload dataminexlsxUpload = new Upload(i18n.uploadDatamineXlsx(), datamineUploadHandler);
        dataminexlsxUpload.addSucceededListener(datamineUploadHandler);
        EsoRawStringRecipeUploadHandler esoRawStringRecipeUploadHandler = new EsoRawStringRecipeUploadHandler();
        Upload esoRawStringRecipeUpload = new Upload(i18n.uploadEsoRawRecipeData(), esoRawStringRecipeUploadHandler);
        esoRawStringRecipeUpload.addSucceededListener(esoRawStringRecipeUploadHandler);
        VerticalLayout vl = new VerticalLayout(header, dataminexlsxUpload, esoRawStringRecipeUpload);
        setCompositionRoot(vl);

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
                        Long id = getLongFromCell(idCell);
                        String textEn = getStringFromCell(textEnCell).replace("Diagram: ", "").replace("Design: ", "").replace("Pattern: ", "").replace("Blueprint: ", "").replace("Praxis: ", "").replace("Formula: ", "");
                        String textDe = getStringFromCell(textDeCell).replace("Skizze: ", "").replace("Entwurf: ", "").replace("Vorlage: ", "").replace("Blaupause: ", "").replace("Anleitung: ", "").replace("Formel: ", "").replace("^f", "").replace("^m", "").replace(":m", "").replace(":n", "").replace(":f", "").replace(":p", "");
                        String textFr = getStringFromCell(textFrCell).replace("Diagramme : ", "").replace("Croquis : ", "").replace("Préparation : ", "").replace("Plan : ", "").replace("Praxis : ", "").replace("Formule : ", "").replace("^f", "").replace("^m", "");
                        dBService.addItemRecipe(id, textEn, textDe, textFr);
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
                    if (s.getSheetName().equals("Furniture List 2.7.4")) {
                        Iterator<Row> rowIterator = s.rowIterator();
                        rowIterator.next();
                        while (rowIterator.hasNext()) {
                            Row r = rowIterator.next();
                            Cell idCell = r.getCell(0);
                            Cell nameCell = r.getCell(1);
                            Cell catCell = r.getCell(2);
                            Cell subCatCell = r.getCell(3);
                            Cell qualityCell = r.getCell(4);
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
                            dBService.addFurnitureItem(id, name, cat, subCat, quality);

                        }
                    } else if (s.getSheetName().equals("Plans 2.7.4")) {
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

    private String getStringFromCell(Cell c) {
        String result = null;
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
        return result;
    }

    private Long getLongFromCell(Cell c) {
        Long result = null;
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
        return result;
    }

}
