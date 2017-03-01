package org.scraelos.esofurnituremp.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import org.scraelos.esofurnituremp.Bundle;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.xpoft.vaadin.VaadinView;

@Component
@Scope("prototype")
@VaadinView(AccessDeniedView.NAME)
public class AccessDeniedView extends CustomComponent implements View {

    public static final String NAME = "accessDenied";

    private Bundle bundle = new Bundle();

    
    public AccessDeniedView() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.addComponent(new Label(bundle.accessDenied(), ContentMode.HTML));
        vl.addComponent(new Label(bundle.dontHavePermission()));
        Link homeLink = new Link(bundle.homePage(), new ExternalResource("#"));
        homeLink.setIcon(FontAwesome.HOME);
        vl.addComponent(homeLink);
        setCompositionRoot(vl);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
    }

}