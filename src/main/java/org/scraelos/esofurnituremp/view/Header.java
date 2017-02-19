/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author scraelos
 */
public class Header extends VerticalLayout{

    public Header() {
        setWidth(100f, Unit.PERCENTAGE);
        setHeight(40f, Unit.PIXELS);
        
        MenuBar menuBar=new MenuBar();
        menuBar.addItem("Import", new MenuBar.Command() {

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(ImportView.NAME);
            }
        });
        menuBar.addItem("List Furniture", new MenuBar.Command() {

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(FurnitureItemsView.NAME);
            }
        });
        addComponent(menuBar);
        
        
    }
    
}
