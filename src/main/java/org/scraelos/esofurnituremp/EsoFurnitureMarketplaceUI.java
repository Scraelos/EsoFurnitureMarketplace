/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp;

import com.github.peholmst.i18n4vaadin.I18N;
import com.github.peholmst.i18n4vaadin.simple.I18NProvidingUIStrategy;
import com.github.peholmst.i18n4vaadin.simple.SimpleI18N;
import com.github.peholmst.i18n4vaadin.util.I18NHolder;
import com.github.peholmst.i18n4vaadin.util.I18NProvider;
import com.vaadin.annotations.Theme;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import java.util.Arrays;
import java.util.Locale;
import javax.servlet.ServletException;
import org.scraelos.esofurnituremp.security.SecurityErrorHandler;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.xpoft.vaadin.DiscoveryNavigator;

/**
 *
 * @author scraelos
 */
@Component
@Theme("valo")
@Scope("prototype")
public class EsoFurnitureMarketplaceUI extends UI implements I18NProvider {

    @Autowired
    private transient ApplicationContext applicationContext;

    private I18N i18n = new SimpleI18N(Arrays.asList(new Locale("en")));

    static {
        I18NHolder.setStrategy(new I18NProvidingUIStrategy());
    }

    @Override
    protected void init(VaadinRequest request) {
        setSizeFull();
        DiscoveryNavigator navigator = new DiscoveryNavigator(this, this);
        ErrorHandler defaultErrorHandler = VaadinSession.getCurrent().getErrorHandler();
        VaadinSession.getCurrent().setErrorHandler(new SecurityErrorHandler(navigator, defaultErrorHandler));
        Page.Styles styles = Page.getCurrent().getStyles();
        styles.add(".v-table-cell-content-legendary {"
                + "    color: #CCAA1A;"
                + "}"
                + ".v-table-cell-content-epic {"
                + "    color: #A02EF7;"
                + "}"
                + ".v-table-cell-content-superior {"
                + "    color: #3A92FF;"
                + "}"
                + ".v-table-cell-content-fine {"
                + "    color: #2DC50E;"
                + "}"
                + ".v-caption-horizontal {"
                + "  display: inline-block;"
                + "}");

    }

    @Override
    public I18N getI18N() {
        return i18n;
    }

}
