/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

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

    public Header() {
        setWidth(100f, Unit.PERCENTAGE);
        setHeight(40f, Unit.PIXELS);

        menuBar = new MenuBar();

    }

    public void build() {
        SysAccount user = SpringSecurityHelper.getUser();
        if (SpringSecurityHelper.hasRole("ROLE_ADMIN")) {
            menuBar.addItem("Import", new MenuBar.Command() {

                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(ImportView.NAME);
                }
            });
        }

        menuBar.addItem("Furniture Catalog", new MenuBar.Command() {

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(FurnitureItemsView.NAME);
            }
        });
        if (user!=null&&SpringSecurityHelper.hasRole("ROLE_USER")) {
        menuBar.addItem("Known Recipes", new MenuBar.Command() {

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(KnownRecipesView.NAME);
            }
        });
        }
        if (user!=null) {
            menuBar.addItem("Logout(" + user.getUsername() + ")", new MenuBar.Command() {

                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(LogoutView.NAME);
                }
            });
        } else {
            menuBar.addItem("Login", new MenuBar.Command() {

                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(LoginView.NAME);
                }
            });
        }
        addComponent(menuBar);
    }

}
