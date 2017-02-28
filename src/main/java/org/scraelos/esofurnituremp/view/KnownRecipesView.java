/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import org.scraelos.esofurnituremp.Bundle;
import org.scraelos.esofurnituremp.data.KnownRecipeRepository;
import org.scraelos.esofurnituremp.data.KnownRecipeSpecification;
import org.scraelos.esofurnituremp.model.ItemCategory;
import org.scraelos.esofurnituremp.model.ItemSubCategory;
import org.scraelos.esofurnituremp.model.KnownRecipe;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
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
public class KnownRecipesView extends CustomComponent implements View {

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
    private GeneratedPropertyListContainer<KnownRecipe> listContainer=new GeneratedPropertyListContainer(KnownRecipe.class);
    private KnownRecipeSpecification specification;
    static final int PAGESIZE = 45;

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
        tree = new Tree(i18n.categories());
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
        grid = new Grid(i18n.knownRecipesTableCaption());
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
        specification = new KnownRecipeSpecification(SpringSecurityHelper.getUser());
        tree.setContainerDataSource(dBService.getItemCategories());
        grid.setContainerDataSource(listContainer);
        grid.setColumns(new Object[]{"recipe", "characterName", "esoServer"});
        grid.getColumn("recipe").setHeaderCaption(i18n.recipe());
        grid.getColumn("characterName").setHeaderCaption(i18n.characterName());
        grid.getColumn("esoServer").setHeaderCaption(i18n.server());
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

}
