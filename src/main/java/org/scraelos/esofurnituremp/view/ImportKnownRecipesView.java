/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.github.peholmst.i18n4vaadin.annotations.Message;
import com.github.peholmst.i18n4vaadin.annotations.Messages;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.scraelos.esofurnituremp.model.ESO_SERVER;
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.scraelos.esofurnituremp.model.ITEM_QUALITY;
import org.scraelos.esofurnituremp.model.Recipe;
import org.scraelos.esofurnituremp.model.tools.LuaDecoder;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
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
@VaadinView(ImportKnownRecipesView.NAME)
@Secured({"ROLE_USER"})
public class ImportKnownRecipesView extends CustomComponent implements View {

    public static final String NAME = "importknownrecipes";
    private Header header;

    private ComboBox server;
    private Upload upload;
    private Table table;
    private HierarchicalContainer container;
    private Button importButton;
    

    @Autowired
    private DBService dBService;
    private Bundle l18n=new Bundle();
    private static final Logger LOG = Logger.getLogger(ImportKnownRecipesView.class.getName());

    @Messages({
        @Message(key = "server", value = "Server"),
        @Message(key = "uploadCraftStoreFile", value = "Upload CraftStoreFixedAndImproved.lua"),
        @Message(key = "importNewRecipes", value = "Import new recipes"),
        @Message(key = "newKnownRecipes", value = "New known recipes"),
        @Message(key = "recipe", value = "Recipe"),
        @Message(key = "characterName", value = "Character Name"),
        @Message(key = "uploadErrorTitle", value = "UploadError"),
        @Message(key = "uploadErrorIDNotFound", value = "ID {0} not found in file!"),
        
    })
    public ImportKnownRecipesView() {
        header = new Header();
        this.setSizeFull();
        UploadHandler handler = new UploadHandler();
        server = new ComboBox(l18n.server(), Arrays.asList(ESO_SERVER.values()));
        server.setNullSelectionAllowed(false);
        server.setValue(SpringSecurityHelper.getUser().getEsoServer());
        upload = new Upload(l18n.uploadCraftStoreFile(), handler);
        upload.addSucceededListener(handler);
        upload.setImmediate(true);
        importButton = new Button(l18n.importNewRecipes(), new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                importButton.setEnabled(false);
                dBService.addKnownRecipes(container, (ESO_SERVER) server.getValue(), SpringSecurityHelper.getUser());
                container.removeAllItems();
                getUI().getNavigator().navigateTo(KnownRecipesView.NAME);
            }
        });
        importButton.setEnabled(false);
        HorizontalLayout hl = new HorizontalLayout(server, upload, importButton);
        hl.setComponentAlignment(importButton, Alignment.BOTTOM_LEFT);
        table = new Table(l18n.newKnownRecipes());
        table.setSizeFull();
        container = new HierarchicalContainer();
        container.addContainerProperty("recipe", Recipe.class, null);
        container.addContainerProperty("characterName", String.class, null);

        table.setContainerDataSource(container);
        table.setVisibleColumns(new Object[]{"recipe", "characterName"});
        table.setColumnHeaders(new String[]{l18n.recipe(), l18n.characterName()});
        table.setCellStyleGenerator(new CustomCellStyleGenerator());
        VerticalLayout vl = new VerticalLayout(header, hl, table);
        vl.setSizeFull();
        vl.setExpandRatio(table, 1f);
        setCompositionRoot(vl);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        header.build();
    }

    private class CustomCellStyleGenerator implements Table.CellStyleGenerator {

        @Override
        public String getStyle(Table source, Object itemId, Object propertyId) {
            String result = null;

            if (propertyId != null && propertyId.equals("recipe")) {
                Recipe value = (Recipe) source.getItem(itemId).getItemProperty(propertyId).getValue();
                ITEM_QUALITY itemQuality = value.getItemQuality();
                if (itemQuality != null) {
                    result = itemQuality.name().toLowerCase();
                }

            }

            return result;
        }

    }

    private class UploadHandler implements Upload.Receiver, Upload.SucceededListener {

        private ByteArrayOutputStream baos = new ByteArrayOutputStream();

        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            baos = new ByteArrayOutputStream();
            return baos;
        }

        @Override
        public void uploadSucceeded(Upload.SucceededEvent event) {
            container.removeAllItems();
            byte[] toByteArray = baos.toByteArray();
            String text = new String(toByteArray);
            JSONObject jsonFromLua = LuaDecoder.getJsonFromLua(text);
            String userId = "@" + SpringSecurityHelper.getUser().getEsoId();
            JSONObject getDefault = jsonFromLua.getJSONObject("Default");
            try {
                JSONObject furnisherknowledge = getDefault.getJSONObject(userId).getJSONObject("$AccountWide").getJSONObject("furnisher").getJSONObject("knowledge");
                for (String characterName : furnisherknowledge.keySet()) {
                    JSONObject characterRecipes = furnisherknowledge.getJSONObject(characterName);
                    for (String recipeIdString : characterRecipes.keySet()) {
                        Boolean known = characterRecipes.getBoolean(recipeIdString);
                        if (known) {
                            Recipe recipe = dBService.getRecipe(Long.valueOf(recipeIdString));
                            if (!dBService.isRecipeKnown(recipe, characterName, (ESO_SERVER) server.getValue(), SpringSecurityHelper.getUser())) {
                                Item item = container.addItem(characterName + recipeIdString);
                                item.getItemProperty("characterName").setValue(characterName);
                                item.getItemProperty("recipe").setValue(recipe);
                            }

                        }
                    }
                }
                if (container.size() > 0) {
                    importButton.setEnabled(true);
                }
            } catch (JSONException ex) {
                Notification.show(l18n.uploadErrorTitle(), l18n.uploadErrorIDNotFound(userId), Notification.Type.ERROR_MESSAGE);
            }
        }

    }

}
