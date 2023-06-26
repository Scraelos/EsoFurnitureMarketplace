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
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.MarginInfo;
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
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.StyleGenerator;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.TextRenderer;
import com.vaadin.ui.themes.ValoTheme;
import elemental.json.JsonValue;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.poi.util.IOUtils;
import org.scraelos.esofurnituremp.Bundle;
import org.scraelos.esofurnituremp.data.FurnitureItemRepository;
import org.scraelos.esofurnituremp.data.FurnitureItemSpecification;
import org.scraelos.esofurnituremp.model.ESO_SERVER;
import org.scraelos.esofurnituremp.model.FURNITURE_THEME;
import org.scraelos.esofurnituremp.model.FurnitureCategory;
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.scraelos.esofurnituremp.model.ITEM_QUALITY;
import org.scraelos.esofurnituremp.model.Ingredient;
import org.scraelos.esofurnituremp.model.ItemScreenshot;
import org.scraelos.esofurnituremp.model.ItemScreenshotFull;
import org.scraelos.esofurnituremp.model.KnownRecipe;
import org.scraelos.esofurnituremp.model.RecipeIngredient;
import org.scraelos.esofurnituremp.model.SelectedFurniture;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
import org.vaadin.artur.spring.dataprovider.PageableDataProvider;
import org.vaadin.liveimageeditor.LiveImageEditor;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@SpringView(name = FurnitureItemsView.NAME)
public class FurnitureItemsView extends CustomComponent implements View, LocaleChangedListener {

    public static final String NAME = "furniture";
    @Autowired
    private DBService dBService;
    private Bundle i18n = new Bundle();

    @Autowired
    FurnitureItemRepository repo;

    private Tree tree;
    private Grid<FurnitureItem> grid;
    private Grid<KnownRecipe> craftersGrid;
    private TabSheet.Tab materialsTab;
    private TabSheet.Tab craftersTab;

    private HorizontalLayout filters;
    private CheckBox onlyCraftable;
    private CheckBox hasCrafters;
    private ComboBox<ITEM_QUALITY> itemQuality;
    private ComboBox<FURNITURE_THEME> theme;
    private ComboBox<ESO_SERVER> server;
    private TextField searchField;
    private TextField crafterField;
    private CheckBox searchFieldIgnoresOtherFilters;
    private CheckBox unknownRecipes;
    private String searchValue;
    private String crafterIdValue;
    private ScreenshotClickListener screenshotClickListener;
    private DeleteImageClickListener deleteImageClickListener;

    private ScreenShotViewWindwow screenShotViewWindwow;

    private FurnitureItemSpecification specification;

    static final int PAGESIZE = 15;

    private String itemNameColumn = "nameEn";

    private TabSheet itemTabs;
    private VerticalLayout itemInfoLayout;

    private Label itemNameLabel;
    private TextField itemLinkField;
    private Link itemTTClink;
    private Link recipeTTClink;

    private Grid<RecipeIngredient> materialsGrid;
    private Button addToCartButton;

    FurnitureItem selectedFurnitureItem;
    private int initialScrollPosition;

    public FurnitureItemsView() {
        this.setSizeFull();
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(false);
        vl.setSizeFull();
        specification = new FurnitureItemSpecification();
        filters = new HorizontalLayout();
        server = new ComboBox(null, Arrays.asList(ESO_SERVER.values()));
        server.setEmptySelectionAllowed(false);
        server.addValueChangeListener(new HasValue.ValueChangeListener() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent event) {
                loadItems();
            }
        });

        filters.addComponent(server);
        itemQuality = new ComboBox(null, Arrays.asList(ITEM_QUALITY.values()));
        itemQuality.setStyleGenerator((ITEM_QUALITY item) -> {
            if (item != null) {
                return item.name().toLowerCase();
            }
            return null;
        });
        itemQuality.setEmptySelectionAllowed(true);

        filters.addComponent(itemQuality);
        theme = new ComboBox(null, Arrays.asList(FURNITURE_THEME.values()));
        theme.setPageLength(25);
        theme.setEmptySelectionAllowed(true);

        filters.addComponent(theme);

        onlyCraftable = new CheckBox(null, false);
        onlyCraftable.addValueChangeListener((HasValue.ValueChangeEvent<Boolean> event) -> {
            loadItems();
        });
        onlyCraftable.addValueChangeListener((HasValue.ValueChangeEvent<Boolean> event) -> {
            loadItems();
        });
        filters.addComponent(onlyCraftable);
        filters.setComponentAlignment(onlyCraftable, Alignment.BOTTOM_LEFT);
        hasCrafters = new CheckBox(null, false);
        hasCrafters.addValueChangeListener((HasValue.ValueChangeEvent<Boolean> event) -> {
            loadItems();
        });
        filters.addComponent(hasCrafters);
        filters.setComponentAlignment(hasCrafters, Alignment.BOTTOM_LEFT);
        unknownRecipes = new CheckBox(null, false);
        unknownRecipes.addValueChangeListener((HasValue.ValueChangeEvent<Boolean> event) -> {
            loadItems();
        });
        if (SpringSecurityHelper.getUser() != null) {
            filters.addComponent(unknownRecipes);
            filters.setComponentAlignment(unknownRecipes, Alignment.BOTTOM_LEFT);
        }
        crafterField = new TextField(null, "");
        crafterField.addValueChangeListener(new HasValue.ValueChangeListener<String>() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent<String> event) {
                crafterIdValue = event.getValue();
                loadItems();
            }
        });
        filters.addComponent(crafterField);
        filters.setComponentAlignment(crafterField, Alignment.BOTTOM_LEFT);
        filters.setSpacing(true);
        vl.addComponent(filters);
        HorizontalLayout textfilter = new HorizontalLayout();
        textfilter.setSpacing(true);
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
        searchFieldIgnoresOtherFilters.addValueChangeListener((HasValue.ValueChangeEvent<Boolean> event) -> {
            loadItems();
        });
        textfilter.addComponent(searchField);
        textfilter.addComponent(searchFieldIgnoresOtherFilters);
        textfilter.setComponentAlignment(searchFieldIgnoresOtherFilters, Alignment.BOTTOM_LEFT);

        vl.addComponent(textfilter);
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        tree = new Tree();
        tree.setSizeFull();
        tree.setWidth(250f, Unit.PIXELS);
        tree.setSelectionMode(Grid.SelectionMode.SINGLE);
        tree.addSelectionListener(new SelectionListener() {
            @Override
            public void selectionChange(SelectionEvent event) {
                loadItems();
            }
        });
        tree.addStyleName("v-scrollable");
        hl.addComponent(tree);
        grid = new Grid<>(FurnitureItem.class);
        grid.setHeaderVisible(false);
        setSpecification();
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
        grid.removeHeaderRow(0);
        grid.setBodyRowHeight(80);
        grid.getColumn("nameEn").setStyleGenerator(new CustomCellStyleGenerator());
        grid.getColumn("nameDe").setStyleGenerator(new CustomCellStyleGenerator());
        grid.getColumn("nameFr").setStyleGenerator(new CustomCellStyleGenerator());
        grid.getColumn("nameRu").setStyleGenerator(new CustomCellStyleGenerator());
        grid.addComponentColumn(new ScreenshotValueProvider()).setId("screenshots");
        grid.addComponentColumn(new IconValueProvider()).setId("iconColumn").setMaximumWidth(100).setMinimumWidth(100);
        grid.setColumns("iconColumn", itemNameColumn, "screenshots");
        grid.setSizeFull();
        grid.addSelectionListener(new FurnitureItemClickListener());
        hl.addComponent(grid);
        hl.setExpandRatio(grid, 1f);
        Panel p = new Panel();
        p.setSizeFull();
        p.setStyleName(ValoTheme.PANEL_BORDERLESS);
        itemTabs = new TabSheet();
        itemTabs.setSizeFull();
        itemInfoLayout = new VerticalLayout();
        itemInfoLayout.setSizeFull();
        itemInfoLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        itemInfoLayout.setMargin(new MarginInfo(false, false, false, true));
        itemNameLabel = new Label();
        itemInfoLayout.addComponent(itemNameLabel);
        itemLinkField = new TextField();
        itemLinkField.setWidth(420f, Unit.PIXELS);
        itemLinkField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        itemLinkField.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        itemInfoLayout.addComponent(itemLinkField);
        itemTTClink = new Link();
        itemTTClink.setTargetName("_blank");
        itemInfoLayout.addComponent(itemTTClink);
        recipeTTClink = new Link();
        recipeTTClink.setTargetName("_blank");
        itemInfoLayout.addComponent(recipeTTClink);
        addToCartButton = new Button("", VaadinIcons.CART);
        addToCartButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                addToCart((FurnitureItem) event.getButton().getData());
            }
        });
        itemInfoLayout.addComponent(addToCartButton);
        materialsGrid = new Grid(RecipeIngredient.class);
        materialsGrid.setSizeFull();

        materialsGrid.setColumns("ingredient", "count");

        materialsGrid.getColumn("ingredient").setRenderer(new TextRenderer() {
            @Override
            public JsonValue encode(Object val) {
                Ingredient value = (Ingredient) val;
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

        });
        materialsTab = itemTabs.addTab(materialsGrid);

        craftersGrid = new Grid<>();
        craftersGrid.setSizeFull();
        craftersGrid.addColumn(new ValueProvider<KnownRecipe, String>() {
            @Override
            public String apply(KnownRecipe source) {
                return "@" + source.getAccount().getEsoId();
            }
        }).setStyleGenerator(new StyleGenerator<KnownRecipe>() {
            @Override
            public String apply(KnownRecipe item) {
                switch (item.getAccount().getOnlineStatus()) {
                    case Online:
                        return "fine";
                    case Offline:
                        return "offline-color";
                    default:
                        return "";
                }
            }
        }).setId("esoId").setMinimumWidth(300).setExpandRatio(2);
        craftersGrid.addColumn(KnownRecipe::getCraftPrice).setId("craftPrice").setRenderer(new TextRenderer("") {
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

        });
        craftersGrid.addColumn(KnownRecipe::getCraftPriceWithMats).setId("craftPriceWithMats").setRenderer(new TextRenderer("") {
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
        });
        craftersGrid.setColumns("esoId", "craftPrice", "craftPriceWithMats");
        itemInfoLayout.setVisible(false);
        craftersTab = itemTabs.addTab(craftersGrid);
        craftersTab.setClosable(false);
        itemTabs.setVisible(false);
        p.setContent(itemTabs);
        itemInfoLayout.addComponent(p);
        itemInfoLayout.setExpandRatio(p, 1f);
        hl.addComponent(itemInfoLayout);
        hl.setExpandRatio(itemInfoLayout, 0.6f);
        vl.addComponent(hl);
        vl.setExpandRatio(hl, 1f);
        setCompositionRoot(vl);
    }

    private void setSpecification() {
        specification.setCategories(tree.getSelectedItems());
        specification.setOnlyCraftable(onlyCraftable.getValue());
        specification.setHasCrafters(hasCrafters.getValue());
        specification.setSearchString(searchValue);
        specification.setEsoServer(server.getValue());
        specification.setCrafterId(crafterIdValue);
        if (itemQuality.getValue() != null) {
            specification.setItemQuality(itemQuality.getValue());
        } else {
            specification.setItemQuality(null);
        }
        if (theme.getValue() != null) {
            specification.setTheme(theme.getValue());
        } else {
            specification.setTheme(null);
        }
        specification.setSearchStringIgnoresAll(searchFieldIgnoresOtherFilters.getValue());
        if (SpringSecurityHelper.getUser() != null) {
            specification.setAccount(SpringSecurityHelper.getUser());
        }
        specification.setUnknownRecipes(unknownRecipes.getValue());
    }

    private void loadItems() {
        setUrlParameters();
        setSpecification();
        grid.getDataProvider().refreshAll();
    }

    private Sort toSort(List<QuerySortOrder> l) {
        List<Order> orders = new ArrayList<>();
        for (QuerySortOrder o : l) {
            orders.add(o.getDirection() == SortDirection.ASCENDING ? Order.asc(o.getSorted()) : Order.desc(o.getSorted()));
        }
        return Sort.by(orders);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        getUI().getPage().setTitle(i18n.furnitureCatalogMenuItemCaption() + " | " + i18n.siteTitle());
        setInitialScrollPosition(event);

        screenshotClickListener = new ScreenshotClickListener();
        deleteImageClickListener = new DeleteImageClickListener();

        List<FurnitureCategory> itemCategoriesList = dBService.getItemCategoriesList();
        TreeData treeData = new TreeData();
        treeData.addRootItems(itemCategoriesList);
        for (FurnitureCategory c : itemCategoriesList) {
            treeData.addItems(c, c.getChilds());
        }
        tree.setTreeData(treeData);
        grid.setStyleGenerator(new CustomCellStyleGenerator());

        if (SpringSecurityHelper.getUser() != null) {
            server.setValue(SpringSecurityHelper.getUser().getEsoServer());
        } else {
            server.setValue(ESO_SERVER.EU);
        }
        handleUrlParameters(event);
        localize(getUI().getLocale());
        loadItems();
        itemQuality.addValueChangeListener(new HasValue.ValueChangeListener() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent event) {
                loadItems();
            }
        });
        theme.addValueChangeListener(new HasValue.ValueChangeListener() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent event) {
                loadItems();
            }
        });
        handleUrlScrollParameters(event);
    }

    private void setUrlParameters() {
        StringBuilder sb = new StringBuilder("!" + NAME);
        if (server.getValue() != null) {
            sb.append("/server=").append(server.getValue().name());
        }
        if (itemQuality.getValue() != null) {
            sb.append("/quality=").append(itemQuality.getValue().name());
        }
        if (theme.getValue() != null) {
            sb.append("/theme=").append(theme.getValue().name());
        }
        if (crafterField.getValue() != null && !crafterField.getValue().isEmpty()) {
            sb.append("/crafter=").append(crafterField.getValue());
        }
        if (onlyCraftable.getValue() != null) {
            sb.append("/onlyCraftable=").append(onlyCraftable.getValue().toString());
        }
        if (hasCrafters.getValue() != null) {
            sb.append("/hasCrafters=").append(hasCrafters.getValue().toString());
        }
        if (searchFieldIgnoresOtherFilters.getValue() != null) {
            sb.append("/searchFieldIgnoresOtherFilters=").append(searchFieldIgnoresOtherFilters.getValue().toString());
        }
        if (searchField.getValue() != null && !searchField.getValue().isEmpty()) {
            try {
                sb.append("/search=").append(URLEncoder.encode(searchField.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(FurnitureItemsView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (tree.getSelectedItems() != null && !tree.getSelectedItems().isEmpty()) {
            sb.append("/cat=").append(((FurnitureCategory) tree.getSelectedItems().iterator().next()).getId());
        }
        if (selectedFurnitureItem != null) {
            sb.append("/item=").append(selectedFurnitureItem.getId().toString());
        }
        getUI().getPage().setUriFragment(sb.toString(), false);
    }

    private void handleUrlParameters(ViewChangeListener.ViewChangeEvent event) {
        Map<String, String> parameterMap = event.getParameterMap("/");
        String serverParameter = parameterMap.get("server");
        String qualityParameter = parameterMap.get("quality");
        String themeParameter = parameterMap.get("theme");
        String crafterParameter = parameterMap.get("crafter");
        String onlyCraftableParameter = parameterMap.get("onlyCraftable");
        String hasCraftersParameter = parameterMap.get("hasCrafters");
        String searchFieldIgnoresOtherFiltersParameter = parameterMap.get("searchFieldIgnoresOtherFilters");
        String searchParameter = parameterMap.get("search");
        String catParameter = parameterMap.get("cat");
        String selectedItemPatameter = parameterMap.get("item");
        if (serverParameter != null) {
            server.setValue(ESO_SERVER.valueOf(serverParameter));
        }
        if (qualityParameter != null) {
            itemQuality.setValue(ITEM_QUALITY.valueOf(qualityParameter));
        }
        if (themeParameter != null) {
            theme.setValue(FURNITURE_THEME.valueOf(themeParameter));
        }
        if (crafterParameter != null) {
            crafterField.setValue(crafterParameter);
        }
        if (onlyCraftableParameter != null) {
            onlyCraftable.setValue(Boolean.valueOf(onlyCraftableParameter));
        }
        if (hasCraftersParameter != null) {
            hasCrafters.setValue(Boolean.valueOf(hasCraftersParameter));
        }
        if (searchFieldIgnoresOtherFiltersParameter != null) {
            searchFieldIgnoresOtherFilters.setValue(Boolean.valueOf(searchFieldIgnoresOtherFiltersParameter));
        }
        if (searchParameter != null) {
            try {
                searchField.setValue(URLDecoder.decode(searchParameter, "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(FurnitureItemsView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (catParameter != null) {
            try {
                FurnitureCategory c = new FurnitureCategory();
                c.setId(Long.valueOf(catParameter));
                tree.select(c);
                if (tree.getTreeData().getParent(c) != null) {
                    tree.expand(tree.getTreeData().getParent(c));
                }
            } catch (NumberFormatException ex) {

            }
        }

    }

    private void handleUrlScrollParameters(ViewChangeListener.ViewChangeEvent event) {

    }

    private void setInitialScrollPosition(ViewChangeListener.ViewChangeEvent event) {
        Map<String, String> parameterMap = event.getParameterMap("/");
        String scrollToParameter = parameterMap.get("scrollTo");
        if (scrollToParameter != null) {
            try {
                initialScrollPosition = Integer.valueOf(scrollToParameter);
            } catch (NumberFormatException ex) {

            }
        }
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
        materialsGrid.getDataProvider().refreshAll();
    }

    private void localize(Locale locale) {
        Boolean useEnglishNames = (Boolean) getUI().getSession().getAttribute("useEnglishNames");
        if (useEnglishNames == null || !useEnglishNames) {
            itemNameColumn = i18n.localizedItemNameColumn();
        } else {
            itemNameColumn = "nameEn";
        }
        materialsTab.setCaption(i18n.materials());
        craftersTab.setCaption(i18n.craftersTableCaption());
        filters.setCaption(i18n.filters());
        server.setCaption(i18n.serverForCraftersSearch());
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
        onlyCraftable.setCaption(i18n.displayOnlyCraftable());
        hasCrafters.setCaption(i18n.hasCrafters());
        searchField.setCaption(i18n.searchField());
        searchFieldIgnoresOtherFilters.setCaption(i18n.searchFieldIgnoreFilters());
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
        grid.setColumns("iconColumn", itemNameColumn, "screenshots");
        grid.setCaption(i18n.furnitureListItemTableCaption());
        grid.getColumn(itemNameColumn).setWidth(400).setCaption(i18n.item());
        grid.getColumn("screenshots").setCaption(i18n.screenshots());
        if (selectedFurnitureItem != null) {
            if (useEnglishNames == null || !useEnglishNames) {
                itemNameLabel.setCaption(selectedFurnitureItem.getLocalizedName(locale));
            } else {
                itemNameLabel.setCaption(selectedFurnitureItem.getNameEn());
            }
        }
        itemTTClink.setCaption(i18n.ttcSearchItem());
        addToCartButton.setCaption(i18n.addToCart());
        recipeTTClink.setCaption(i18n.ttcRecipeSearchItem());
        unknownRecipes.setCaption(i18n.unknownRecipes());
        crafterField.setCaption(i18n.crafterId());
        craftersGrid.getColumn("craftPrice").setCaption(i18n.craftPrice());
        craftersGrid.getColumn("craftPriceWithMats").setCaption(i18n.craftPriceWithMats());

    }

    private class CustomCellStyleGenerator implements StyleGenerator<FurnitureItem> {

        @Override
        public String apply(FurnitureItem item) {
            if (item != null && item.getItemQuality() != null) {
                return item.getItemQuality().name().toLowerCase();
            }
            return null;
        }

    }

    private class ScreenshotValueProvider implements ValueProvider<FurnitureItem, HorizontalLayout> {

        @Override
        public HorizontalLayout apply(FurnitureItem source) {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setDefaultComponentAlignment(Alignment.TOP_LEFT);
            hl.setSpacing(true);
            int counter = 0;
            if (SpringSecurityHelper.hasRole("ROLE_UPLOAD_SCREENSHOTS")) {
                Button uploadScreenShot = new Button(FontAwesome.UPLOAD);
                uploadScreenShot.setData(source);
                uploadScreenShot.addClickListener(new Button.ClickListener() {

                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        UploadScreenshotWindow window = new UploadScreenshotWindow(source);
                        getUI().addWindow(window);

                    }
                });
                hl.addComponent(uploadScreenShot);

            }
            for (final ItemScreenshot s : source.getItemScreenshots()) {
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
                counter++;
                if (counter > 1) {
                    break;
                }
            }

            return hl;
        }

    }

    private class IconValueProvider implements ValueProvider<FurnitureItem, Image> {

        @Override
        public Image apply(FurnitureItem source) {
            if (source.getIcon() != null) {
                Image image = new Image(null, new ExternalResource("https://elderscrolls.net" + source.getIcon().replaceAll(".dds", ".png")));
                return image;
            }
            return null;
        }

    }

    public void refreshFurnitureItem(FurnitureItem item) {
        try {
            grid.getDataProvider().refreshItem(item);
        } catch (Exception ex) {

        }
    }

    private class ScreenshotClickListener implements MouseEvents.ClickListener {

        @Override
        public void click(MouseEvents.ClickEvent event) {
            Image i = (Image) event.getComponent();
            final ItemScreenshot sc = (ItemScreenshot) i.getData();
            grid.select(sc.getFurnitureItem());
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
                ItemScreenshotFull itemScreenshotFull = new ItemScreenshotFull();
                itemScreenshotFull.setScreenshot(image);
                itemScreenshotFull.setThumb(screenshot);
                screenshot.setFull(itemScreenshotFull);
                screenshot.setThumbnail(resize(image, 100));
                dBService.saveEntity(screenshot);
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

    private void addToCart(FurnitureItem item) {
        List<SelectedFurniture> cart = (List<SelectedFurniture>) getUI().getSession().getAttribute("itemCart");
        if (cart == null) {
            cart = new ArrayList<>();
        }
        boolean found = false;
        for (SelectedFurniture f : cart) {
            if (f.getItem().equals(item)) {
                found = true;
                f.add();
                break;
            }
        }
        if (!found) {
            SelectedFurniture f = new SelectedFurniture();
            f.setItem(item);
            f.add();
            cart.add(f);
        }
        getUI().getSession().setAttribute("itemCart", cart);
        openCart();

    }

    private void openCart() {
        CartWindow w = new CartWindow();
        getUI().addWindow(w);
        w.LoadCart();
    }

    private class FurnitureItemClickListener implements SelectionListener<FurnitureItem> {

        @Override
        public void selectionChange(SelectionEvent<FurnitureItem> event) {
            if (event.getAllSelectedItems() != null && !event.getAllSelectedItems().isEmpty()) {

                selectedFurnitureItem = (FurnitureItem) event.getFirstSelectedItem().get();
                setUrlParameters();
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

                itemLinkField.setReadOnly(false);
                itemLinkField.setValue(selectedFurnitureItem.getItemLink());
                itemLinkField.setReadOnly(true);
                StringBuilder itemStoreLinkBuilder = new StringBuilder();
                ESO_SERVER serverValue = server.getValue();
                if (serverValue == ESO_SERVER.NA) {
                    itemStoreLinkBuilder.append("https://na.");
                } else {
                    itemStoreLinkBuilder.append("https://eu.");
                }
                itemStoreLinkBuilder.append("tamrieltradecentre.com/pc/Trade/SearchResult?ItemID=&ItemCategory1ID=6");
                itemStoreLinkBuilder.append("&ItemCategory2ID=");
                /*if (selectedFurnitureItem.getSubCategory().getTtcSubcategory() != null) {
                if (selectedFurnitureItem.getSubCategory().getTtcSubcategory() == 33) {
                    itemStoreLinkBuilder.append("");
                } else {
                    itemStoreLinkBuilder.append(selectedFurnitureItem.getSubCategory().getTtcSubcategory().toString());
                }

            } else {
                itemStoreLinkBuilder.append("35");
            }*/
                itemStoreLinkBuilder.append("&ItemNamePattern=");
                itemStoreLinkBuilder.append(HtmlUtils.htmlEscape(selectedFurnitureItem.getNameEn()));
                ExternalResource itemStoreLink = new ExternalResource(itemStoreLinkBuilder.toString());
                itemTTClink.setResource(itemStoreLink);
                if (selectedFurnitureItem.getRecipe() != null) {
                    StringBuilder recipeStoreLinkBuilder = new StringBuilder();
                    if (serverValue == ESO_SERVER.NA) {
                        recipeStoreLinkBuilder.append("https://na.");
                    } else {
                        recipeStoreLinkBuilder.append("https://eu.");
                    }
                    recipeStoreLinkBuilder.append("tamrieltradecentre.com/pc/Trade/SearchResult?ItemID=&ItemCategory1ID=6&ItemCategory2ID=38");
                    recipeStoreLinkBuilder.append("&lang=en-US&ItemNamePattern=");
                    recipeStoreLinkBuilder.append(HtmlUtils.htmlEscape(selectedFurnitureItem.getRecipe().getNameEn()));
                    ExternalResource recipeStoreLink = new ExternalResource(recipeStoreLinkBuilder.toString());
                    recipeTTClink.setResource(recipeStoreLink);
                    recipeTTClink.setVisible(true);
                } else {
                    recipeTTClink.setVisible(false);
                }
                itemInfoLayout.setVisible(true);
                if (selectedFurnitureItem.getRecipe() != null) {
                    addToCartButton.setVisible(true);
                    addToCartButton.setData(selectedFurnitureItem);
                    materialsGrid.setItems(selectedFurnitureItem.getRecipe().getRecipeIngredients());
                    itemTabs.setVisible(true);
                    craftersGrid.setItems(dBService.getCrafters(selectedFurnitureItem.getRecipe(), (ESO_SERVER) server.getValue()));
                } else {
                    addToCartButton.setVisible(false);
                    addToCartButton.setData(null);
                    itemTabs.setVisible(false);
                }
            }
        }
    }

}
