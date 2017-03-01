/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

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
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.datenhahn.vaadin.componentrenderer.ComponentRenderer;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.scraelos.esofurnituremp.model.ItemCategory;
import org.scraelos.esofurnituremp.model.ItemScreenshot;
import org.scraelos.esofurnituremp.model.ItemSubCategory;
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
public class FurnitureItemsView extends CustomComponent implements View {

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
    private Table craftersTable;

    private SortableLazyList<FurnitureItem> furnitureList;

    private HierarchicalContainer craftersContainer;

    private CheckBox onlyCraftable;
    private ComboBox server;
    private TextField searchField;
    private CheckBox searchFieldIgnoresOtherFilters;
    private ItemSubCategory currentCategory;
    private String searchValue;
    private ScreenshotClickListener screenshotClickListener;

    private FurnitureItemSpecification specification;

    static final int PAGESIZE = 45;

    private String itemNameColumn;

    public FurnitureItemsView() {
        this.setSizeFull();
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        header = new Header();
        vl.addComponent(header);
        HorizontalLayout filters = new HorizontalLayout();
        filters.setCaption(i18n.filters());
        server = new ComboBox(i18n.serverForCraftersSearch(), Arrays.asList(ESO_SERVER.values()));
        server.setNullSelectionAllowed(false);

        filters.addComponent(server);

        onlyCraftable = new CheckBox(i18n.displayOnlyCraftable(), false);
        onlyCraftable.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                loadItems();
            }
        });
        filters.addComponent(onlyCraftable);
        filters.setComponentAlignment(onlyCraftable, Alignment.BOTTOM_LEFT);
        filters.setSpacing(true);
        vl.addComponent(filters);
        HorizontalLayout textfilter = new HorizontalLayout();
        textfilter.setSpacing(true);
        searchField = new TextField(i18n.searchField());
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
        searchFieldIgnoresOtherFilters = new CheckBox(i18n.searchFieldIgnoreFilters(), true);
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
        tree = new Tree(i18n.categories());
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
        grid.setCaption(i18n.furnitureListItemTableCaption());
        grid.setSizeFull();
        grid.addItemClickListener(new ItemClickEvent.ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                FurnitureItem furnitureItem = (FurnitureItem) event.getItemId();

                if (furnitureItem.getRecipe() != null) {
                    craftersContainer = dBService.getCrafters(craftersContainer, furnitureItem.getRecipe(), (ESO_SERVER) server.getValue());
                    craftersTable.setVisible(true);
                } else {
                    craftersTable.setVisible(false);
                }
            }
        });
        listContainer = new GeneratedPropertyListContainer<>(FurnitureItem.class);
        hl.addComponent(grid);
        hl.setExpandRatio(grid, 1f);
        craftersTable = new Table(i18n.craftersTableCaption());
        craftersTable.setSizeFull();
        craftersContainer = new HierarchicalContainer();
        craftersContainer.addContainerProperty("id", String.class, null);
        craftersTable.setContainerDataSource(craftersContainer);
        craftersTable.setVisible(false);
        hl.addComponent(craftersTable);
        hl.setExpandRatio(craftersTable, 0.5f);
        vl.addComponent(hl);
        vl.setExpandRatio(hl, 1f);
        setCompositionRoot(vl);
    }

    private void loadItems() {
        specification.setCategory(currentCategory);
        specification.setOnlyCraftable(onlyCraftable.getValue());
        specification.setSearchString(searchValue);
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
        Boolean useEnglishNames = (Boolean) getUI().getSession().getAttribute("useEnglishNames");
        if (useEnglishNames == null || !useEnglishNames) {
            itemNameColumn = i18n.localizedItemNameColumn();
        } else {
            itemNameColumn = "nameEn";
        }
        specification = new FurnitureItemSpecification();
        screenshotClickListener = new ScreenshotClickListener();
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
        grid.setContainerDataSource(listContainer);
        grid.getColumn(itemNameColumn).setWidth(400).setHeaderCaption(i18n.item());
        grid.getColumn("category").setHeaderCaption(i18n.category());
        grid.getColumn("screenshots").setHeaderCaption(i18n.screenshots()).setRenderer(new ComponentRenderer()).setExpandRatio(1);
        grid.setColumns(new Object[]{itemNameColumn, "screenshots", "category"});
        loadItems();
        if (SpringSecurityHelper.getUser() != null) {
            server.setValue(SpringSecurityHelper.getUser().getEsoServer());
        } else {
            server.setValue(ESO_SERVER.EU);
        }

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

    private class ScreenShotsColumnGenerator extends PropertyValueGenerator<HorizontalLayout> {

        @Override
        public HorizontalLayout getValue(Item item, Object itemId, Object propertyId) {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSizeFull();
            hl.setSpacing(true);
            final FurnitureItem furnitureItem = (FurnitureItem) itemId;
            int counter = 0;
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
                if (counter > 2) {
                    break;
                }
            }
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
            ScreenShotViewWindwow window = new ScreenShotViewWindwow(sc);
            getUI().addWindow(window);
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
            panel.setContent(screenshotImage);
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
