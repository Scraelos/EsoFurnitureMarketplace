package org.scraelos.esofurnituremp.view;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringView;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@SpringView(name = LogoutView.NAME)
@Secured({"ROLE_USER"})
public class LogoutView extends Navigator.EmptyView {

    public static final String NAME = "logout";

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        SecurityContextHolder.clearContext();
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        VaadinSession.getCurrent().close();
        ExternalResource resource = new ExternalResource("");
        Page.getCurrent().open(resource.getURL(), null);
    }
}
