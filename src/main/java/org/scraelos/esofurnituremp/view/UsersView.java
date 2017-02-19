/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import ru.xpoft.vaadin.VaadinView;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@VaadinView(UsersView.NAME)
@Secured({"ROLE_ADMIN"})
public class UsersView extends CustomComponent implements View{
    public static final String NAME = "users";

    public UsersView() {
        Label label=new Label("hello!");
        VerticalLayout vl=new VerticalLayout(label);
        setCompositionRoot(vl);
    }

    
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        
    }
}
