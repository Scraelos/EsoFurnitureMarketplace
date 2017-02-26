/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.github.peholmst.i18n4vaadin.annotations.Message;
import com.github.peholmst.i18n4vaadin.annotations.Messages;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import org.scraelos.esofurnituremp.model.SysAccount;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
import org.scraelos.esofurnituremp.view.security.LoginView;

/**
 *
 * @author scraelos
 */
public class Header extends VerticalLayout {

    private MenuBar menuBar;
    private final Bundle i18n=new Bundle();

    public Header() {
        setWidth(100f, Unit.PERCENTAGE);
        setHeight(40f, Unit.PIXELS);

        menuBar = new MenuBar();

    }

    @Messages({
        @Message(key = "adminSubmenuCaption",value = "Admin"),
        @Message(key = "importMenuItemCaption",value = "Import"),
        @Message(key = "usersMenuItemCaption",value = "Users"),
        @Message(key = "furnitureCatalogMenuItemCaption",value = "Furniture Catalog"),
        @Message(key = "knownRecipesMenuItemCaption",value = "Known Recipes"),
        @Message(key = "userProfileMenuItemCaption",value = "User Profile"),
        @Message(key = "logoutMenuItemCaption",value = "Logout({0})"),
        @Message(key = "loginMenuItemCaption",value = "Login"),
    })
    public void build() {
        SysAccount user = SpringSecurityHelper.getUser();
        if (SpringSecurityHelper.hasRole("ROLE_ADMIN")) {
            MenuBar.MenuItem adminMenu = menuBar.addItem(i18n.adminSubmenuCaption(), null);
            adminMenu.addItem(i18n.importMenuItemCaption(), new MenuBar.Command() {

                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(ImportView.NAME);
                }
            });
            adminMenu.addItem(i18n.usersMenuItemCaption(), new MenuBar.Command() {

                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(UsersView.NAME);
                }
            });
        }

        menuBar.addItem(i18n.furnitureCatalogMenuItemCaption(), new MenuBar.Command() {

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(FurnitureItemsView.NAME);
            }
        });
        if (user != null && SpringSecurityHelper.hasRole("ROLE_USER")) {
            menuBar.addItem(i18n.knownRecipesMenuItemCaption(), new MenuBar.Command() {

                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(KnownRecipesView.NAME);
                }
            });
            menuBar.addItem(i18n.userProfileMenuItemCaption(), new MenuBar.Command() {

                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(UserProfileView.NAME);
                }
            });
        }
        if (user != null) {
            menuBar.addItem(i18n.logoutMenuItemCaption(user.getUsername()), new MenuBar.Command() {

                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(LogoutView.NAME);
                }
            });
        } else {
            menuBar.addItem(i18n.loginMenuItemCaption(), new MenuBar.Command() {

                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(LoginView.NAME);
                }
            });
        }
        addComponent(menuBar);
    }

}
