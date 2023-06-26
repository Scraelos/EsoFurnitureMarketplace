/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.github.peholmst.i18n4vaadin.LocaleChangedEvent;
import com.github.peholmst.i18n4vaadin.LocaleChangedListener;
import com.github.peholmst.i18n4vaadin.util.I18NHolder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringViewDisplay;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ItemCaptionGenerator;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Arrays;
import org.scraelos.esofurnituremp.Bundle;
import org.scraelos.esofurnituremp.model.ONLINE_STATUS;
import org.scraelos.esofurnituremp.model.SysAccount;
import org.scraelos.esofurnituremp.model.USER_LANGUAGE;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
import org.scraelos.esofurnituremp.service.DBService;
import org.scraelos.esofurnituremp.view.security.LoginView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author scraelos
 */
@Component
@Scope(value = "vaadin-ui")

public class Header extends VerticalLayout implements LocaleChangedListener {

    private final MenuBar menuBar;
    private final Bundle i18n = new Bundle();
    private final ComboBox languageBox;
    private final ComboBox<ONLINE_STATUS> statusBox;
    private final CheckBox useEnglishNamesBox;
    private FormLayout statusLayout;
    private FormLayout itemNamesLayout;
    private FormLayout languageLayout;

    @Autowired
    private DBService dBService;

    private MenuBar.MenuItem adminMenu;
    private MenuBar.MenuItem importMenuItem;
    private MenuBar.MenuItem usersMenuItem;
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
        this.setSpacing(false);
        this.setMargin(false);
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(false);
        hl.setMargin(false);
        hl.setSizeFull();
        menuBar = new MenuBar();
        languageBox = new ComboBox(null, Arrays.asList(USER_LANGUAGE.values()));
        languageBox.setHeight(40f, Unit.PIXELS);
        languageBox.setIcon(FontAwesome.LANGUAGE);
        languageBox.setEmptySelectionAllowed(false);
        languageBox.addValueChangeListener(event -> updateLocale());
        languageLayout = new FormLayout(languageBox);
        languageLayout.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        languageLayout.setHeight(40f, Unit.PIXELS);
        languageLayout.setWidth(175f, Unit.PIXELS);
        statusBox = new ComboBox(null, Arrays.asList(ONLINE_STATUS.values()));
        statusBox.setHeight(40f, Unit.PIXELS);
        statusBox.setIcon(FontAwesome.GAMEPAD);
        statusBox.setEmptySelectionAllowed(false);
        statusBox.addValueChangeListener(event -> {
            dBService.setStatus(SpringSecurityHelper.getUser(), event.getValue());
        });
        statusLayout = new FormLayout(statusBox);
        statusLayout.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        statusLayout.setHeight(40f, Unit.PIXELS);
        statusLayout.setWidth(175f, Unit.PIXELS);
        useEnglishNamesBox = new CheckBox();
        useEnglishNamesBox.addValueChangeListener(event -> updateLocale());
        itemNamesLayout = new FormLayout(useEnglishNamesBox);
        itemNamesLayout.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        itemNamesLayout.setHeight(40f, Unit.PIXELS);
        itemNamesLayout.setWidth(250f, Unit.PIXELS);
        adminMenu = menuBar.addItem("", FontAwesome.COGS, null);
        importMenuItem = adminMenu.addItem("", new MenuBar.Command() {

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(ImportView.NAME);
            }
        });
        usersMenuItem = adminMenu.addItem("", FontAwesome.USERS, new MenuBar.Command() {

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(UsersView.NAME);
            }
        });
        furnitureCatalogMenuItem = menuBar.addItem("", FontAwesome.TABLE, new MenuBar.Command() {

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(FurnitureItemsView.NAME);
            }
        });
        knownRecipesMenuItem = menuBar.addItem("", FontAwesome.BOOKMARK, new MenuBar.Command() {

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(KnownRecipesView.NAME);
            }
        });
        userProfileMenuItem = menuBar.addItem("", FontAwesome.USER, new MenuBar.Command() {

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(UserProfileView.NAME);
            }
        });
        cartMenuItem = menuBar.addItem("", VaadinIcons.CART, new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                openCart();
            }
        });
        topMenuItem = menuBar.addItem("", VaadinIcons.BAR_CHART, new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(TopListView.NAME);
            }
        });
        logoutMenuItem = menuBar.addItem("", FontAwesome.SIGN_OUT, new MenuBar.Command() {

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(LogoutView.NAME);
            }
        });
        loginMenuItem = menuBar.addItem("", FontAwesome.SIGN_IN, new MenuBar.Command() {

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(LoginView.NAME);
            }
        });

        menuBar.setSizeFull();
        hl.addComponent(menuBar);
        hl.addComponent(languageLayout);
        hl.setComponentAlignment(languageLayout, Alignment.TOP_RIGHT);
        hl.addComponent(statusLayout);
        hl.setComponentAlignment(statusLayout, Alignment.TOP_RIGHT);
        hl.addComponent(itemNamesLayout);
        hl.setComponentAlignment(itemNamesLayout, Alignment.TOP_RIGHT);
        hl.setExpandRatio(menuBar, 1f);
        addComponent(hl);
    }

    public void build() {
        SysAccount user = SpringSecurityHelper.getUser();
        if (SpringSecurityHelper.hasRole("ROLE_ADMIN")) {
            adminMenu.setVisible(true);
            importMenuItem.setVisible(true);
            usersMenuItem.setVisible(true);

        } else {
            adminMenu.setVisible(false);
            importMenuItem.setVisible(false);
            usersMenuItem.setVisible(false);
        }

        if (user != null && SpringSecurityHelper.hasRole("ROLE_USER")) {
            knownRecipesMenuItem.setVisible(true);
            userProfileMenuItem.setVisible(true);
        } else {
            knownRecipesMenuItem.setVisible(false);
            userProfileMenuItem.setVisible(false);
        }

        if (user != null) {
            logoutMenuItem.setText(i18n.logoutMenuItemCaption(user.getUsername()));
            logoutMenuItem.setVisible(true);
            loginMenuItem.setVisible(false);
            statusLayout.setVisible(true);
            statusBox.setValue(dBService.getStatus(user));
        } else {
            logoutMenuItem.setVisible(false);
            loginMenuItem.setVisible(true);
            statusLayout.setVisible(false);
        }
        languageBox.setValue(USER_LANGUAGE.getLanguageByLocale(getUI().getLocale()));
        Boolean useEnglishNames = (Boolean) getUI().getSession().getAttribute("useEnglishNames");
        if (useEnglishNames != null) {
            useEnglishNamesBox.setValue(useEnglishNames);
        }
        localize();
    }

    private void openCart() {
        CartWindow w = new CartWindow();
        getUI().addWindow(w);
        w.LoadCart();
    }

    private void localize() {
        statusBox.setItemCaptionGenerator(new ItemCaptionGenerator<ONLINE_STATUS>() {
            @Override
            public String apply(ONLINE_STATUS status) {
                switch (getLocale().getLanguage()) {
                    case "en":
                        return status.getNameEn();
                    case "de":
                        return status.getNameDe();
                    case "fr":
                        return status.getNameFr();
                    case "ru":
                        return status.getNameRu();
                }
                return null;
            }
        });
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
        if (logoutMenuItem != null && SpringSecurityHelper.getUser() != null) {
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

    private void updateLocale() {
        getUI().getSession().setAttribute("useEnglishNames", useEnglishNamesBox.getValue());
        getUI().setLocale(((USER_LANGUAGE) languageBox.getValue()).getLocale());
    }

    @Override
    public void attach() {
        super.attach();
        I18NHolder.get().addLocaleChangedListener(this);
    }

    @Override
    public void detach() {
        I18NHolder.get().removeLocaleChangedListener(this);
        super.detach();
    }

    @Override
    public void localeChanged(LocaleChangedEvent lce) {
        if (getUI().getLocale() != null) {
            USER_LANGUAGE newLocale = USER_LANGUAGE.getLanguageByLocale(lce.getNewLocale());
            if (newLocale != languageBox.getValue()) {
                languageBox.setValue(USER_LANGUAGE.getLanguageByLocale(lce.getNewLocale()));
            }
        }
        localize();
    }

}
