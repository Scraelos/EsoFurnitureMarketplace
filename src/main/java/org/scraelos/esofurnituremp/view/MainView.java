/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@ViewScope
@SpringView(name = MainView.NAME)
public class MainView extends CustomComponent implements View {

    public static final String NAME = "";

    private Header header;

    public MainView() {
        header = new Header();
        VerticalLayout vl = new VerticalLayout(header);
        vl.setSizeFull();
        setCompositionRoot(vl);
    }

    @Override
    public void enter(ViewChangeEvent event) {
        header.build();

    }

}
