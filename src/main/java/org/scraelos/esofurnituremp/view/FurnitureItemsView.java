/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.github.peholmst.i18n4vaadin.LocaleChangedEvent;
import com.github.peholmst.i18n4vaadin.LocaleChangedListener;
import com.github.peholmst.i18n4vaadin.util.I18NHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
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
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.poi.util.IOUtils;
import org.scraelos.esofurnituremp.Bundle;
import org.scraelos.esofurnituremp.data.FurnitureItemRepository;
import org.scraelos.esofurnituremp.data.FurnitureItemSpecification;
import org.scraelos.esofurnituremp.model.ESO_SERVER;
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.scraelos.esofurnituremp.model.ITEM_QUALITY;
import org.scraelos.esofurnituremp.model.ItemCategory;
import org.scraelos.esofurnituremp.model.ItemScreenshot;
import org.scraelos.esofurnituremp.model.ItemSubCategory;
import org.scraelos.esofurnituremp.model.KnownRecipe;
import org.scraelos.esofurnituremp.model.RecipeIngredient;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.vaadin.liveimageeditor.LiveImageEditor;
import org.vaadin.viritin.SortableLazyList;
import org.vaadin.viritin.grid.GeneratedPropertyListContainer;
import ru.xpoft.vaadin.VaadinView;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@VaadinView(FurnitureItemsView.NAME)
public class FurnitureItemsView extends CustomComponent implements View, LocaleChangedListener {

    public static final String NAME = "furniture";

    private Header header;
    @Autowired
    private DBService dBService;
    private Bundle i18n = new Bundle();

    @Autowired
    FurnitureItemRepository repo;

    private Tree tree;
    private Grid grid;
    private GeneratedPropertyListContainer<FurnitureItem> listContainer;
    private Grid craftersGrid;

    private SortableLazyList<FurnitureItem> furnitureList;

    private GeneratedPropertyListContainer craftersContainer;

    private HorizontalLayout filters;
    private CheckBox onlyCraftable;
    private CheckBox hasCrafters;
    private ComboBox itemQuality;
    private ComboBox server;
    private TextField searchField;
    private CheckBox searchFieldIgnoresOtherFilters;
    private ItemSubCategory currentCategory;
    private String searchValue;
    private ScreenshotClickListener screenshotClickListener;
    private DeleteImageClickListener deleteImageClickListener;

    private ScreenShotViewWindwow screenShotViewWindwow;

    private FurnitureItemSpecification specification;

    static final int PAGESIZE = 20;

    private String itemNameColumn;

    private VerticalLayout itemInfoLayout;
    private Label itemNameLabel;

    private Grid materialsGrid;
    private GeneratedPropertyListContainer materialsContainer;

    FurnitureItem selectedFurnitureItem;

    public FurnitureItemsView() {
        this.setSizeFull();
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        header = new Header();
        specification = new FurnitureItemSpecification();
        vl.addComponent(header);
        filters = new HorizontalLayout();
        server = new ComboBox(null, Arrays.asList(ESO_SERVER.values()));
        server.setNullSelectionAllowed(false);
        server.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                loadItems();
            }
        });

        filters.addComponent(server);
        itemQuality = new ComboBox(null, Arrays.asList(ITEM_QUALITY.values()));
        itemQuality.setNullSelectionAllowed(true);

        filters.addComponent(itemQuality);

        onlyCraftable = new CheckBox(null, false);
        onlyCraftable.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                loadItems();
            }
        });
        filters.addComponent(onlyCraftable);
        filters.setComponentAlignment(onlyCraftable, Alignment.BOTTOM_LEFT);
        hasCrafters = new CheckBox(null, false);
        hasCrafters.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                loadItems();
            }
        });
        filters.addComponent(hasCrafters);
        filters.setComponentAlignment(hasCrafters, Alignment.BOTTOM_LEFT);
        filters.setSpacing(true);
        vl.addComponent(filters);
        HorizontalLayout textfilter = new HorizontalLayout();
        textfilter.setSpacing(true);
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
        textfilter.addComponent(searchField);
        textfilter.addComponent(searchFieldIgnoresOtherFilters);
        textfilter.setComponentAlignment(searchFieldIgnoresOtherFilters, Alignment.BOTTOM_LEFT);

        vl.addComponent(textfilter);
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        tree = new Tree(null);
        tree.setSizeFull();
        tree.setWidth(200f, Unit.PIXELS);
        tree.addItemClickListener(new TreeItemClickListener());
        tree.addExpandListener(new Tree.ExpandListener() {

            @Override
            public void nodeExpand(Tree.ExpandEvent event) {
                Object expandedItemId = event.getItemId();
                for (Object itemId : tree.getItemIds()) {
                    if (!itemId.equals(expandedItemId)) {
                        tree.collapseItem(itemId);
                    }
                }
            }
        });
        hl.addComponent(tree);
        grid = new Grid();
        grid.setStyleName("my-grid");
        grid.setSizeFull();
        grid.addItemClickListener(new ItemClickEvent.ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                selectedFurnitureItem = (FurnitureItem) event.getItemId();
                Boolean useEnglishNames = (Boolean) getUI().getSession().getAttribute("useEnglishNames");
                if (useEnglishNames == null || !useEnglishNames) {
                    itemNameLabel.setCaption(selectedFurnitureItem.getLocalizedName(getUI().getLocale()));
                } else {
                    itemNameLabel.setCaption(selectedFurnitureItem.getNameEn());
                }

                for (ITEM_QUALITY q : ITEM_QUALITY.values()) {
                    itemNameLabel.removeStyleName(q.name().toLowerCase());
                }
                if (selectedFurnitureItem.getItemQuality() != null) {
                    itemNameLabel.addStyleName(selectedFurnitureItem.getItemQuality().name().toLowerCase());
                }

                itemInfoLayout.setVisible(true);
                if (selectedFurnitureItem.getRecipe() != null) {
                    materialsContainer.setCollection(selectedFurnitureItem.getRecipe().getRecipeIngredients());
                    materialsGrid.setVisible(true);
                    craftersContainer.setCollection(dBService.getCrafters(selectedFurnitureItem.getRecipe(), (ESO_SERVER) server.getValue()));
                    craftersGrid.setVisible(true);
                } else {
                    materialsGrid.setVisible(false);
                    craftersGrid.setVisible(false);
                }
            }
        });
        listContainer = new GeneratedPropertyListContainer<>(FurnitureItem.class);
        hl.addComponent(grid);
        hl.setExpandRatio(grid, 1f);
        itemInfoLayout = new VerticalLayout();
        itemInfoLayout.setSizeFull();
        itemNameLabel = new Label();
        itemInfoLayout.addComponent(itemNameLabel);
        materialsGrid = new Grid();
        materialsGrid.setSizeFull();
        materialsContainer = new GeneratedPropertyListContainer(RecipeIngredient.class);
        materialsGrid.setContainerDataSource(materialsContainer);
        materialsGrid.setColumns("ingredient", "count");
        itemInfoLayout.addComponent(materialsGrid);

        craftersGrid = new Grid();
        craftersGrid.setSizeFull();
        craftersContainer = new GeneratedPropertyListContainer(KnownRecipe.class);
        craftersContainer.addGeneratedProperty("esoId", new PropertyValueGenerator() {

            @Override
            public Object getValue(Item item, Object itemId, Object propertyId) {
                String result = "@" + ((KnownRecipe) itemId).getAccount().getEsoId();
                return result;
            }

            @Override
            public Class getType() {
                return String.class;
            }
        }
        );
        craftersGrid.setContainerDataSource(craftersContainer);
        craftersGrid.setColumns(new Object[]{"esoId", "craftPrice", "craftPriceWithMats"});
        craftersGrid.getColumn("craftPrice").setConverter(new Converter<String, BigDecimal>() {

            @Override
            public BigDecimal convertToModel(String value, Class<? extends BigDecimal> targetType, Locale locale) throws Converter.ConversionException {
                if (value != null) {
                    try {
                        Number parse = NumberFormat.getInstance(locale).parse(value);
                        BigDecimal result = BigDecimal.valueOf(parse.doubleValue()).setScale(0, RoundingMode.HALF_UP);
                        return result;
                    } catch (ParseException ex) {
                        return null;
                    }
                } else {
                    return null;
                }
            }

            @Override
            public String convertToPresentation(BigDecimal value, Class<? extends String> targetType, Locale locale) throws Converter.ConversionException {
                if (value != null) {
                    return NumberFormat.getInstance(locale).format(value.doubleValue());
                } else {
                    return i18n.nullPrice();
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
        craftersGrid.getColumn("craftPriceWithMats").setConverter(new Converter<String, BigDecimal>() {

            @Override
            public BigDecimal convertToModel(String value, Class<? extends BigDecimal> targetType, Locale locale) throws Converter.ConversionException {
                if (value != null) {
                    try {
                        Number parse = NumberFormat.getInstance(locale).parse(value);
                        BigDecimal result = BigDecimal.valueOf(parse.doubleValue()).setScale(0, RoundingMode.HALF_UP);
                        return result;
                    } catch (ParseException ex) {
                        return null;
                    }
                } else {
                    return null;
                }
            }

            @Override
            public String convertToPresentation(BigDecimal value, Class<? extends String> targetType, Locale locale) throws Converter.ConversionException {
                if (value != null) {
                    return NumberFormat.getInstance(locale).format(value.doubleValue());
                } else {
                    return i18n.nullPrice();
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
        craftersGrid.setVisible(false);
        itemInfoLayout.setVisible(false);
        materialsGrid.setVisible(false);
        itemInfoLayout.addComponent(craftersGrid);
        itemInfoLayout.setExpandRatio(materialsGrid, 0.5f);
        itemInfoLayout.setExpandRatio(craftersGrid, 1f);
        hl.addComponent(itemInfoLayout);
        hl.setExpandRatio(itemInfoLayout, 0.4f);
        vl.addComponent(hl);
        vl.setExpandRatio(hl, 1f);
        setCompositionRoot(vl);
    }

    private void loadItems() {
        specification.setCategory(currentCategory);
        specification.setOnlyCraftable(onlyCraftable.getValue());
        specification.setHasCrafters(hasCrafters.getValue());
        specification.setSearchString(searchValue);
        specification.setEsoServer((ESO_SERVER) server.getValue());
        if (itemQuality.getValue() != null) {
            specification.setItemQuality((ITEM_QUALITY) itemQuality.getValue());
        } else {
            specification.setItemQuality(null);
        }
        specification.setSearchStringIgnoresAll(searchFieldIgnoresOtherFilters.getValue());
        furnitureList = new SortableLazyList<>((int firstRow, boolean sortAscending, String property) -> repo.findAll(specification, new PageRequest(
                firstRow / PAGESIZE,
                PAGESIZE,
                sortAscending ? Sort.Direction.ASC : Sort.Direction.DESC,
                property == null ? "id" : property
        )).getContent(),
                () -> (int) repo.count(specification),
                PAGESIZE);
        listContainer.setCollection(furnitureList);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        header.build();

        screenshotClickListener = new ScreenshotClickListener();
        deleteImageClickListener = new DeleteImageClickListener();
        tree.setContainerDataSource(dBService.getItemCategories());
        grid.setCellStyleGenerator(new CustomCellStyleGenerator());

        listContainer.addGeneratedProperty("category", new PropertyValueGenerator<String>() {

            @Override
            public String getValue(Item item, Object itemId, Object propertyId) {
                FurnitureItem furnitureItem = (FurnitureItem) itemId;
                String result = furnitureItem.getSubCategory().getCategory().toString() + ", " + furnitureItem.getSubCategory().toString();
                return result;
            }

            @Override
            public Class<String> getType() {
                return String.class;
            }

        });
        listContainer.addGeneratedProperty("screenshots", new ScreenShotsColumnGenerator());
        listContainer.addGeneratedProperty("links", new ItemLinkCoumnGenerator());
        grid.setContainerDataSource(listContainer);
        grid.getColumn("links").setRenderer(new ComponentRenderer());
        grid.getColumn("screenshots").setRenderer(new ComponentRenderer()).setExpandRatio(1);
        if (SpringSecurityHelper.getUser() != null) {
            server.setValue(SpringSecurityHelper.getUser().getEsoServer());
        } else {
            server.setValue(ESO_SERVER.EU);
        }
        localize(getUI().getLocale());
        loadItems();
        itemQuality.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                loadItems();
            }
        });
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
        if (useEnglishNames == null || !useEnglishNames) {
            itemNameColumn = i18n.localizedItemNameColumn();
        } else {
            itemNameColumn = "nameEn";
        }
        craftersGrid.setCaption(i18n.craftersTableCaption());
        filters.setCaption(i18n.filters());
        server.setCaption(i18n.serverForCraftersSearch());
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
        onlyCraftable.setCaption(i18n.displayOnlyCraftable());
        hasCrafters.setCaption(i18n.hasCrafters());
        searchField.setCaption(i18n.searchField());
        searchFieldIgnoresOtherFilters.setCaption(i18n.searchFieldIgnoreFilters());
        tree.setCaption(i18n.categories());
        grid.setCaption(i18n.furnitureListItemTableCaption());
        grid.setColumns(new Object[]{itemNameColumn, "links", "screenshots", "category"});
        grid.getColumn(itemNameColumn).setWidth(400).setHeaderCaption(i18n.item());
        grid.getColumn("category").setHeaderCaption(i18n.category());
        grid.getColumn("links").setHeaderCaption(i18n.itemLink());
        grid.getColumn("screenshots").setHeaderCaption(i18n.screenshots());
        if (selectedFurnitureItem != null) {
            if (useEnglishNames == null || !useEnglishNames) {
                itemNameLabel.setCaption(selectedFurnitureItem.getLocalizedName(locale));
            } else {
                itemNameLabel.setCaption(selectedFurnitureItem.getNameEn());
            }
        }
        materialsGrid.setCaption(i18n.materials());
        craftersGrid.getColumn("craftPrice").setHeaderCaption(i18n.craftPrice());
        craftersGrid.getColumn("craftPriceWithMats").setHeaderCaption(i18n.craftPriceWithMats());

    }

    private class TreeItemClickListener implements ItemClickEvent.ItemClickListener {

        @Override
        public void itemClick(ItemClickEvent event) {
            Object itemId = event.getItemId();
            if (itemId instanceof ItemCategory) {
                tree.expandItem(itemId);
            } else if (itemId instanceof ItemSubCategory) {
                currentCategory = (ItemSubCategory) itemId;
                loadItems();
            }
        }

    }

    private class CustomCellStyleGenerator implements Grid.CellStyleGenerator {

        @Override
        public String getStyle(Grid.CellReference cell) {
            Object propertyId = cell.getPropertyId();
            if (propertyId != null && propertyId.equals(itemNameColumn)) {
                FurnitureItem furnitureItem = (FurnitureItem) cell.getItemId();
                return furnitureItem.getItemQuality().name().toLowerCase();
            }

            return null;
        }

    }

    private class ItemLinkCoumnGenerator extends PropertyValueGenerator<VerticalLayout> {

        @Override
        public VerticalLayout getValue(Item item, Object itemId, Object propertyId) {
            VerticalLayout result = new VerticalLayout();
            final FurnitureItem furnitureItem = (FurnitureItem) itemId;
            TextField linkField = new TextField();
            linkField.setValue(furnitureItem.getItemLink());
            linkField.setReadOnly(true);
            String linkeId = "itemLink" + furnitureItem.getId();
            linkField.setId(linkeId);
            result.addComponent(linkField);
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
            final FurnitureItem furnitureItem = (FurnitureItem) itemId;
            int counter = 0;
            if (SpringSecurityHelper.hasRole("ROLE_UPLOAD_SCREENSHOTS")) {
                Button uploadScreenShot = new Button(FontAwesome.UPLOAD);
                uploadScreenShot.setData(furnitureItem);
                uploadScreenShot.addClickListener(new Button.ClickListener() {

                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        UploadScreenshotWindow window = new UploadScreenshotWindow(furnitureItem);
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
                screenshotThumb.setImmediate(true);
                screenshotThumb.setData(s);
                screenshotThumb.addClickListener(screenshotClickListener);
                Panel screenshotThumbPanel = new Panel(screenshotThumb);
                hl.addComponent(screenshotThumbPanel);
                screenshotThumbPanel.setHeight(102f, Unit.PIXELS);
                screenshotThumbPanel.setWidth(176f, Unit.PIXELS);
                hl.setComponentAlignment(screenshotThumbPanel, Alignment.TOP_LEFT);
                hl.setExpandRatio(screenshotThumbPanel, 1f);
                counter++;
                if (counter > 1) {
                    break;
                }
            }

            return hl;
        }

        @Override
        public Class<HorizontalLayout> getType() {
            return HorizontalLayout.class;
        }

    }

    public void refreshFurnitureItem(FurnitureItem item) {
        try {
            grid.refreshRows(item);
        } catch (Exception ex) {

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
                screenshotThumb.setImmediate(true);
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
                    ByteArrayInputStream bais = new ByteArrayInputStream(s.getScreenshot());
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
            refreshFurnitureItem(furnitureItem);
            screenShotViewWindwow.close();
        }

    }

    private class UploadScreenshotWindow extends Window implements Upload.Receiver, Upload.SucceededListener, LiveImageEditor.ImageReceiver, Button.ClickListener {

        private final Upload upload;
        private final FurnitureItem item;
        private Panel editorPanel;
        private LiveImageEditor liveImageEditor;
        private Button saveImage;
        private ByteArrayOutputStream baos;
        private String filename;

        public UploadScreenshotWindow(FurnitureItem item_) {

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
            this.addCloseListener(new CloseListener() {

                @Override
                public void windowClose(CloseEvent e) {
                    refreshFurnitureItem(item);
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
                screenshot.setFurnitureItem(item);
                screenshot.setFileName(filename);
                screenshot.setScreenshot(image);
                screenshot.setThumbnail(resize(image, 100));
                if (item.getItemScreenshots() == null) {
                    item.setItemScreenshots(new ArrayList<>());
                }
                item.getItemScreenshots().add(screenshot);
                dBService.saveEntity(item);
                refreshFurnitureItem(item);
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

}
