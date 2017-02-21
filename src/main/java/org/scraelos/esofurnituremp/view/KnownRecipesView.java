/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.scraelos.esofurnituremp.model.ItemCategory;
import org.scraelos.esofurnituremp.model.ItemSubCategory;
import org.scraelos.esofurnituremp.model.KnownRecipe;
import org.scraelos.esofurnituremp.model.RecipeIngredient;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import ru.xpoft.vaadin.VaadinView;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@VaadinView(KnownRecipesView.NAME)
@Secured({"ROLE_USER"})
public class KnownRecipesView extends CustomComponent implements View {

    public static final String NAME = "knownrecipes";
    private Header header;
    @Autowired
    private DBService dBService;

    private Tree tree;
    private Table table;
    private JPAContainer<KnownRecipe> container;
    private HorizontalLayout actions;
    private Button importButton;

    public KnownRecipesView() {
        header = new Header();
        this.setSizeFull();
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        header = new Header();
        vl.addComponent(header);
        actions = new HorizontalLayout();
        importButton = new Button("Import data from CraftStore", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getUI().getNavigator().navigateTo(ImportKnownRecipesView.NAME);
            }
        });
        actions.addComponent(importButton);
        vl.addComponent(actions);
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        tree = new Tree("Catergories");
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
        table = new Table("Items");
        table.setSizeFull();
        table.setCellStyleGenerator(new CustomCellStyleGenerator());
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
        container = dBService.getJPAContainerContainerForClass(KnownRecipe.class);
        applyFilters();
        table.setContainerDataSource(container);
        table.setVisibleColumns(new Object[]{"recipe", "characterName", "esoServer"});
        table.setColumnHeaders(new String[]{"Recipe", "Character", "Server"});

    }

    private void applyFilters() {
        container.removeAllContainerFilters();
        container.addContainerFilter(new Compare.Equal("account", SpringSecurityHelper.getUser()));
    }

    private class TreeItemClickListener implements ItemClickEvent.ItemClickListener {

        @Override
        public void itemClick(ItemClickEvent event) {
            Object itemId = event.getItemId();
            if (itemId instanceof ItemCategory) {
                tree.expandItem(itemId);
            } else if (itemId instanceof ItemSubCategory) {
                container.removeAllContainerFilters();
                container.addContainerFilter(new Compare.Equal("recipe.furnitureItem.subCategory", itemId));
            }
        }

    }

    private class CustomCellStyleGenerator implements Table.CellStyleGenerator {

        @Override
        public String getStyle(Table source, Object itemId, Object propertyId) {
            String result = null;
            if (propertyId != null && propertyId.equals("recipe")) {
                EntityItem item = (EntityItem) source.getItem(itemId);
                KnownRecipe recipe = (KnownRecipe) item.getEntity();
                if (recipe.getRecipe().getItemQuality() != null) {
                    return recipe.getRecipe().getItemQuality().name().toLowerCase();
                }
            }

            return result;
        }

    }

}
