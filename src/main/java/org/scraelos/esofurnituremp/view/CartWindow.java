/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.StyleGenerator;
import com.vaadin.ui.Window;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.TextRenderer;
import elemental.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import org.scraelos.esofurnituremp.Bundle;
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.scraelos.esofurnituremp.model.Ingredient;
import org.scraelos.esofurnituremp.model.IngredientCount;
import org.scraelos.esofurnituremp.model.RecipeIngredient;
import org.scraelos.esofurnituremp.model.SelectedFurniture;

/**
 *
 * @author guest
 */
public class CartWindow extends Window {

    private final Bundle i18n = new Bundle();
    private final VerticalLayout layout;
    private final Grid<SelectedFurniture> cartGrid;
    private final Grid<IngredientCount> materialsGrid;
    private final List<SelectedFurniture> cart = new ArrayList<>();
    private final List<IngredientCount> ingredients = new ArrayList<>();

    public CartWindow() {
        setClosable(true);
        setModal(true);
        setWidth(700f, Unit.PIXELS);
        setResizable(false);
        setCaption(i18n.cart());
        center();
        layout = new VerticalLayout();
        cartGrid = new Grid(SelectedFurniture.class);
        cartGrid.setSizeFull();
        cartGrid.setHeaderVisible(false);
        cartGrid.setDataProvider(new ListDataProvider(cart));
        cartGrid.addColumn(new ValueProvider<SelectedFurniture, String>() {
            @Override
            public String apply(SelectedFurniture source) {
                FurnitureItem value = source.getItem();
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

                return result;
            }
        }).setId("itemName").setStyleGenerator(new CustomCellStyleGenerator());

        cartGrid.addComponentColumn(new ValueProvider<SelectedFurniture, Button>() {
            @Override
            public Button apply(SelectedFurniture source) {
                Button button = new Button(VaadinIcons.PLUS);
                button.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        add(source.getItem());
                        LoadCart();
                    }
                });
                return button;
            }
        }).setId("add");
        cartGrid.addComponentColumn(new ValueProvider<SelectedFurniture, Button>() {
            @Override
            public Button apply(SelectedFurniture source) {
                Button button = new Button(VaadinIcons.MINUS);
                button.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        subtract(source.getItem());
                        LoadCart();
                    }
                });
                return button;
            }
        }).setId("subtract");
        cartGrid.setColumns("itemName", "count", "add", "subtract");
        layout.addComponent(cartGrid);

        materialsGrid = new Grid(IngredientCount.class);
        materialsGrid.setSizeFull();
        materialsGrid.setHeaderVisible(false);
        materialsGrid.setDataProvider(new ListDataProvider(ingredients));
        materialsGrid.addColumn(new ValueProvider<IngredientCount, String>() {
            @Override
            public String apply(IngredientCount source) {
                Ingredient value = source.getIngredient();
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

                return result;
            }
        }).setId("ingredientName");
        materialsGrid.setColumns("ingredientName", "count");
        layout.addComponent(materialsGrid);
        layout.setExpandRatio(cartGrid, 1f);
        layout.setExpandRatio(materialsGrid, 1f);
        setContent(layout);
    }

    public void LoadCart() {

        List<SelectedFurniture> sessionCart = (List<SelectedFurniture>) getUI().getSession().getAttribute("itemCart");
        if (sessionCart == null) {
            sessionCart = new ArrayList<>();
        }
        cart.clear();
        cart.addAll(sessionCart);
        cartGrid.getDataProvider().refreshAll();
        cartGrid.sort("itemName", SortDirection.ASCENDING);
        ingredients.clear();
        for (SelectedFurniture f : cart) {
            if (f.getItem().getRecipe() != null) {
                for (RecipeIngredient i : f.getItem().getRecipe().getRecipeIngredients()) {
                    boolean found = false;
                    for (IngredientCount c : ingredients) {
                        if (c.getIngredient().equals(i.getIngredient())) {
                            found = true;
                            c.add(i.getCount() * f.getCount());
                        }
                    }
                    if (!found) {
                        IngredientCount c = new IngredientCount();
                        c.setIngredient(i.getIngredient());
                        c.add(i.getCount() * f.getCount());
                        ingredients.add(c);
                    }
                }
            }
        }
        materialsGrid.getDataProvider().refreshAll();
        materialsGrid.sort("ingredientName", SortDirection.ASCENDING);

    }

    private void add(FurnitureItem item) {
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
    }

    private void subtract(FurnitureItem item) {
        List<SelectedFurniture> cart = (List<SelectedFurniture>) getUI().getSession().getAttribute("itemCart");
        if (cart == null) {
            cart = new ArrayList<>();
        }
        SelectedFurniture found = null;
        for (SelectedFurniture f : cart) {
            if (f.getItem().equals(item)) {
                found = f;
                f.subtract();
                break;
            }
        }
        if (found.getCount() == 0) {
            cart.remove(found);
        }
        getUI().getSession().setAttribute("itemCart", cart);
    }

    private class CustomCellStyleGenerator implements StyleGenerator<SelectedFurniture> {

        @Override
        public String apply(SelectedFurniture item) {
            if (item != null) {
                return item.getItem().getItemQuality().name().toLowerCase();
            }
            return null;
        }

    }

}
