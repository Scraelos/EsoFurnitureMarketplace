/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.scraelos.esofurnituremp.model.ItemCategory;
import org.scraelos.esofurnituremp.model.ItemSubCategory;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.xpoft.vaadin.VaadinView;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@VaadinView(FurnitureItemsView.NAME)
public class FurnitureItemsView extends CustomComponent implements View {

    public static final String NAME = "furniture";

    @Autowired
    private DBService dBService;

    private Tree tree;
    private Table table;
    private JPAContainer<FurnitureItem> container;

    public FurnitureItemsView() {
        this.setSizeFull();
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.addComponent(new Header());
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        tree = new Tree("Catergories");
        tree.setSizeFull();
        tree.setWidth(200f, Unit.PIXELS);
        tree.addItemClickListener(new TreeItemClickListener());
        hl.addComponent(tree);
        table=new Table("Items");
        table.setSizeFull();
        hl.addComponent(table);
        hl.setExpandRatio(table, 1f);
        vl.addComponent(hl);
        vl.setExpandRatio(hl, 1f);
        setCompositionRoot(vl);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        tree.setContainerDataSource(dBService.getItemCategories());
        container=dBService.getJPAContainerContainerForClass(FurnitureItem.class);
        table.setContainerDataSource(container);
    }
    
    private class TreeItemClickListener implements ItemClickEvent.ItemClickListener {

        @Override
        public void itemClick(ItemClickEvent event) {
            Object itemId = event.getItemId();
            if(itemId instanceof ItemCategory) {
                container.removeAllContainerFilters();
                container.addContainerFilter(new Compare.Equal("subCategory.category", itemId));
            } else if(itemId instanceof ItemSubCategory) {
                container.removeAllContainerFilters();
                container.addContainerFilter(new Compare.Equal("subCategory", itemId));
            }
        }
        
    }

}
