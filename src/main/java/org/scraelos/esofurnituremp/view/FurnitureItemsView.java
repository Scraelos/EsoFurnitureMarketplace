/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import java.util.Locale;
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.scraelos.esofurnituremp.model.ItemCategory;
import org.scraelos.esofurnituremp.model.ItemSubCategory;
import org.scraelos.esofurnituremp.model.RecipeIngredient;
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
    private JPAContainer<FurnitureItem> container;

    public FurnitureItemsView() {
        this.setSizeFull();
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        header = new Header();
        vl.addComponent(header);
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
        table = new Table("Items");
        table.setSizeFull();
        hl.addComponent(table);
        hl.setExpandRatio(table, 1f);
        vl.addComponent(hl);
        vl.setExpandRatio(hl, 1f);
        setCompositionRoot(vl);
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
                if(furnitureItem.getRecipe()!=null) {
                    StringBuilder sb=new StringBuilder();
                    for(RecipeIngredient i:furnitureItem.getRecipe().getRecipeIngredients()) {
                        if(!sb.toString().isEmpty()) {
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
        table.setVisibleColumns(new Object[]{"nameEn", "category","ingredients"});
        table.setColumnHeaders(new String[]{"nameEn", "category","ingredients"});
        table.setColumnExpandRatio("ingredients", 1f);
    }

    private class TreeItemClickListener implements ItemClickEvent.ItemClickListener {

        @Override
        public void itemClick(ItemClickEvent event) {
            Object itemId = event.getItemId();
            if (itemId instanceof ItemCategory) {
                tree.expandItem(itemId);
                //container.removeAllContainerFilters();
                //container.addContainerFilter(new Compare.Equal("subCategory.category", itemId));
            } else if (itemId instanceof ItemSubCategory) {
                container.removeAllContainerFilters();
                container.addContainerFilter(new Compare.Equal("subCategory", itemId));
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
