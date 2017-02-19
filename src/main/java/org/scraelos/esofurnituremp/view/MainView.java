/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.xpoft.vaadin.VaadinView;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@VaadinView(MainView.NAME)
public class MainView extends CustomComponent implements View {

    public static final String NAME = "";

    Label text = new Label();

    Button login = new Button("Login", new Button.ClickListener() {

        @Override
        public void buttonClick(ClickEvent event) {
            getUI().getNavigator().navigateTo("login");
        }
    });

    Button users = new Button("users", new Button.ClickListener() {

        @Override
        public void buttonClick(ClickEvent event) {
            getUI().getNavigator().navigateTo("users");
        }
    });

    Button logout = new Button("Logout", new Button.ClickListener() {

        @Override
        public void buttonClick(ClickEvent event) {

            // "Logout" the user
            SecurityContextHolder.clearContext();
            SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
            VaadinSession.getCurrent().close();
            ExternalResource resource = new ExternalResource("");
            Page.getCurrent().open(resource.getURL(), null);
            //getUI().getNavigator().navigateTo("/");
        }
    });

    public MainView() {
        setCompositionRoot(new CssLayout(new Header(),text, users, login, logout));
    }

    @Override
    public void enter(ViewChangeEvent event) {
        // Get the user name from the session
        String username = String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        // And show the username
        text.setValue("Hello " + username);
    }

}
