/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.v7.data.Property;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.v7.ui.VerticalLayout;
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
    private final CheckBox useEnglishNamesBox;
    
    private MenuBar.MenuItem adminMenu;
    private MenuBar.MenuItem importMenuItem;
    private MenuBar.MenuItem usersMenuItem;
    private MenuBar.MenuItem systemPropertiesMenuItem;
    private MenuBar.MenuItem furnitureCatalogMenuItem;
    private MenuBar.MenuItem knownRecipesMenuItem;
    private MenuBar.MenuItem userProfileMenuItem;
    private MenuBar.MenuItem logoutMenuItem;
    private MenuBar.MenuItem loginMenuItem;
    private MenuBar.MenuItem cartMenuItem;
    private MenuBar.MenuItem topMenuItem;
    
    public Header() {
        setWidth(100f, Unit.PERCENTAGE);
        setHeight(40f, Unit.PIXELS);
        menuBar = new MenuBar();
        languageBox = new ComboBox(null, Arrays.asList(USER_LANGUAGE.values()));
        languageBox.setHeight(40f, Unit.PIXELS);
        languageBox.setIcon(FontAwesome.LANGUAGE);
        useEnglishNamesBox = new CheckBox();
        
    }
    
    public void build() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        SysAccount user = SpringSecurityHelper.getUser();
        if (SpringSecurityHelper.hasRole("ROLE_ADMIN")) {
            adminMenu = menuBar.addItem(i18n.adminSubmenuCaption(), FontAwesome.COGS, null);
            importMenuItem = adminMenu.addItem(i18n.importMenuItemCaption(), new MenuBar.Command() {
                
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(ImportView.NAME);
                }
            });
            usersMenuItem = adminMenu.addItem(i18n.usersMenuItemCaption(), FontAwesome.USERS, new MenuBar.Command() {
                
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(UsersView.NAME);
                }
            });
            systemPropertiesMenuItem = adminMenu.addItem("System Properties", FontAwesome.GEARS, new MenuBar.Command() {
                
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(SystemPropertiesView.NAME);
                }
            });
        }
        
        furnitureCatalogMenuItem = menuBar.addItem(i18n.furnitureCatalogMenuItemCaption(), FontAwesome.TABLE, new MenuBar.Command() {
            
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(FurnitureItemsView.NAME);
            }
        });
        if (user != null && SpringSecurityHelper.hasRole("ROLE_USER")) {
            knownRecipesMenuItem = menuBar.addItem(i18n.knownRecipesMenuItemCaption(), FontAwesome.BOOKMARK, new MenuBar.Command() {
                
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(KnownRecipesView.NAME);
                }
            });
            userProfileMenuItem = menuBar.addItem(i18n.userProfileMenuItemCaption(), FontAwesome.USER, new MenuBar.Command() {
                
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(UserProfileView.NAME);
                }
            });
        }
        cartMenuItem = menuBar.addItem(i18n.cart(), VaadinIcons.CART, new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                openCart();
            }
        });
        topMenuItem = menuBar.addItem(i18n.topList(), VaadinIcons.BAR_CHART, new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(TopListView.NAME);
            }
        });
        if (user != null) {
            logoutMenuItem = menuBar.addItem(i18n.logoutMenuItemCaption(user.getUsername()), FontAwesome.SIGN_OUT, new MenuBar.Command() {
                
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(LogoutView.NAME);
                }
            });
        } else {
            loginMenuItem = menuBar.addItem(i18n.loginMenuItemCaption(), FontAwesome.SIGN_IN, new MenuBar.Command() {
                
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
            updateLocale();
        });
        FormLayout languageLayout = new FormLayout(languageBox);
        languageLayout.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        languageLayout.setHeight(40f, Unit.PIXELS);
        languageLayout.setWidth(175f, Unit.PIXELS);
        hl.addComponent(languageLayout);
        hl.setComponentAlignment(languageLayout, Alignment.TOP_RIGHT);
        useEnglishNamesBox.setCaption(i18n.useEnglishItemNames());
        
        Boolean useEnglishNames = (Boolean) getUI().getSession().getAttribute("useEnglishNames");
        if (useEnglishNames != null) {
            useEnglishNamesBox.setValue(useEnglishNames);
        }
        useEnglishNamesBox.addValueChangeListener(new Property.ValueChangeListener() {
            
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                updateLocale();
            }
        });
        FormLayout itemNamesLayout = new FormLayout(useEnglishNamesBox);
        itemNamesLayout.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        itemNamesLayout.setHeight(40f, Unit.PIXELS);
        itemNamesLayout.setWidth(250f, Unit.PIXELS);
        hl.addComponent(itemNamesLayout);
        hl.setComponentAlignment(itemNamesLayout, Alignment.TOP_RIGHT);
        hl.setExpandRatio(menuBar, 1f);
        addComponent(hl);
    }
    
    private void openCart() {
        CartWindow w = new CartWindow();
        getUI().addWindow(w);
        w.LoadCart();
    }
    
    private void updateLocale() {
        getUI().getSession().setAttribute("useEnglishNames", useEnglishNamesBox.getValue());
        getUI().setLocale(((USER_LANGUAGE) languageBox.getValue()).getLocale());
        
        useEnglishNamesBox.setCaption(i18n.useEnglishItemNames());
        if (adminMenu != null) {
            adminMenu.setText(i18n.adminSubmenuCaption());
        }
        if (importMenuItem != null) {
            importMenuItem.setText(i18n.importMenuItemCaption());
        }
        if (usersMenuItem != null) {
            usersMenuItem.setText(i18n.usersMenuItemCaption());
        }
        if (furnitureCatalogMenuItem != null) {
            furnitureCatalogMenuItem.setText(i18n.furnitureCatalogMenuItemCaption());
        }
        if (knownRecipesMenuItem != null) {
            knownRecipesMenuItem.setText(i18n.knownRecipesMenuItemCaption());
        }
        if (userProfileMenuItem != null) {
            userProfileMenuItem.setText(i18n.userProfileMenuItemCaption());
        }
        if (logoutMenuItem != null) {
            logoutMenuItem.setText(i18n.logoutMenuItemCaption(SpringSecurityHelper.getUser().getUsername()));
        }
        if (loginMenuItem != null) {
            loginMenuItem.setText(i18n.loginMenuItemCaption());
        }
        if (cartMenuItem != null) {
            cartMenuItem.setText(i18n.cart());
        }
        if (topMenuItem != null) {
            topMenuItem.setText(i18n.topList());
        }
        
    }
    
}
