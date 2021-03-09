/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.github.peholmst.i18n4vaadin.LocaleChangedEvent;
import com.github.peholmst.i18n4vaadin.LocaleChangedListener;
import com.github.peholmst.i18n4vaadin.util.I18NHolder;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.StyleGenerator;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.TextRenderer;
import elemental.json.JsonValue;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import org.scraelos.esofurnituremp.Bundle;
import org.scraelos.esofurnituremp.model.ESO_SERVER;
import org.scraelos.esofurnituremp.model.KnownRecipe;
import org.scraelos.esofurnituremp.model.Recipe;
import org.scraelos.esofurnituremp.model.SysAccount;
import org.scraelos.esofurnituremp.model.tools.LuaDecoder;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@SpringView(name = ImportKnownRecipesView.NAME)
@Secured({"ROLE_USER"})
public class ImportKnownRecipesView extends CustomComponent implements View, LocaleChangedListener {

    public static final String NAME = "importknownplans";
    private static Pattern MEGASERVER_PATTERN = Pattern.compile("(EU|NA) Megaserver");
    private Header header;

    private ComboBox<ESO_SERVER> server;
    private Upload upload;
    private Grid<KnownRecipe> grid;
    private List<KnownRecipe> container;
    private Label noRecipes;

    @Autowired
    private DBService dBService;
    private Bundle i18n = new Bundle();
    private static final Logger LOG = Logger.getLogger(ImportKnownRecipesView.class.getName());

    public ImportKnownRecipesView() {
        header = new Header();
        this.setSizeFull();
        UploadHandler handler = new UploadHandler();
        server = new ComboBox<>(null, Arrays.asList(ESO_SERVER.values()));
        server.setEmptySelectionAllowed(false);
        server.setValue(SpringSecurityHelper.getUser().getEsoServer());
        upload = new Upload(null, handler);
        upload.addSucceededListener(handler);
        HorizontalLayout hl = new HorizontalLayout(server, upload);
        hl.setSizeUndefined();
        grid = new Grid<>();
        grid.setSizeFull();
        container = new CopyOnWriteArrayList<>();
        grid.setDataProvider(new ListDataProvider<>(container));
        grid.addColumn(KnownRecipe::getRecipe).setId("recipe").setWidth(450).setRenderer(new TextRenderer() {
            @Override
            public JsonValue encode(Object val) {
                Recipe value = (Recipe) val;
                String result = null;
                Boolean useEnglishNames = (Boolean) getUI().getSession().getAttribute("useEnglishNames");
                if (useEnglishNames == null || !useEnglishNames) {
                    switch (getLocale().getLanguage()) {
                        case "en":
                            result = value.getNameEn();
                            break;
                        case "de":
                            result = value.getNameDe();
                            break;
                        case "fr":
                            result = value.getNameFr();
                            break;
                        case "ru":
                            result = value.getNameRu();
                            break;
                    }
                } else {
                    result = value.getNameEn();
                }
                return super.encode(result);
            }
        }).setStyleGenerator(new CustomCellStyleGenerator());
        grid.addColumn(KnownRecipe::getCharacterName).setId("characterName");
        noRecipes = new Label();
        Label efmpDumpLabel = new Label(i18n.efmpDumpInfo());
        Link link = new Link(i18n.efmpDumpDownload(), new ExternalResource("https://disk.yandex.ru/d/r3_uCOammdtg0Q"));
        link.setTargetName("_blank");
        VerticalLayout uploadLayout = new VerticalLayout(efmpDumpLabel, link, hl);
        uploadLayout.setMargin(false);
        VerticalLayout contentLayout = new VerticalLayout(uploadLayout, grid, noRecipes);
        contentLayout.setExpandRatio(grid, 1f);
        contentLayout.setExpandRatio(noRecipes, 1f);
        contentLayout.setMargin(false);
        contentLayout.setSizeFull();
        VerticalLayout vl = new VerticalLayout(header, contentLayout);
        noRecipes.setVisible(false);
        grid.setVisible(false);
        vl.setSizeFull();
        vl.setMargin(false);
        vl.setExpandRatio(contentLayout, 1f);
        setCompositionRoot(vl);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        getUI().getPage().setTitle(i18n.importDataFromCraftStoreButtonCaption() + " | " + i18n.siteTitle());
        header.build();
        localize();
    }

    private void localize() {
        server.setCaption(i18n.server());
        upload.setCaption(i18n.uploadCraftStoreFile());
        grid.setCaption(i18n.newKnownRecipes());
        noRecipes.setCaption(i18n.noNewKnownRecipes());
        grid.getColumn("recipe").setCaption(i18n.recipe()).setWidth(500);
        grid.getColumn("characterName").setCaption(i18n.characterName());
    }

    @Override
    public void localeChanged(LocaleChangedEvent lce) {
        localize();
    }

    @Override
    public void attach() {
        super.attach();
        I18NHolder.get().addLocaleChangedListener(this);
    }

    @Override
    public void detach() {
        I18NHolder.get().removeLocaleChangedListener(this);
        super.detach();
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
            SysAccount account = SpringSecurityHelper.getUser();
            ESO_SERVER esoserver = (ESO_SERVER) server.getValue();
            container.clear();
            byte[] toByteArray = baos.toByteArray();
            String text = new String(toByteArray);
            if (text.contains("EfmpDump_SavedVariables")) {
                String userId = "@" + SpringSecurityHelper.getUser().getEsoId();
                try {
                    JSONObject jsonFromLua = LuaDecoder.getJsonFromLua(text);

                    JSONObject accountOject = jsonFromLua.getJSONObject(userId);

                    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
                    taskExecutor.initialize();
                    taskExecutor.setCorePoolSize(4);
                    taskExecutor.setMaxPoolSize(4);
                    taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
                    for (String serverString : accountOject.keySet()) {
                        Matcher serverMatcher = MEGASERVER_PATTERN.matcher(serverString);
                        if (serverMatcher.find()) {
                            ESO_SERVER server = ESO_SERVER.valueOf(serverMatcher.group(1));
                            if (server != null) {
                                JSONObject serverObject = accountOject.getJSONObject(serverString);
                                for (String character : serverObject.keySet()) {
                                    JSONObject characterObject = serverObject.getJSONObject(character);
                                    for (String itemId : characterObject.keySet()) {
                                        taskExecutor.execute(new CheckItemTask(Long.valueOf(itemId), character, server, account));
                                    }
                                }
                            }
                        }
                    }
                    taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
                    taskExecutor.setAwaitTerminationSeconds(120);
                    taskExecutor.shutdown();
                    if (container.size() > 0) {
                        noRecipes.setVisible(false);
                        grid.setVisible(true);
                        grid.getDataProvider().refreshAll();
                        dBService.addKnownRecipes(container, (ESO_SERVER) server.getValue(), SpringSecurityHelper.getUser());
                    } else {
                        noRecipes.setVisible(true);
                        grid.setVisible(false);
                    }
                } catch (JSONException ex) {
                    LOG.log(Level.INFO, userId, ex);
                    Notification.show(i18n.uploadErrorTitle(), i18n.uploadErrorIDNotFound(userId), Notification.Type.ERROR_MESSAGE);
                }
            } else {
                Notification.show(i18n.uploadErrorTitle(), i18n.uploadErrorNotFurnitureCatalogueOrCraftStore(), Notification.Type.ERROR_MESSAGE);
            }

        }

    }

    private class CustomCellStyleGenerator implements StyleGenerator<KnownRecipe> {

        @Override
        public String apply(KnownRecipe item) {
            if (item.getRecipe().getItemQuality() != null) {
                return item.getRecipe().getItemQuality().name().toLowerCase();
            }
            return null;
        }

    }

    private class CheckItemTask implements Runnable {

        private final Long itemId;
        private final String characterName;
        private final ESO_SERVER esoserver;
        private final SysAccount account;

        public CheckItemTask(Long itemId, String characterName, ESO_SERVER server, SysAccount account) {
            this.itemId = itemId;
            this.characterName = characterName;
            this.esoserver = server;
            this.account = account;
        }

        @Override
        public void run() {
            Recipe recipe = dBService.getItemRecipe(Long.valueOf(itemId));
            if (recipe == null) {
                LOG.warning("recipe not found for item: " + itemId.toString());
            }
            if (recipe != null && !dBService.isRecipeKnown(recipe, characterName, esoserver, account)) {
                KnownRecipe item = new KnownRecipe();
                item.setCharacterName(characterName);
                item.setRecipe(recipe);
                container.add(item);
            }
        }

    }

}
