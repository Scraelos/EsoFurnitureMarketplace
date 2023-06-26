/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.github.peholmst.i18n4vaadin.LocaleChangedEvent;
import com.github.peholmst.i18n4vaadin.LocaleChangedListener;
import com.github.peholmst.i18n4vaadin.util.I18NHolder;
import com.vaadin.data.Binder;
import com.vaadin.data.HasValue;
import com.vaadin.data.TreeData;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.converter.StringToBigDecimalConverter;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.ItemCaptionGenerator;
import com.vaadin.ui.Panel;
import com.vaadin.ui.StyleGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.grid.EditorSaveEvent;
import com.vaadin.ui.components.grid.EditorSaveListener;
import com.vaadin.ui.renderers.TextRenderer;
import elemental.json.JsonValue;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import org.vaadin.artur.spring.dataprovider.PageableDataProvider;
import org.vaadin.liveimageeditor.LiveImageEditor;

/**
 *
 * @author scraelos
 */
@Component
//@Scope("prototype")
@SpringView(name = KnownRecipesView.NAME)
@Secured({"ROLE_USER"})
public class KnownRecipesView extends CustomComponent implements View, LocaleChangedListener {

    public static final String NAME = "knownplans";
    @Autowired
    private DBService dBService;
    @Autowired
    private KnownRecipeRepository repo;
    private Bundle i18n = new Bundle();

    private Tree tree;
    private Grid<KnownRecipe> grid;
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

    private KnownRecipeSpecification specification;
    private ScreenshotClickListener screenshotClickListener;
    private DeleteImageClickListener deleteImageClickListener;
    static final int PAGESIZE = 20;

    public KnownRecipesView() {
        this.setSizeFull();
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(false);
        vl.setSpacing(false);
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
        recipeType.setEmptySelectionAllowed(true);
        recipeType.addValueChangeListener(new HasValue.ValueChangeListener() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent event) {
                loadItems();
            }
        });
        filters.addComponent(recipeType);
        itemQuality = new ComboBox(null, Arrays.asList(ITEM_QUALITY.values()));
        itemQuality.setEmptySelectionAllowed(true);
        itemQuality.addValueChangeListener(new HasValue.ValueChangeListener() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent event) {
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

        searchField.addValueChangeListener(new HasValue.ValueChangeListener<String>() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent<String> event) {

                searchValue = event.getValue();
                loadItems();
            }
        });
        searchFieldIgnoresOtherFilters = new CheckBox(null, true);
        searchFieldIgnoresOtherFilters.addValueChangeListener(new HasValue.ValueChangeListener() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent event) {
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
        grid = new Grid<>(KnownRecipe.class);
        grid.setSizeFull();
        grid.setBodyRowHeight(80);
        hl.addComponent(grid);
        hl.setExpandRatio(grid, 1f);

        vl.addComponent(hl);
        vl.setExpandRatio(hl, 1f);
        setCompositionRoot(vl);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        getUI().getPage().setTitle(i18n.knownRecipesMenuItemCaption() + " | " + i18n.siteTitle());
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
        PageableDataProvider dataProvider = new PageableDataProvider() {
            @Override
            protected Page fetchFromBackEnd(Query query, Pageable pgbl) {
                return repo.findAll(specification, pgbl);
            }

            @Override
            protected List getDefaultSortOrders() {
                return QuerySortOrder.asc("id").build();
            }

            @Override
            protected int sizeInBackEnd(Query query) {
                return (int) repo.count(specification);
            }
        };
        grid.setDataProvider(dataProvider);
        grid.getEditor().setEnabled(true);
        grid.getEditor().addSaveListener(new EditorSaveListener<KnownRecipe>() {
            @Override
            public void onEditorSave(EditorSaveEvent<KnownRecipe> event) {
                KnownRecipe recipe = event.getBean();
                dBService.saveEntity(recipe);
                refreshGridItem(recipe);
            }
        });
        TextField craftPrice = new TextField();
        Binder.Binding craftPriceBind = grid.getEditor().getBinder().forField(craftPrice).withNullRepresentation("").withConverter(new StringToBigDecimalConverter("")).bind("craftPrice");
        grid.getColumn("craftPrice").setRenderer(new TextRenderer("") {
            @Override
            public JsonValue encode(Object value) {
                if (value != null) {
                    NumberFormat nf = NumberFormat.getInstance(getLocale());
                    nf.setMaximumFractionDigits(0);
                    return super.encode(nf.format(((BigDecimal) value)));
                } else {
                    return super.encode(i18n.nullPrice());
                }
            }
        }).setEditorBinding(craftPriceBind).setEditable(true);
        TextField craftPriceWithMats = new TextField();
        Binder.Binding craftPriceWithMatsBind = grid.getEditor().getBinder().forField(craftPriceWithMats).withNullRepresentation("").withConverter(new StringToBigDecimalConverter("")).bind("craftPriceWithMats");
        grid.getColumn("craftPriceWithMats").setRenderer(new TextRenderer("") {
            @Override
            public JsonValue encode(Object value) {
                if (value != null) {
                    NumberFormat nf = NumberFormat.getInstance(getLocale());
                    nf.setMaximumFractionDigits(0);
                    return super.encode(nf.format(((BigDecimal) value)));
                } else {
                    return super.encode(i18n.nullPrice());
                }
            }
        }).setEditorBinding(craftPriceWithMatsBind).setEditable(true);
        grid.getColumn("craftPriceWithMats").setEditable(true);
        grid.addComponentColumn(new ItemLinkCoumnGenerator()).setId("links");
        grid.addComponentColumn(new ScreenShotsColumnGenerator()).setId("screenshots").setExpandRatio(1);
        grid.getColumn("recipe").setWidth(450).setRenderer(new TextRenderer() {
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
        grid.setColumns("recipe", "links", "screenshots", "characterName", "esoServer", "craftPrice", "craftPriceWithMats");
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
        grid.getDataProvider().refreshAll();
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
        grid.getDataProvider().refreshAll();
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
        recipeType.setItemCaptionGenerator(new ItemCaptionGenerator<RECIPE_TYPE>() {
            @Override
            public String apply(RECIPE_TYPE item) {
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
        itemQuality.setCaption(i18n.itemQualityCaption());
        itemQuality.setItemCaptionGenerator(new ItemCaptionGenerator<ITEM_QUALITY>() {
            @Override
            public String apply(ITEM_QUALITY item) {
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
        grid.getColumn("recipe").setCaption(i18n.recipe());
        grid.getColumn("characterName").setCaption(i18n.characterName());
        grid.getColumn("esoServer").setCaption(i18n.server());
        grid.getColumn("screenshots").setCaption(i18n.screenshots());
        grid.getColumn("links").setCaption(i18n.itemLink());
        grid.getColumn("craftPrice").setCaption(i18n.craftPrice());
        grid.getColumn("craftPriceWithMats").setCaption(i18n.craftPriceWithMats());
        grid.getEditor().setCancelCaption(i18n.gridItemCancelCaption());
        grid.getEditor().setSaveCaption(i18n.gridItemSaveCaption());
    }

    public void refreshGridItem(KnownRecipe itemId) {
        grid.getDataProvider().refreshItem(itemId);
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

    private class ItemLinkCoumnGenerator implements ValueProvider<KnownRecipe, VerticalLayout> {

        @Override
        public VerticalLayout apply(KnownRecipe source) {
            VerticalLayout result = new VerticalLayout();
            result.setMargin(false);
            result.setSpacing(false);

            if (source.getRecipe().getFurnitureItem() != null) {
                TextField linkField = new TextField();
                linkField.setValue(source.getRecipe().getItemLink());
                linkField.setReadOnly(true);
                String linkeId = "itemLink" + source.getId();
                linkField.setId(linkeId);
                result.addComponent(linkField);
            }

            return result;
        }

    }

    private class ScreenShotsColumnGenerator implements ValueProvider<KnownRecipe, HorizontalLayout> {

        @Override
        public HorizontalLayout apply(KnownRecipe source) {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSizeFull();
            hl.setSpacing(true);
            KnownRecipe knownRecipe = source;
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
            vl.setMargin(false);
            vl.setSpacing(false);
            this.item = item_;
            this.setModal(true);
            this.setResizable(false);
            this.setWidth(1300f, Unit.PIXELS);
            this.setHeight(900f, Unit.PIXELS);
            center();
            this.setCaption(i18n.uploadScreenshotWindowCaption());
            upload = new Upload(i18n.uploadScreenshotUploadCaption(), this);
            upload.addSucceededListener(this);
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
            vl.setMargin(false);
            vl.setSpacing(false);
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
            imageLayout.setMargin(false);
            imageLayout.setSpacing(false);
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
            grid.getDataProvider().refreshAll();
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
            itemQualityBox.setEmptySelectionAllowed(false);
            itemQualityBox.setCaption(i18n.itemQualityCaption());

            hl.addComponent(itemQualityBox);
            isPriceNull = new CheckBox(i18n.massPriceSetupNullCraftPrice(), true);
            hl.addComponent(isPriceNull);
            priceField = new TextField(i18n.craftPrice());
            hl.addComponent(priceField);
            isPriceWithMatsNull = new CheckBox(i18n.massPriceSetupNullCraftPriceWithMats(), true);
            hl.addComponent(isPriceWithMatsNull);
            priceWithMatsField = new TextField(i18n.craftPriceWithMats());
            hl.addComponent(priceWithMatsField);
            setupPriceButton = new Button(i18n.applyPricesButton(), new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    if (itemQualityBox.getValue() != null) {
                        BigDecimal price = null;
                        if (!priceField.getValue().isEmpty()) {
                            price = new BigDecimal(priceField.getValue());
                        }
                        BigDecimal priceWithMats = null;
                        if (!priceWithMatsField.getValue().isEmpty()) {
                            priceWithMats = new BigDecimal(priceWithMatsField.getValue());
                        }
                        dBService.applyPrices(SpringSecurityHelper.getUser(), (ITEM_QUALITY) itemQualityBox.getValue(), price, priceWithMats, isPriceNull.getValue(), isPriceWithMatsNull.getValue());
                        loadItems();
                    }
                }
            });
            hl.addComponent(setupPriceButton);
            this.setContent(hl);

        }

        public void localize() {
            Boolean useEnglishNames = (Boolean) getUI().getSession().getAttribute("useEnglishNames");
            itemQualityBox.setItemCaptionGenerator(new ItemCaptionGenerator<ITEM_QUALITY>() {
                @Override
                public String apply(ITEM_QUALITY item) {
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
        }

    }

}
