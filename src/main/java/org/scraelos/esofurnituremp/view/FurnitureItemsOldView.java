/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.github.peholmst.i18n4vaadin.annotations.Message;
import com.github.peholmst.i18n4vaadin.annotations.Messages;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.IsNull;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Not;
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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.poi.util.IOUtils;
import org.scraelos.esofurnituremp.model.ESO_SERVER;
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.scraelos.esofurnituremp.model.ItemCategory;
import org.scraelos.esofurnituremp.model.ItemScreenshot;
import org.scraelos.esofurnituremp.model.ItemSubCategory;
import org.scraelos.esofurnituremp.model.RecipeIngredient;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.liveimageeditor.LiveImageEditor;
import ru.xpoft.vaadin.VaadinView;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@VaadinView(FurnitureItemsOldView.NAME)
public class FurnitureItemsOldView extends CustomComponent implements View {

    public static final String NAME = "furnitureOld";

    private Header header;
    @Autowired
    private DBService dBService;
    private Bundle i18n = new Bundle();

    private Tree tree;
    private Table table;
    private Table craftersTable;
    private JPAContainer<FurnitureItem> container;

    private HierarchicalContainer craftersContainer;

    private CheckBox onlyCraftable;
    private ComboBox server;
    private TextField searchField;
    private ItemSubCategory currentCategory;
    private String searchValue;
    private ScreenshotClickListener screenshotClickListener;

    @Messages({
        @Message(key = "filters", value = "Filters"),
        @Message(key = "serverForCraftersSearch", value = "Search for crafters on this server"),
        @Message(key = "searchField", value = "Search string"),
        @Message(key = "displayOnlyCraftable", value = "Display only craftable items"),
        @Message(key = "categories", value = "Catergories"),
        @Message(key = "furnitureListItemTableCaption", value = "Items - click on item name to display crafters"),
        @Message(key = "craftersTableCaption", value = "Crafters"),
        @Message(key = "item", value = "Item"),
        @Message(key = "category", value = "Category"),
        @Message(key = "screenshotAlternativeText", value = "{0} by @{1}"),
        @Message(key = "screenshots", value = "Screenshots"),
        @Message(key = "uploadScreenshotWindowCaption", value = "New Screenshot"),
        @Message(key = "uploadScreenshotUploadCaption", value = "Upload your screenshot"),
        @Message(key = "uploadScreenshotSaveCaption", value = "Save Screenshot")
    })
    public FurnitureItemsOldView() {
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

        searchField = new TextField(i18n.searchField());
        searchField.setWidth(300f, Unit.PIXELS);
        searchField.addTextChangeListener(new FieldEvents.TextChangeListener() {

            @Override
            public void textChange(FieldEvents.TextChangeEvent event) {
                searchValue = event.getText();
                applyFilters();
            }
        });
        searchField.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.TIMEOUT);
        searchField.setTextChangeTimeout(2000);
        filters.addComponent(searchField);
        onlyCraftable = new CheckBox(i18n.displayOnlyCraftable(), false);
        onlyCraftable.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                applyFilters();
            }
        });
        filters.addComponent(onlyCraftable);
        filters.setComponentAlignment(onlyCraftable, Alignment.BOTTOM_LEFT);
        filters.setSpacing(true);
        vl.addComponent(filters);

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
        table = new Table(i18n.furnitureListItemTableCaption());
        table.setSizeFull();
        table.setSelectable(true);
        table.addItemClickListener(new ItemClickEvent.ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                EntityItem item = (EntityItem) event.getItem();
                FurnitureItem furnitureItem = (FurnitureItem) item.getEntity();

                if (furnitureItem.getRecipe() != null) {
                    craftersContainer = dBService.getCrafters(craftersContainer, furnitureItem.getRecipe(), (ESO_SERVER) server.getValue());
                    craftersTable.setVisible(true);
                } else {
                    craftersTable.setVisible(false);
                }
            }
        });
        hl.addComponent(table);
        hl.setExpandRatio(table, 1f);
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

    private void applyFilters() {
        container.removeAllContainerFilters();
        if (searchValue != null && searchValue.length() > 2) {
            container.addContainerFilter(new Like("nameEn", "%" + searchValue + "%", false));
        } else {
            if (currentCategory != null) {
                container.addContainerFilter(new Compare.Equal("subCategory", currentCategory));
            }
            if (onlyCraftable.getValue()) {
                container.addContainerFilter(new Not(new IsNull("recipe")));
            }
        }

    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        header.build();
        screenshotClickListener = new ScreenshotClickListener();
        tree.setContainerDataSource(dBService.getItemCategories());
        container = dBService.getJPAContainerContainerForClass(FurnitureItem.class);
        table.setContainerDataSource(container);
        table.addGeneratedColumn("category", new Table.ColumnGenerator() {

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                EntityItem item = (EntityItem) source.getItem(itemId);
                FurnitureItem furnitureItem = (FurnitureItem) item.getEntity();
                String result = furnitureItem.getSubCategory().getCategory().toString() + ", " + furnitureItem.getSubCategory().toString();
                return result;
            }
        });
        /*table.addGeneratedColumn("ingredients", new Table.ColumnGenerator() {

         @Override
         public Object generateCell(Table source, Object itemId, Object columnId) {
         EntityItem item = (EntityItem) source.getItem(itemId);
         FurnitureItem furnitureItem = (FurnitureItem) item.getEntity();
         if (furnitureItem.getRecipe() != null) {
         StringBuilder sb = new StringBuilder();
         for (RecipeIngredient i : furnitureItem.getRecipe().getRecipeIngredients()) {
         if (!sb.toString().isEmpty()) {
         sb.append(", ");
         }
         sb.append(i.toString());
         }
         return sb.toString();
         }
         return null;
         }
         });*/
        table.addGeneratedColumn("screenshots", new ScreenShotsColumnGenerator(this));
        table.setCellStyleGenerator(new CustomCellStyleGenerator());
        table.setVisibleColumns(new Object[]{"nameEn", "screenshots", "category"});
        table.setColumnHeaders(new String[]{i18n.item(), i18n.screenshots(), i18n.category()});
        table.setColumnExpandRatio("screenshots", 1f);
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
                applyFilters();
            }
        }

    }

    private class CustomCellStyleGenerator implements Table.CellStyleGenerator {

        @Override
        public String getStyle(Table source, Object itemId, Object propertyId) {

            if (propertyId != null && (propertyId.equals("nameEn") || propertyId.equals("nameDe") || propertyId.equals("nameFr") || propertyId.equals("nameRu"))) {
                EntityItem item = (EntityItem) source.getItem(itemId);
                FurnitureItem furnitureItem = (FurnitureItem) item.getEntity();
                return furnitureItem.getItemQuality().name().toLowerCase();
            }

            return null;
        }

    }

    private class ScreenShotsColumnGenerator implements Table.ColumnGenerator {

        private final FurnitureItemsOldView furnitureItemsView;

        public ScreenShotsColumnGenerator(FurnitureItemsOldView furnitureItemsView_) {
            this.furnitureItemsView = furnitureItemsView_;
        }

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSizeFull();
            hl.setSpacing(true);
            EntityItem item = (EntityItem) source.getItem(itemId);
            final FurnitureItem furnitureItem = (FurnitureItem) item.getEntity();
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
                counter++;
                if (counter > 2) {
                    break;
                }
            }
            if (SpringSecurityHelper.hasRole("ROLE_UPLOAD_SCREENSHOTS")) {
                Button uploadScreenShot = new Button(FontAwesome.PLUS);
                uploadScreenShot.setData(furnitureItem);
                uploadScreenShot.addClickListener(new Button.ClickListener() {

                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        UploadScreenshotWindow window = new UploadScreenshotWindow(furnitureItem, furnitureItemsView);
                        getUI().addWindow(window);

                    }
                });
                hl.addComponent(uploadScreenShot);
            }

            return hl;
        }

    }

    public void refreshFurnitureItem(FurnitureItem item) {
        try {
            container.refreshItem(item.getId());
        } catch (Exception ex) {

        }
    }

    private class ScreenshotClickListener implements MouseEvents.ClickListener {

        @Override
        public void click(MouseEvents.ClickEvent event) {
            Image i = (Image) event.getComponent();
            final ItemScreenshot sc = (ItemScreenshot) i.getData();
            ScreenShotViewWindwow window = new ScreenShotViewWindwow(sc.getFurnitureItem(), sc);
            getUI().addWindow(window);
        }

    }

    private class ScreenShotViewWindwow extends Window {

        private final FurnitureItem item;
        private final ItemScreenshot screenshot;

        private VerticalLayout vl;
        private Panel panel;
        private HorizontalLayout thumbs;
        private Panel thumbsPanel;

        public ScreenShotViewWindwow(FurnitureItem item_, ItemScreenshot screenshot_) {
            setWidth(1050f, Unit.PIXELS);
            setHeight(800f, Unit.PIXELS);
            setClosable(true);
            setModal(true);
            setDraggable(false);
            setResizable(false);
            center();
            this.item = item_;
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
            for (final ItemScreenshot s : item_.getItemScreenshots()) {
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
            screenshotImage.setAlternateText(i18n.screenshotAlternativeText(s.getFileName(),s.getAuthor().getEsoId()));
            panel.setContent(screenshotImage);
        }

    }

    private class UploadScreenshotWindow extends Window implements Upload.Receiver, Upload.SucceededListener, LiveImageEditor.ImageReceiver, Button.ClickListener {

        private final Upload upload;
        private final FurnitureItem item;
        private Panel editorPanel;
        private final FurnitureItemsOldView furnitureItemsView;
        private LiveImageEditor liveImageEditor;
        private Button saveImage;
        private ByteArrayOutputStream baos;
        private String filename;

        public UploadScreenshotWindow(FurnitureItem item_, FurnitureItemsOldView furnitureItemsView_) {

            VerticalLayout vl = new VerticalLayout();
            this.item = item_;
            this.furnitureItemsView = furnitureItemsView_;
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
                dBService.saveEntity(screenshot);
                furnitureItemsView.refreshFurnitureItem(item);
                this.close();
            } catch (IOException ex) {
                Logger.getLogger(FurnitureItemsOldView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void buttonClick(Button.ClickEvent event) {
            liveImageEditor.requestEditedImage();
            saveImage.setEnabled(false);
        }

    }

}
