/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.data.Property;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Arrays;
import org.scraelos.esofurnituremp.Bundle;
import org.scraelos.esofurnituremp.model.SysAccount;
import org.scraelos.esofurnituremp.model.USER_LANGUAGE;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
import org.scraelos.esofurnituremp.view.security.LoginView;

/**
 *
 * @author scraelos
 */
public class Header extends VerticalLayout {

    private final MenuBar menuBar;
    private final Bundle i18n = new Bundle();
    private final ComboBox languageBox;

    public Header() {
        setWidth(100f, Unit.PERCENTAGE);
        setHeight(40f, Unit.PIXELS);
        menuBar = new MenuBar();
        languageBox = new ComboBox(null, Arrays.asList(USER_LANGUAGE.values()));
        languageBox.setHeight(40f, Unit.PIXELS);
        languageBox.setIcon(FontAwesome.LANGUAGE);

    }

    public void build() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        SysAccount user = SpringSecurityHelper.getUser();
        if (SpringSecurityHelper.hasRole("ROLE_ADMIN")) {
            MenuBar.MenuItem adminMenu = menuBar.addItem(i18n.adminSubmenuCaption(), null);
            adminMenu.addItem(i18n.importMenuItemCaption(), new MenuBar.Command() {

                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(ImportView.NAME);
                }
            });
            adminMenu.addItem(i18n.usersMenuItemCaption(),FontAwesome.USERS, new MenuBar.Command() {

                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(UsersView.NAME);
                }
            });
        }

        menuBar.addItem(i18n.furnitureCatalogMenuItemCaption(),FontAwesome.TABLE, new MenuBar.Command() {

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(FurnitureItemsView.NAME);
            }
        });
        if (user != null && SpringSecurityHelper.hasRole("ROLE_USER")) {
            menuBar.addItem(i18n.knownRecipesMenuItemCaption(),FontAwesome.BOOKMARK, new MenuBar.Command() {

                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(KnownRecipesView.NAME);
                }
            });
            menuBar.addItem(i18n.userProfileMenuItemCaption(),FontAwesome.USER, new MenuBar.Command() {

                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(UserProfileView.NAME);
                }
            });
        }
        if (user != null) {
            menuBar.addItem(i18n.logoutMenuItemCaption(user.getUsername()),FontAwesome.SIGN_OUT, new MenuBar.Command() {

                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(LogoutView.NAME);
                }
            });
        } else {
            menuBar.addItem(i18n.loginMenuItemCaption(),FontAwesome.SIGN_IN, new MenuBar.Command() {

                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(LoginView.NAME);
                }
            });
        }
        menuBar.setSizeFull();
        hl.addComponent(menuBar);

        if (getUI().getLocale() != null) {
            languageBox.setValue(USER_LANGUAGE.getLanguageByLocale(getUI().getLocale()));
        }
        languageBox.setNullSelectionAllowed(false);
        languageBox.addValueChangeListener((Property.ValueChangeEvent event) -> {
            if (languageBox.getValue() != null) {
                getUI().setLocale(((USER_LANGUAGE) languageBox.getValue()).getLocale());
                getUI().getNavigator().navigateTo(getUI().getNavigator().getState());
            }
        });
        FormLayout languageLayout=new FormLayout(languageBox);
        languageLayout.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        languageLayout.setHeight(40f, Unit.PIXELS);
        languageLayout.setWidth(200f, Unit.PIXELS);
        hl.addComponent(languageLayout);
        hl.setComponentAlignment(languageLayout, Alignment.TOP_RIGHT);
        hl.setExpandRatio(menuBar, 1f);
        addComponent(hl);
    }

}
