/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import org.springframework.context.annotation.Scope;
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

    private Header header;

    public MainView() {
        header = new Header();
        setCompositionRoot(new CssLayout(header));
    }

    @Override
    public void enter(ViewChangeEvent event) {
        header.build();

    }

}
