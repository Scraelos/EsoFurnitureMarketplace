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
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Panel;
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
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.poi.util.IOUtils;
import org.scraelos.esofurnituremp.Bundle;
import org.scraelos.esofurnituremp.data.KnownRecipeRepository;
import org.scraelos.esofurnituremp.data.KnownRecipeSpecification;
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.scraelos.esofurnituremp.model.ItemCategory;
import org.scraelos.esofurnituremp.model.ItemScreenshot;
import org.scraelos.esofurnituremp.model.ItemSubCategory;
import org.scraelos.esofurnituremp.model.KnownRecipe;
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
import org.vaadin.viritin.SortableLazyList;
import org.vaadin.viritin.grid.GeneratedPropertyListContainer;
import ru.xpoft.vaadin.VaadinView;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@VaadinView(KnownRecipesView.NAME)
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
    private Button importButton;

    private SortableLazyList<KnownRecipe> itemList;
    private GeneratedPropertyListContainer<KnownRecipe> listContainer = new GeneratedPropertyListContainer(KnownRecipe.class);
    private KnownRecipeSpecification specification;
    private ScreenshotClickListener screenshotClickListener;
    static final int PAGESIZE = 45;

    public KnownRecipesView() {
        header = new Header();
        this.setSizeFull();
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        header = new Header();
        vl.addComponent(header);
        actions = new HorizontalLayout();
        importButton = new Button(null, new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getUI().getNavigator().navigateTo(ImportKnownRecipesView.NAME);
            }
        });
        actions.addComponent(importButton);
        vl.addComponent(actions);
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        tree = new Tree();
        tree.setSizeFull();
        tree.setWidth(200f, Sizeable.Unit.PIXELS);
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
        specification = new KnownRecipeSpecification(SpringSecurityHelper.getUser());
        tree.setContainerDataSource(dBService.getItemCategories());
        listContainer.addGeneratedProperty("screenshots", new ScreenShotsColumnGenerator());
        grid.setContainerDataSource(listContainer);
        grid.setColumns(new Object[]{"recipe", "screenshots", "characterName", "esoServer"});
        grid.getColumn("screenshots").setRenderer(new ComponentRenderer()).setExpandRatio(1);
        grid.getColumn("recipe").setWidth(450).setConverter(new Converter<String, Recipe>() {

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
                            if (value.getRecipeType() != null) {
                                result = value.getRecipeType().getNameEn() + ": " + result;
                            }
                            break;
                        case "de":
                            result = value.getNameDe();
                            if (value.getRecipeType() != null) {
                                result = value.getRecipeType().getNameDe() + ": " + result;
                            }
                            break;
                        case "fr":
                            result = value.getNameFr();
                            if (value.getRecipeType() != null) {
                                result = value.getRecipeType().getNameFr() + " : " + result;
                            }
                            break;
                        case "ru":
                            result = value.getNameRu();
                            if (value.getRecipeType() != null) {
                                result = value.getRecipeType().getNameRu() + ": " + result;
                            }
                            break;
                    }
                } else {
                    result = value.getNameEn();
                    if (value.getRecipeType() != null) {
                        result = value.getRecipeType().getNameEn() + ": " + result;
                    }
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
        localize();
        loadItems();

    }

    private void loadItems() {
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
        localize();
        grid.refreshAllRows();
    }

    private void localize() {
        importButton.setCaption(i18n.importDataFromCraftStoreButtonCaption());
        tree.setCaption(i18n.categories());
        grid.setCaption(i18n.knownRecipesTableCaption());
        grid.getColumn("recipe").setHeaderCaption(i18n.recipe());
        grid.getColumn("characterName").setHeaderCaption(i18n.characterName());
        grid.getColumn("esoServer").setHeaderCaption(i18n.server());
    }

    public void refreshGridItem(Object itemId) {
        grid.refreshRows(itemId);
    }

    private class TreeItemClickListener implements ItemClickEvent.ItemClickListener {

        @Override
        public void itemClick(ItemClickEvent event) {
            Object itemId = event.getItemId();
            if (itemId instanceof ItemCategory) {
                tree.expandItem(itemId);
            } else if (itemId instanceof ItemSubCategory) {
                specification.setCategory((ItemSubCategory) itemId);
                loadItems();
            }
        }

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
                            UploadScreenshotWindow window = new UploadScreenshotWindow(knownRecipe);
                            getUI().addWindow(window);

                        }
                    });
                    hl.addComponent(uploadScreenShot);

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
                screenshot.setScreenshot(image);
                screenshot.setThumbnail(resize(image, 100));
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

}
