/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.spring.annotation.SpringViewDisplay;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Locale;
import java.util.logging.Logger;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
import org.scraelos.esofurnituremp.view.Header;
import org.scraelos.esofurnituremp.view.security.LoginView;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author scraelos
 */
@SuppressWarnings("WeakerAccess")
@SpringViewDisplay
public class EfmpSpringViewDisplay extends VerticalLayout implements ViewDisplay {

    @Autowired
    private Header header;
    private static final Logger LOG = Logger.getLogger(EfmpSpringViewDisplay.class.getName());
    private final Panel contentPanel;
    private final Panel headerPanel;
    private final Panel loginPanel;

    public EfmpSpringViewDisplay() {
        setSizeFull();
        setMargin(false);
        setSpacing(true);
        contentPanel = new Panel();
        contentPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
        headerPanel = new Panel();
        loginPanel = new Panel();
        addComponents(loginPanel, headerPanel, contentPanel);
        setExpandRatio(contentPanel, 1f);
        setExpandRatio(loginPanel, 1f);
        contentPanel.setSizeFull();
        loginPanel.setSizeFull();
    }

    @Override
    public void showView(View view) {
        if (view.getViewComponent() instanceof LoginView) {
            contentPanel.setVisible(false);
            headerPanel.setVisible(false);
            loginPanel.setVisible(true);
            loginPanel.setContent(view.getViewComponent());
        } else {
            contentPanel.setVisible(true);
            headerPanel.setVisible(true);
            loginPanel.setVisible(false);
            if (headerPanel.getContent() == null) {
                headerPanel.setContent(header);
            }
            header.build();
            contentPanel.setContent(view.getViewComponent());
        }
//        if (SpringSecurityHelper.getUser() != null && SpringSecurityHelper.getUser().getUserLanguage() != null) {
//            Locale lc = SpringSecurityHelper.getUser().getUserLanguage().getLocale();
//            getUI().setLocale(lc);
//            getUI().getSession().setAttribute("useEnglishNames", SpringSecurityHelper.getUser().getUseEnItemNames());
//        }
    }
}
