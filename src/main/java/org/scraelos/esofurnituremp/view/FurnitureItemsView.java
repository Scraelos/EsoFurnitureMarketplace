/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

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
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Arrays;
import org.scraelos.esofurnituremp.model.ESO_SERVER;
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.scraelos.esofurnituremp.model.ItemCategory;
import org.scraelos.esofurnituremp.model.ItemSubCategory;
import org.scraelos.esofurnituremp.model.RecipeIngredient;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
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

    public FurnitureItemsView() {
        this.setSizeFull();
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        header = new Header();
        vl.addComponent(header);
        HorizontalLayout filters = new HorizontalLayout();
        filters.setCaption("Filters");
        server = new ComboBox("Search for crafters on this server", Arrays.asList(ESO_SERVER.values()));
        server.setNullSelectionAllowed(false);

        filters.addComponent(server);

        searchField = new TextField("Search string (overrides other filters)");
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
        onlyCraftable = new CheckBox("Display only craftable items", false);
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
        tree = new Tree("Catergories");
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
        table = new Table("Items - click on item to display crafters");
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
        craftersTable = new Table("Crafters");
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
        table.addGeneratedColumn("ingredients", new Table.ColumnGenerator() {

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
        });
        table.setCellStyleGenerator(new CustomCellStyleGenerator());
        table.setVisibleColumns(new Object[]{"nameEn", "category", "ingredients"});
        table.setColumnHeaders(new String[]{"Item", "Category", "Ingredients"});
        table.setColumnExpandRatio("ingredients", 1f);
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

            if (propertyId != null && propertyId.equals("nameEn")) {
                EntityItem item = (EntityItem) source.getItem(itemId);
                FurnitureItem furnitureItem = (FurnitureItem) item.getEntity();
                return furnitureItem.getItemQuality().name().toLowerCase();
            }

            return null;
        }

    }

}
