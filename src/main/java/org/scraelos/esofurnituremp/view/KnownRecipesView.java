/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.github.peholmst.i18n4vaadin.LocaleChangedEvent;
import com.github.peholmst.i18n4vaadin.LocaleChangedListener;
import com.github.peholmst.i18n4vaadin.util.I18NHolder;
import com.vaadin.data.HasValue;
import com.vaadin.data.TreeData;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.data.util.PropertyValueGenerator;
import com.vaadin.v7.data.util.converter.Converter;
import com.vaadin.v7.data.validator.BigDecimalRangeValidator;
import com.vaadin.v7.event.FieldEvents;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.v7.ui.AbstractTextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.v7.ui.Grid;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.ItemCaptionGenerator;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.Upload;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.datenhahn.vaadin.componentrenderer.ComponentRenderer;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.poi.util.IOUtils;
import org.scraelos.esofurnituremp.Bundle;
import org.scraelos.esofurnituremp.data.KnownRecipeRepository;
import org.scraelos.esofurnituremp.data.KnownRecipeSpecification;
import org.scraelos.esofurnituremp.model.FURNITURE_THEME;
import org.scraelos.esofurnituremp.model.FurnitureCategory;
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.scraelos.esofurnituremp.model.ITEM_QUALITY;
import org.scraelos.esofurnituremp.model.ItemCategory;
import org.scraelos.esofurnituremp.model.ItemScreenshot;
import org.scraelos.esofurnituremp.model.ItemScreenshotFull;
import org.scraelos.esofurnituremp.model.ItemSubCategory;
import org.scraelos.esofurnituremp.model.KnownRecipe;
import org.scraelos.esofurnituremp.model.RECIPE_TYPE;
import org.scraelos.esofurnituremp.model.Recipe;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import org.vaadin.liveimageeditor.LiveImageEditor;
import org.vaadin.viritin.v7.SortableLazyList;
import org.vaadin.viritin.v7.grid.GeneratedPropertyListContainer;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@SpringView(name = KnownRecipesView.NAME)
@Secured({"ROLE_USER"})
public class KnownRecipesView extends CustomComponent implements View, LocaleChangedListener {

    public static final String NAME = "knownrecipes";
    private Header header;
    @Autowired
    private DBService dBService;
    @Autowired
    private KnownRecipeRepository repo;
    private Bundle i18n = new Bundle();

    private Tree tree;
    private Grid grid;
    private HorizontalLayout actions;
    private HorizontalLayout filters;
    private HorizontalLayout textFilter;
    private ComboBox itemQuality;
    private com.vaadin.ui.ComboBox<FURNITURE_THEME> theme;
    private TextField searchField;
    private CheckBox searchFieldIgnoresOtherFilters;
    private String searchValue;
    private ItemSubCategory currentCategory;
    private ComboBox recipeType;
    private Button importButton;
    private Button massPriceSetupButton;
    private ScreenShotViewWindwow screenShotViewWindwow;

    private SortableLazyList<KnownRecipe> itemList;
    private GeneratedPropertyListContainer<KnownRecipe> listContainer = new GeneratedPropertyListContainer(KnownRecipe.class);
    private KnownRecipeSpecification specification;
    private ScreenshotClickListener screenshotClickListener;
    private DeleteImageClickListener deleteImageClickListener;
    static final int PAGESIZE = 20;

    public KnownRecipesView() {
        header = new Header();
        this.setSizeFull();
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        header = new Header();
        vl.addComponent(header);
        actions = new HorizontalLayout();
        importButton = new Button("", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getUI().getNavigator().navigateTo(ImportKnownRecipesView.NAME);
            }
        });
        actions.addComponent(importButton);
        massPriceSetupButton = new Button("", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                MassPricesSetupWindow window = new MassPricesSetupWindow();
                getUI().addWindow(window);
                window.localize();
            }
        });
        actions.addComponent(massPriceSetupButton);
        vl.addComponent(actions);
        filters = new HorizontalLayout();
        recipeType = new ComboBox(null, Arrays.asList(RECIPE_TYPE.values()));
        recipeType.setNullSelectionAllowed(true);
        recipeType.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                loadItems();
            }
        });
        filters.addComponent(recipeType);
        itemQuality = new ComboBox(null, Arrays.asList(ITEM_QUALITY.values()));
        itemQuality.setNullSelectionAllowed(true);
        itemQuality.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                loadItems();
            }
        });
        filters.addComponent(itemQuality);
        theme = new com.vaadin.ui.ComboBox(null, Arrays.asList(FURNITURE_THEME.values()));
        theme.setPageLength(25);
        theme.setEmptySelectionAllowed(true);
        theme.addValueChangeListener(new HasValue.ValueChangeListener<FURNITURE_THEME>() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent<FURNITURE_THEME> event) {
                loadItems();
            }
        });

        filters.addComponent(theme);
        vl.addComponent(filters);
        textFilter = new HorizontalLayout();
        searchField = new TextField();
        searchField.setWidth(300f, Unit.PIXELS);
        searchField.addTextChangeListener(new FieldEvents.TextChangeListener() {

            @Override
            public void textChange(FieldEvents.TextChangeEvent event) {
                searchValue = event.getText();
                loadItems();
            }
        });
        searchField.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.TIMEOUT);
        searchField.setTextChangeTimeout(2000);
        searchFieldIgnoresOtherFilters = new CheckBox(null, true);
        searchFieldIgnoresOtherFilters.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                loadItems();
            }
        });
        textFilter.addComponent(searchField);
        textFilter.addComponent(searchFieldIgnoresOtherFilters);
        textFilter.setComponentAlignment(searchFieldIgnoresOtherFilters, Alignment.BOTTOM_LEFT);
        vl.addComponent(textFilter);
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        tree = new Tree();
        tree.setSizeFull();
        tree.setWidth(250f, Unit.PIXELS);
        tree.setSelectionMode(com.vaadin.ui.Grid.SelectionMode.SINGLE);
        tree.addSelectionListener(new SelectionListener() {
            @Override
            public void selectionChange(SelectionEvent event) {
                loadItems();
            }
        });
        tree.addStyleName("v-scrollable");
        hl.addComponent(tree);
        grid = new Grid();
        grid.setStyleName("my-grid");
        grid.setSizeFull();
        grid.setCellStyleGenerator(new CustomCellStyleGenerator());
        hl.addComponent(grid);
        hl.setExpandRatio(grid, 1f);

        vl.addComponent(hl);
        vl.setExpandRatio(hl, 1f);
        setCompositionRoot(vl);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        header.build();
        screenshotClickListener = new ScreenshotClickListener();
        deleteImageClickListener = new DeleteImageClickListener();
        specification = new KnownRecipeSpecification(SpringSecurityHelper.getUser());
        List<FurnitureCategory> itemCategoriesList = dBService.getItemCategoriesList();
        TreeData treeData = new TreeData();
        treeData.addRootItems(itemCategoriesList);
        for (FurnitureCategory c : itemCategoriesList) {
            treeData.addItems(c, c.getChilds());
        }
        tree.setTreeData(treeData);
        listContainer.addGeneratedProperty("screenshots", new ScreenShotsColumnGenerator());
        listContainer.addGeneratedProperty("links", new ItemLinkCoumnGenerator());

        grid.setContainerDataSource(listContainer);
        grid.setColumns(new Object[]{"recipe", "links", "screenshots", "characterName", "esoServer", "craftPrice", "craftPriceWithMats"});
        grid.setEditorEnabled(true);
        grid.getEditorFieldGroup().addCommitHandler(new FieldGroup.CommitHandler() {

            @Override
            public void preCommit(FieldGroup.CommitEvent commitEvent) throws FieldGroup.CommitException {

            }

            @Override
            public void postCommit(FieldGroup.CommitEvent commitEvent) throws FieldGroup.CommitException {
                for (Object itemId : listContainer.getItemIds()) {
                    if (commitEvent.getFieldBinder().getItemDataSource().equals(listContainer.getItem(itemId))) {
                        KnownRecipe recipe = (KnownRecipe) itemId;
                        dBService.saveEntity(recipe);
                        refreshGridItem(itemId);
                    }
                }

            }
        });
        grid.getColumn("characterName").setEditable(false);
        grid.getColumn("esoServer").setEditable(false);
        grid.getColumn("craftPrice").setEditable(true);
        grid.getColumn("craftPriceWithMats").setEditable(true);
        grid.getColumn("links").setEditable(false).setRenderer(new ComponentRenderer());
        grid.getColumn("screenshots").setEditable(false).setRenderer(new ComponentRenderer()).setExpandRatio(1);
        grid.getColumn("recipe").setWidth(450).setEditable(false).setConverter(new Converter<String, Recipe>() {

            @Override
            public Recipe convertToModel(String value, Class<? extends Recipe> targetType, Locale locale) throws Converter.ConversionException {
                return null;
            }

            @Override
            public String convertToPresentation(Recipe value, Class<? extends String> targetType, Locale locale) throws Converter.ConversionException {
                String result = null;
                Boolean useEnglishNames = (Boolean) getUI().getSession().getAttribute("useEnglishNames");
                if (useEnglishNames == null || !useEnglishNames) {
                    switch (locale.getLanguage()) {
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
                return result;
            }

            @Override
            public Class<Recipe> getModelType() {
                return Recipe.class;
            }

            @Override
            public Class<String> getPresentationType() {
                return String.class;
            }

        });
        localize(getUI().getLocale());
        loadItems();

    }

    private void loadItems() {
        specification.setSearchString(searchValue);
        specification.setSearchStringIgnoresAll(searchFieldIgnoresOtherFilters.getValue());
        specification.setCategories(tree.getSelectedItems());
        if (recipeType.getValue() != null && (recipeType.getValue() instanceof RECIPE_TYPE)) {
            specification.setRecipeType((RECIPE_TYPE) recipeType.getValue());
        } else {
            specification.setRecipeType(null);
        }
        if (itemQuality.getValue() != null) {
            specification.setItemQuality((ITEM_QUALITY) itemQuality.getValue());
        } else {
            specification.setItemQuality(null);
        }
        if (theme.getValue() != null) {
            specification.setTheme(theme.getValue());
        } else {
            specification.setTheme(null);
        }
        itemList = new SortableLazyList<>((int firstRow, boolean sortAscending, String property) -> repo.findAll(specification, new PageRequest(
                firstRow / PAGESIZE,
                PAGESIZE,
                sortAscending ? Sort.Direction.ASC : Sort.Direction.DESC,
                property == null ? "id" : property
        )).getContent(),
                () -> (int) repo.count(specification),
                PAGESIZE);
        listContainer.setCollection(itemList);
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

    @Override
    public void localeChanged(LocaleChangedEvent lce) {
        localize(lce.getNewLocale());
        grid.refreshAllRows();
    }

    private void localize(Locale locale) {
        Boolean useEnglishNames = (Boolean) getUI().getSession().getAttribute("useEnglishNames");
        importButton.setCaption(i18n.importDataFromCraftStoreButtonCaption());
        massPriceSetupButton.setCaption(i18n.massPriceSetup());
        tree.setCaption(i18n.categories());
        tree.setItemCaptionGenerator(new ItemCaptionGenerator() {
            @Override
            public String apply(Object item) {

                FurnitureCategory value = (FurnitureCategory) item;
                String result = null;
                Boolean useEnglishNames = (Boolean) getUI().getSession().getAttribute("useEnglishNames");
                if (useEnglishNames == null || !useEnglishNames) {
                    switch (getLocale().getLanguage()) {
                        case "en":
                            result = value.getTextEn();
                            break;
                        case "de":
                            result = value.getTextDe();
                            break;
                        case "fr":
                            result = value.getTextFr();
                            break;
                        case "ru":
                            result = value.getTextRu();
                            break;
                    }
                } else {
                    result = value.getTextEn();
                }
                return result;
            }
        });
        grid.setCaption(i18n.knownRecipesTableCaption());
        recipeType.setCaption(i18n.recipeTypeCaption());
        for (RECIPE_TYPE t : RECIPE_TYPE.values()) {
            if (useEnglishNames == null || !useEnglishNames) {
                switch (locale.getLanguage()) {
                    case "en":
                        recipeType.setItemCaption(t, t.getNameEn());
                        break;
                    case "de":
                        recipeType.setItemCaption(t, t.getNameDe());
                        break;
                    case "fr":
                        recipeType.setItemCaption(t, t.getNameFr());
                        break;
                    case "ru":
                        recipeType.setItemCaption(t, t.getNameRu());
                        break;
                }
            } else {
                recipeType.setItemCaption(t, t.getNameEn());
            }
        }
        itemQuality.setCaption(i18n.itemQualityCaption());
        for (ITEM_QUALITY q : ITEM_QUALITY.values()) {
            if (useEnglishNames == null || !useEnglishNames) {
                switch (locale.getLanguage()) {
                    case "en":
                        itemQuality.setItemCaption(q, q.getNameEn());
                        break;
                    case "de":
                        itemQuality.setItemCaption(q, q.getNameDe());
                        break;
                    case "fr":
                        itemQuality.setItemCaption(q, q.getNameFr());
                        break;
                    case "ru":
                        itemQuality.setItemCaption(q, q.getNameRu());
                        break;
                }
            } else {
                itemQuality.setItemCaption(q, q.getNameEn());
            }
        }
        itemQuality.markAsDirtyRecursive();
        theme.setCaption(i18n.theme());
        theme.setItemCaptionGenerator(new ItemCaptionGenerator<FURNITURE_THEME>() {
            @Override
            public String apply(FURNITURE_THEME item) {
                Boolean useEnglishNames = (Boolean) getUI().getSession().getAttribute("useEnglishNames");
                if (useEnglishNames == null || !useEnglishNames) {
                    switch (getLocale().getLanguage()) {
                        case "en":
                            return item.getNameEn();
                        case "de":
                            return item.getNameDe();
                        case "fr":
                            return item.getNameFr();
                        case "ru":
                            return item.getNameRu();
                    }
                } else {
                    return item.getNameEn();
                }
                return null;
            }
        });
        searchField.setCaption(i18n.searchField());
        searchFieldIgnoresOtherFilters.setCaption(i18n.searchFieldIgnoreFilters());
        grid.getColumn("recipe").setHeaderCaption(i18n.recipe());
        grid.getColumn("characterName").setHeaderCaption(i18n.characterName());
        grid.getColumn("esoServer").setHeaderCaption(i18n.server());
        grid.getColumn("screenshots").setHeaderCaption(i18n.screenshots());
        grid.getColumn("links").setHeaderCaption(i18n.itemLink());
        grid.getColumn("craftPrice").setHeaderCaption(i18n.craftPrice());
        grid.getColumn("craftPriceWithMats").setHeaderCaption(i18n.craftPriceWithMats());
        grid.setEditorCancelCaption(i18n.gridItemCancelCaption());
        grid.setEditorSaveCaption(i18n.gridItemSaveCaption());
    }

    public void refreshGridItem(Object itemId) {
        grid.refreshRows(itemId);
    }

    private class CustomCellStyleGenerator implements Grid.CellStyleGenerator {

        @Override
        public String getStyle(Grid.CellReference cell) {
            Object propertyId = cell.getPropertyId();
            if (propertyId != null && propertyId.equals("recipe")) {
                KnownRecipe recipe = (KnownRecipe) cell.getItemId();
                if (recipe.getRecipe().getItemQuality() != null) {
                    return recipe.getRecipe().getItemQuality().name().toLowerCase();
                }
            }

            return null;
        }

    }

    private class ItemLinkCoumnGenerator extends PropertyValueGenerator<VerticalLayout> {

        @Override
        public VerticalLayout getValue(Item item, Object itemId, Object propertyId) {
            VerticalLayout result = new VerticalLayout();
            final KnownRecipe knownRecipe = (KnownRecipe) itemId;
            if (knownRecipe.getRecipe().getFurnitureItem() != null) {
                TextField linkField = new TextField();
                linkField.setValue(knownRecipe.getRecipe().getItemLink());
                linkField.setReadOnly(true);
                String linkeId = "itemLink" + knownRecipe.getId();
                linkField.setId(linkeId);
                result.addComponent(linkField);
            }

            return result;
        }

        @Override
        public Class<VerticalLayout> getType() {
            return VerticalLayout.class;
        }

    }

    private class ScreenShotsColumnGenerator extends PropertyValueGenerator<HorizontalLayout> {

        @Override
        public HorizontalLayout getValue(Item item, Object itemId, Object propertyId) {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSizeFull();
            hl.setSpacing(true);
            KnownRecipe knownRecipe = (KnownRecipe) itemId;
            if (knownRecipe.getRecipe().getFurnitureItem() != null) {
                final FurnitureItem furnitureItem = knownRecipe.getRecipe().getFurnitureItem();
                int counter = 0;
                if (SpringSecurityHelper.hasRole("ROLE_UPLOAD_SCREENSHOTS")) {
                    Button uploadScreenShot = new Button(FontAwesome.UPLOAD);
                    uploadScreenShot.setData(furnitureItem);
                    uploadScreenShot.addClickListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            UploadScreenshotWindow window = new UploadScreenshotWindow(knownRecipe);
                            getUI().addWindow(window);

                        }
                    });
                    hl.addComponent(uploadScreenShot);

                }
                for (final ItemScreenshot s : furnitureItem.getItemScreenshots()) {
                    StreamResource.StreamSource streamSource = new StreamResource.StreamSource() {

                        @Override
                        public InputStream getStream() {
                            ByteArrayInputStream bais = new ByteArrayInputStream(s.getThumbnail());
                            return bais;
                        }
                    };

                    Image screenshotThumb = new Image(null, new StreamResource(streamSource, "thumb_" + s.getFileName()));
                    screenshotThumb.setSizeFull();
                    screenshotThumb.setData(s);
                    screenshotThumb.addClickListener(screenshotClickListener);
                    Panel screenshotThumbPanel = new Panel(screenshotThumb);
                    hl.addComponent(screenshotThumbPanel);
                    screenshotThumbPanel.setHeight(102f, Unit.PIXELS);
                    screenshotThumbPanel.setWidth(176f, Unit.PIXELS);
                    hl.setComponentAlignment(screenshotThumbPanel, Alignment.TOP_LEFT);
                    hl.setExpandRatio(screenshotThumbPanel, 1f);
                    counter++;
                    if (counter > 2) {
                        break;
                    }
                }
            }
            return hl;
        }

        @Override
        public Class<HorizontalLayout> getType() {
            return HorizontalLayout.class;
        }

    }

    private class UploadScreenshotWindow extends Window implements Upload.Receiver, Upload.SucceededListener, LiveImageEditor.ImageReceiver, Button.ClickListener {

        private final Upload upload;
        private final KnownRecipe item;
        private Panel editorPanel;
        private LiveImageEditor liveImageEditor;
        private Button saveImage;
        private ByteArrayOutputStream baos;
        private String filename;

        public UploadScreenshotWindow(KnownRecipe item_) {

            VerticalLayout vl = new VerticalLayout();
            this.item = item_;
            this.setModal(true);
            this.setResizable(false);
            this.setWidth(1300f, Unit.PIXELS);
            this.setHeight(900f, Unit.PIXELS);
            center();
            this.setCaption(i18n.uploadScreenshotWindowCaption());
            upload = new Upload(i18n.uploadScreenshotUploadCaption(), this);

            upload.addSucceededListener(this);
            upload.setImmediate(true);

            vl.addComponent(upload);
            editorPanel = new Panel();
            liveImageEditor = new LiveImageEditor(this);
            liveImageEditor.setWidth(1280f, Unit.PIXELS);
            liveImageEditor.setHeight(720f, Unit.PIXELS);
            editorPanel.setContent(liveImageEditor);
            vl.addComponent(editorPanel);
            this.addCloseListener(new Window.CloseListener() {

                @Override
                public void windowClose(Window.CloseEvent e) {
                    refreshGridItem(item);
                }
            });
            saveImage = new Button(i18n.uploadScreenshotSaveCaption(), this);
            saveImage.setEnabled(false);
            vl.addComponent(saveImage);

            this.setContent(vl);
        }

        @Override
        public OutputStream receiveUpload(String filename_, String mimeType) {
            baos = new ByteArrayOutputStream();
            filename = filename_;
            return baos;
        }

        @Override
        public void uploadSucceeded(Upload.SucceededEvent event) {

            liveImageEditor.setImage(baos.toByteArray());
            saveImage.setEnabled(true);

        }

        public byte[] resize(byte[] bytes, int scaledHeight) throws IOException {
            // reads input image
            InputStream is = new ByteArrayInputStream(bytes);
            BufferedImage inputImage = ImageIO.read(is);
            float originalHeight = inputImage.getHeight();
            float resizeRatio = originalHeight / scaledHeight;
            Float scaledWidth = inputImage.getWidth() / resizeRatio;

            // creates output image
            BufferedImage outputImage = new BufferedImage(scaledWidth.intValue(), scaledHeight, inputImage.getType());
            // scales the input image to the output image
            Graphics2D g2d = outputImage.createGraphics();
            g2d.drawImage(inputImage, 0, 0, scaledWidth.intValue(), scaledHeight, null);
            g2d.dispose();
            // writes to output file
            ByteArrayOutputStream thumbBaos = new ByteArrayOutputStream();
            ImageIO.write(outputImage, "jpg", thumbBaos);
            return thumbBaos.toByteArray();
        }

        @Override
        public void receiveImage(InputStream inputStream) {
            try {
                byte[] image = IOUtils.toByteArray(inputStream);
                ItemScreenshot screenshot = new ItemScreenshot();
                screenshot.setAuthor(SpringSecurityHelper.getUser());
                screenshot.setFurnitureItem(item.getRecipe().getFurnitureItem());
                screenshot.setFileName(filename);
                ItemScreenshotFull itemScreenshotFull = new ItemScreenshotFull();
                itemScreenshotFull.setScreenshot(image);
                itemScreenshotFull.setThumb(screenshot);
                screenshot.setFull(itemScreenshotFull);
                screenshot.setThumbnail(resize(image, 100));
                dBService.saveEntity(screenshot);
                if (item.getRecipe().getFurnitureItem().getItemScreenshots() == null) {
                    item.getRecipe().getFurnitureItem().setItemScreenshots(new ArrayList<>());
                }
                item.getRecipe().getFurnitureItem().getItemScreenshots().add(screenshot);
                dBService.saveEntity(item.getRecipe().getFurnitureItem());
                refreshGridItem(item);
                this.close();
            } catch (IOException ex) {
                Logger.getLogger(FurnitureItemsView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void buttonClick(Button.ClickEvent event) {
            liveImageEditor.requestEditedImage();
            saveImage.setEnabled(false);
        }

    }

    private class ScreenshotClickListener implements MouseEvents.ClickListener {

        @Override
        public void click(MouseEvents.ClickEvent event) {
            Image i = (Image) event.getComponent();
            final ItemScreenshot sc = (ItemScreenshot) i.getData();
            screenShotViewWindwow = new ScreenShotViewWindwow(sc);
            getUI().addWindow(screenShotViewWindwow);
        }

    }

    private class ScreenShotViewWindwow extends Window {

        private final ItemScreenshot screenshot;

        private VerticalLayout vl;
        private Panel panel;
        private HorizontalLayout thumbs;
        private Panel thumbsPanel;

        public ScreenShotViewWindwow(ItemScreenshot screenshot_) {
            setWidth(1050f, Unit.PIXELS);
            setHeight(800f, Unit.PIXELS);
            setClosable(true);
            setModal(true);
            setDraggable(false);
            setResizable(false);
            center();
            this.screenshot = screenshot_;
            vl = new VerticalLayout();
            vl.setSizeFull();

            panel = new Panel();
            panel.setSizeFull();
            vl.addComponent(panel);
            vl.setExpandRatio(panel, 1f);

            thumbsPanel = new Panel();
            thumbs = new HorizontalLayout();
            thumbs.addStyleName("v-scrollable");

            this.setContent(vl);
            for (final ItemScreenshot s : screenshot.getFurnitureItem().getItemScreenshots()) {
                StreamResource.StreamSource streamSource = new StreamResource.StreamSource() {

                    @Override
                    public InputStream getStream() {
                        ByteArrayInputStream bais = new ByteArrayInputStream(s.getThumbnail());
                        return bais;
                    }
                };

                Panel imagePanel = new Panel();
                imagePanel.setHeight(100f, Unit.PIXELS);
                Image screenshotThumb = new Image(null, new StreamResource(streamSource, "thumb_" + s.getFileName()));
                screenshotThumb.setSizeFull();
                screenshotThumb.setData(s);
                screenshotThumb.addClickListener(new MouseEvents.ClickListener() {

                    @Override
                    public void click(MouseEvents.ClickEvent event) {
                        Image i = (Image) event.getComponent();
                        final ItemScreenshot sc = (ItemScreenshot) i.getData();
                        renderScreenshot(sc);
                    }
                });
                imagePanel.setContent(screenshotThumb);
                thumbs.addComponent(imagePanel);
                thumbs.setSpacing(true);

            }
            thumbsPanel.setSizeFull();
            thumbsPanel.setHeight(120f, Unit.PIXELS);
            thumbsPanel.setContent(thumbs);
            vl.addComponent(thumbsPanel);
            vl.setComponentAlignment(thumbsPanel, Alignment.BOTTOM_CENTER);
            renderScreenshot(screenshot);

        }

        private void renderScreenshot(final ItemScreenshot s) {
            StreamResource.StreamSource screenshotStreamSource = new StreamResource.StreamSource() {

                @Override
                public InputStream getStream() {
                    ByteArrayInputStream bais = new ByteArrayInputStream(s.getFull().getScreenshot());
                    return bais;
                }
            };
            Image screenshotImage = new Image(null, new StreamResource(screenshotStreamSource, s.getFileName()));
            screenshotImage.setWidth(1048f, Unit.PIXELS);
            screenshotImage.setAlternateText(i18n.screenshotAlternativeText(s.getFileName(), s.getAuthor().getEsoId()));
            VerticalLayout imageLayout = new VerticalLayout();
            imageLayout.setSizeFull();
            imageLayout.addComponent(screenshotImage);
            if (SpringSecurityHelper.getUser() != null && SpringSecurityHelper.hasRole("ROLE_UPLOAD_SCREENSHOTS") && SpringSecurityHelper.getUser().equals(s.getAuthor())) {
                Button deleteButton = new Button();
                deleteButton.addClickListener(deleteImageClickListener);
                deleteButton.setData(s);
                deleteButton.setIcon(FontAwesome.RECYCLE);
                imageLayout.addComponent(deleteButton);
                imageLayout.setComponentAlignment(deleteButton, Alignment.BOTTOM_CENTER);
            }

            panel.setContent(imageLayout);
        }

    }

    private class DeleteImageClickListener implements Button.ClickListener {

        @Override
        public void buttonClick(Button.ClickEvent event) {
            ItemScreenshot itemScreenshot = (ItemScreenshot) event.getButton().getData();
            FurnitureItem furnitureItem = itemScreenshot.getFurnitureItem();
            furnitureItem.getItemScreenshots().remove(itemScreenshot);
            dBService.deleteScreenShot(itemScreenshot);
            for (Object itemId : listContainer.getItemIds()) {
                KnownRecipe r = (KnownRecipe) itemId;
                if (r.getRecipe().getFurnitureItem() != null && r.getRecipe().getFurnitureItem().equals(furnitureItem)) {
                    refreshGridItem(r);
                    break;
                }
            }
            screenShotViewWindwow.close();
        }

    }

    private class MassPricesSetupWindow extends Window {

        private ComboBox itemQualityBox;
        private CheckBox isPriceNull;
        private CheckBox isPriceWithMatsNull;
        private TextField priceField;
        private TextField priceWithMatsField;
        private Button setupPriceButton;

        public MassPricesSetupWindow() {
            this.setModal(true);
            this.setResizable(false);
            this.setWidth(1400f, Unit.PIXELS);
            this.setHeight(70, Unit.PIXELS);
            center();
            HorizontalLayout hl = new HorizontalLayout();
            hl.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);
            itemQualityBox = new ComboBox(i18n.itemQualityCaption(), Arrays.asList(ITEM_QUALITY.values()));
            itemQualityBox.setNullSelectionAllowed(false);
            itemQualityBox.setCaption(i18n.itemQualityCaption());

            hl.addComponent(itemQualityBox);
            isPriceNull = new CheckBox(i18n.massPriceSetupNullCraftPrice(), true);
            hl.addComponent(isPriceNull);
            priceField = new TextField(i18n.craftPrice());
            priceField.setNullRepresentation("");
            priceField.addValidator(new BigDecimalRangeValidator(i18n.wrongPriceMessage(), BigDecimal.ONE, BigDecimal.valueOf(1000000000L).setScale(2, RoundingMode.HALF_UP)));
            hl.addComponent(priceField);
            isPriceWithMatsNull = new CheckBox(i18n.massPriceSetupNullCraftPriceWithMats(), true);
            hl.addComponent(isPriceWithMatsNull);
            priceWithMatsField = new TextField(i18n.craftPriceWithMats());
            priceWithMatsField.setNullRepresentation("");
            priceWithMatsField.addValidator(new BigDecimalRangeValidator(i18n.wrongPriceMessage(), BigDecimal.ONE, BigDecimal.valueOf(1000000000L).setScale(0, RoundingMode.HALF_UP)));
            hl.addComponent(priceWithMatsField);
            setupPriceButton = new Button(i18n.applyPricesButton(), new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    if (itemQualityBox.isValid() && priceField.isValid() && priceWithMatsField.isValid()) {
                        dBService.applyPrices(SpringSecurityHelper.getUser(), (ITEM_QUALITY) itemQualityBox.getValue(), (BigDecimal) priceField.getConvertedValue(), (BigDecimal) priceWithMatsField.getConvertedValue(), isPriceNull.getValue(), isPriceWithMatsNull.getValue());
                        loadItems();
                    }
                }
            });
            hl.addComponent(setupPriceButton);
            this.setContent(hl);

        }

        public void localize() {
            priceField.setConverter(new Converter<String, BigDecimal>() {

                @Override
                public BigDecimal convertToModel(String value, Class<? extends BigDecimal> targetType, Locale locale) throws Converter.ConversionException {
                    if (value == null) {
                        return null;
                    }
                    try {
                        Number parse = NumberFormat.getInstance(locale).parse(value);
                        BigDecimal result = BigDecimal.valueOf(parse.doubleValue()).setScale(0, RoundingMode.HALF_UP);
                        return result;
                    } catch (ParseException ex) {
                        return null;
                    }
                }

                @Override
                public String convertToPresentation(BigDecimal value, Class<? extends String> targetType, Locale locale) throws Converter.ConversionException {
                    if (value != null) {
                        return NumberFormat.getInstance(locale).format(value.doubleValue());
                    } else {
                        return "";
                    }
                }

                @Override
                public Class<BigDecimal> getModelType() {
                    return BigDecimal.class;
                }

                @Override
                public Class<String> getPresentationType() {
                    return String.class;
                }
            });
            priceWithMatsField.setConverter(new Converter<String, BigDecimal>() {

                @Override
                public BigDecimal convertToModel(String value, Class<? extends BigDecimal> targetType, Locale locale) throws Converter.ConversionException {
                    if (value == null) {
                        return null;
                    }
                    try {
                        Number parse = NumberFormat.getInstance(locale).parse(value);
                        BigDecimal result = BigDecimal.valueOf(parse.doubleValue()).setScale(0, RoundingMode.HALF_UP);
                        return result;
                    } catch (ParseException ex) {
                        return null;
                    }
                }

                @Override
                public String convertToPresentation(BigDecimal value, Class<? extends String> targetType, Locale locale) throws Converter.ConversionException {
                    if (value != null) {
                        return NumberFormat.getInstance(locale).format(value.doubleValue());
                    } else {
                        return "";
                    }
                }

                @Override
                public Class<BigDecimal> getModelType() {
                    return BigDecimal.class;
                }

                @Override
                public Class<String> getPresentationType() {
                    return String.class;
                }
            });
            Boolean useEnglishNames = (Boolean) getUI().getSession().getAttribute("useEnglishNames");
            for (ITEM_QUALITY q : ITEM_QUALITY.values()) {
                if (useEnglishNames == null || !useEnglishNames) {
                    switch (getUI().getLocale().getLanguage()) {
                        case "en":
                            itemQualityBox.setItemCaption(q, q.getNameEn());
                            break;
                        case "de":
                            itemQualityBox.setItemCaption(q, q.getNameDe());
                            break;
                        case "fr":
                            itemQualityBox.setItemCaption(q, q.getNameFr());
                            break;
                        case "ru":
                            itemQualityBox.setItemCaption(q, q.getNameRu());
                            break;
                    }
                } else {
                    itemQualityBox.setItemCaption(q, q.getNameEn());
                }
            }
        }

    }

}
