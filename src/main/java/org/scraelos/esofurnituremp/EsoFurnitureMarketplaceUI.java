/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp;

import com.github.peholmst.i18n4vaadin.I18N;
import com.github.peholmst.i18n4vaadin.annotations.Message;
import com.github.peholmst.i18n4vaadin.annotations.Messages;
import com.github.peholmst.i18n4vaadin.simple.I18NProvidingUIStrategy;
import com.github.peholmst.i18n4vaadin.simple.SimpleI18N;
import com.github.peholmst.i18n4vaadin.util.I18NHolder;
import com.github.peholmst.i18n4vaadin.util.I18NProvider;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.EnableVaadin;
import com.vaadin.spring.annotation.EnableVaadinNavigation;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.server.SpringVaadinServlet;
import com.vaadin.ui.UI;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import org.scraelos.esofurnituremp.security.SecurityErrorHandler;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoaderListener;

/**
 *
 * @author scraelos
 */
@Component
@Theme("demo")
@UIScope
@SpringUI
public class EsoFurnitureMarketplaceUI extends UI implements I18NProvider {

    @Autowired
    private transient ApplicationContext applicationContext;

    private I18N i18n = new SimpleI18N(Arrays.asList(new Locale("en"), new Locale("ru"), new Locale("fr"), new Locale("de")));
    private static final Logger LOG = Logger.getLogger(EsoFurnitureMarketplaceUI.class.getName());

    static {
        I18NHolder.setStrategy(new I18NProvidingUIStrategy());
    }

    @Messages({
        @Message(key = "importDataFromCraftStoreButtonCaption", value = "Import data from CraftStore or FurnitureCatalogue"),
        @Message(key = "knownRecipesTableCaption", value = "Known Recipes"),
        @Message(key = "accessDenied", value = "<h1>Access Denied!</h1>"),
        @Message(key = "dontHavePermission", value = "You don't have required permission to access this resource."),
        @Message(key = "homePage", value = "Home"),
        @Message(key = "filters", value = "Filters"),
        @Message(key = "serverForCraftersSearch", value = "Search for crafters on this server"),
        @Message(key = "searchField", value = "Search string"),
        @Message(key = "searchFieldIgnoreFilters", value = "Ignore other filters"),
        @Message(key = "displayOnlyCraftable", value = "Display only craftable items"),
        @Message(key = "categories", value = "Catergories"),
        @Message(key = "furnitureListItemTableCaption", value = "Items - click on item name to display crafters"),
        @Message(key = "craftersTableCaption", value = "Crafters"),
        @Message(key = "item", value = "Item"),
        @Message(key = "category", value = "Category"),
        @Message(key = "screenshotAlternativeText", value = "{0} by @{1}"),
        @Message(key = "screenshots", value = "Screenshots"),
        @Message(key = "uploadScreenshotWindowCaption", value = "New Screenshot"),
        @Message(key = "uploadScreenshotUploadCaption", value = "Upload your screenshot"),
        @Message(key = "uploadScreenshotSaveCaption", value = "Save Screenshot"),
        @Message(key = "refreshTableCaption", value = "Refresh Table"),
        @Message(key = "adminSubmenuCaption", value = "Admin"),
        @Message(key = "importMenuItemCaption", value = "Import"),
        @Message(key = "usersMenuItemCaption", value = "Users"),
        @Message(key = "furnitureCatalogMenuItemCaption", value = "Furniture Catalog"),
        @Message(key = "knownRecipesMenuItemCaption", value = "Known Recipes"),
        @Message(key = "userProfileMenuItemCaption", value = "User Profile"),
        @Message(key = "logoutMenuItemCaption", value = "Logout({0})"),
        @Message(key = "loginMenuItemCaption", value = "Login"),
        @Message(key = "server", value = "Server"),
        @Message(key = "uploadCraftStoreFile", value = "Upload EfmpDump.lua from SavedVariables folder"),
        @Message(key = "importNewRecipes", value = "Import new recipes"),
        @Message(key = "newKnownRecipes", value = "Following recipes were added:"),
        @Message(key = "noNewKnownRecipes", value = "No new recipes were found"),
        @Message(key = "recipe", value = "Recipe"),
        @Message(key = "characterName", value = "Character Name"),
        @Message(key = "uploadErrorTitle", value = "UploadError"),
        @Message(key = "uploadErrorIDNotFound", value = "ID {0} not found in file!"),
        @Message(key = "uploadErrorNotFurnitureCatalogueOrCraftStore", value = "File contents must start with \"EfmpDump_SavedVariables\" line!"),
        @Message(key = "uploadDatamineXlsx", value = "Upload datamine xlsx"),
        @Message(key = "uploadEsoRawRecipeData", value = "Upload eso raw recipe data"),
        @Message(key = "localizedItemNameColumn", value = "nameEn"),
        @Message(key = "languageCaption", value = "Language"),
        @Message(key = "saveProfileCaption", value = "Save User Profile"),
        @Message(key = "oldPassword", value = "Current Password"),
        @Message(key = "newPassword", value = "New Password"),
        @Message(key = "newPasswordConfirm", value = "Repeat New Password"),
        @Message(key = "changePassword", value = "Change Password"),
        @Message(key = "useEnglishItemNames", value = "English item names"),
        @Message(key = "passwordsDoNotMatch", value = "Passwords do not match"),
        @Message(key = "wrongCurrentPassword", value = "Wrong current password"),
        @Message(key = "email", value = "E-mail"),
        @Message(key = "emailRepeat", value = "Repeat E-mail"),
        @Message(key = "emailPromt", value = "Your username (eg. joe@email.com)"),
        @Message(key = "emailRepeatPromt", value = "Repeat your E-mail"),
        @Message(key = "invalidUsername", value = "Username must be an email address"),
        @Message(key = "password", value = "Password"),
        @Message(key = "activeServer", value = "Active Server*"),
        @Message(key = "activeServerNotice", value = "*You will be able to change it in profile settings"),
        @Message(key = "ingameId", value = "Ingame id without @"),
        @Message(key = "ingameIdPromt", value = "Your ingame id without @"),
        @Message(key = "registerAndLogin", value = "Register & Login"),
        @Message(key = "loginTab", value = "Login"),
        @Message(key = "registerTab", value = "Register"),
        @Message(key = "authErrorCaption", value = "Authentication error"),
        @Message(key = "authErrorDescription", value = "Could not authenticate"),
        @Message(key = "passwordRepeat", value = "Repeat Password"),
        @Message(key = "registrationErrorCaption", value = "Registration error"),
        @Message(key = "itemQualityCaption", value = "Quality"),
        @Message(key = "gridItemSaveCaption", value = "Save"),
        @Message(key = "gridItemCancelCaption", value = "Cancel"),
        @Message(key = "itemLink", value = "Ingame Link"),
        @Message(key = "craftPrice", value = "Craft Price"),
        @Message(key = "craftPriceWithMats", value = "Craft Price with crafter's materials"),
        @Message(key = "materials", value = "Materials"),
        @Message(key = "nullPrice", value = "N/A"),
        @Message(key = "hasCrafters", value = "Only with crafters available"),
        @Message(key = "massPriceSetupNullCraftPrice", value = "Craft Price is not set"),
        @Message(key = "massPriceSetupNullCraftPriceWithMats", value = "Craft Price with crafter's materials is not set"),
        @Message(key = "wrongPriceMessage", value = "Wrong Price Value"),
        @Message(key = "applyPricesButton", value = "Apply Prices"),
        @Message(key = "massPriceSetup", value = "Mass Price Setup"),
        @Message(key = "recipeTypeCaption", value = "Recipe Type"),
        @Message(key = "ttcRecipeSearchItem", value = "Search recipe at TTC"),
        @Message(key = "ttcSearchItem", value = "Search item at TTC"),
        @Message(key = "unknownRecipes", value = "Unknown Recipe"),
        @Message(key = "itemInfo", value = "Item Info"),
        @Message(key = "crafterId", value = "Crafter's id(without @)"),
        @Message(key = "theme", value = "Theme"),
        @Message(key = "cart", value = "Cart"),
        @Message(key = "addToCart", value = "Add to Cart"),
        @Message(key = "topList", value = "Top 20"),
        @Message(key = "knownCount", value = "Known Recipes"),
        @Message(key = "rank", value = "Rank"),
        @Message(key = "siteTitle", value = "Elder Scrolls Online Furniture Database"),
        @Message(key = "clearKnownRecipes", value = "Clean Known Recipes"),
        @Message(key = "efmpDumpInfo", value = "Please install EfmpDump Addon, write /efmpdump in chat, then switch character or make /reloadui"),
        @Message(key = "efmpDumpDownload", value = "Download EfmpDump Addon"),})

    @WebListener
    public static class SpringContextLoaderListener extends ContextLoaderListener {
    }

    @Configuration
    @EnableVaadin
    @EnableVaadinNavigation
    public static class MyConfiguration {
    }

    @WebServlet(urlPatterns = "/*", name = "EsoFurnitureMarketplaceServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = EsoFurnitureMarketplaceUI.class, productionMode = true)
    public static class MyUIServlet extends SpringVaadinServlet {
    }

    @Autowired
    private EfmpSpringViewDisplay springViewDisplay;

    @Override
    protected void init(VaadinRequest request) {

        Navigator navigator = getNavigator();
        ErrorHandler defaultErrorHandler = VaadinSession.getCurrent().getErrorHandler();
        VaadinSession.getCurrent().setErrorHandler(new SecurityErrorHandler(navigator, defaultErrorHandler));
        Page.Styles styles = Page.getCurrent().getStyles();
        styles.add(".v-caption-legendary {"
                + "    color: #CCAA1A;"
                + "}"
                + ".v-caption-epic {"
                + "    color: #A02EF7;"
                + "}"
                + ".v-caption-superior {"
                + "    color: #3A92FF;"
                + "}"
                + ".v-caption-fine {"
                + "    color: #2DC50E;"
                + "}"
                + ".legendary {"
                + "    color: #CCAA1A;"
                + "}"
                + ".epic {"
                + "    color: #A02EF7;"
                + "}"
                + ".superior {"
                + "    color: #3A92FF;"
                + "}"
                + ".fine {"
                + "    color: #2DC50E;"
                + "}"
                + ".standard {"
                + "    color: #000000;"
                + "}"
                + ".v-caption-horizontal {"
                + "  display: inline-block;"
                + "}"
                + ".v-grid-cell {  "
                + "    line-height: 100px;"
                + "}"
                + ".my-grid .v-grid-body .v-grid-cell { height: 100px; }");
        String language = getLocale().getLanguage();
        if (!language.equals("en") && !language.equals("de") && !language.equals("fr") && !language.equals("ru")) {
            language = "en";
        }
        Locale lc = new Locale(language);
        if (SpringSecurityHelper.getUser() != null && SpringSecurityHelper.getUser().getUserLanguage() != null) {
            lc = SpringSecurityHelper.getUser().getUserLanguage().getLocale();
        }
        setLocale(lc);
        setContent(springViewDisplay);
    }

    @Override
    public I18N getI18N() {
        return i18n;
    }

    @Override
    public void setLocale(Locale locale) {
        Locale lc = new Locale(locale.getLanguage());
        i18n.setLocale(lc);
        super.setLocale(lc);
    }

}
