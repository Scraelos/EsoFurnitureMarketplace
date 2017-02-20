package org.scraelos.esofurnituremp.view;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.xpoft.vaadin.VaadinView;

@Component
@Scope("prototype")
@VaadinView(LogoutView.NAME)
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
