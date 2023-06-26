/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.github.peholmst.i18n4vaadin.LocaleChangedEvent;
import com.github.peholmst.i18n4vaadin.LocaleChangedListener;
import com.github.peholmst.i18n4vaadin.util.I18NHolder;
import com.vaadin.data.HasValue;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.StyleGenerator;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.scraelos.esofurnituremp.Bundle;
import org.scraelos.esofurnituremp.model.ESO_SERVER;
import org.scraelos.esofurnituremp.model.TopListItem;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author guest
 */
@Component
@Scope("prototype")
@SpringView(name = TopListView.NAME)
public class TopListView extends CustomComponent implements View, LocaleChangedListener {

    @Autowired
    private DBService service;
    public static final String NAME = "top";
    private ComboBox<ESO_SERVER> server;
    private Bundle i18n = new Bundle();
    private List<TopListItem> topList = new ArrayList<>();
    private String esoid;
    private BoldCellStyleGenerator styleGenerator = new BoldCellStyleGenerator();

    private final Grid<TopListItem> grid = new Grid<>(TopListItem.class);

    public TopListView() {
        this.setSizeFull();
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(false);
        vl.setSizeFull();
        server = new ComboBox(null, Arrays.asList(ESO_SERVER.values()));
        server.setEmptySelectionAllowed(false);
        server.addValueChangeListener(new HasValue.ValueChangeListener() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent event) {
                loadGrid();
            }
        });
        vl.addComponent(server);
        grid.setHeight(100f, Unit.PERCENTAGE);
        grid.setColumns("order", "esoId", "count");
        grid.getColumn("order").setSortable(false).setResizable(false).setStyleGenerator(styleGenerator);
        grid.getColumn("esoId").setSortable(false).setResizable(false).setStyleGenerator(styleGenerator);
        grid.getColumn("count").setSortable(false).setResizable(false).setStyleGenerator(styleGenerator);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setDataProvider(new ListDataProvider(topList));
        vl.addComponent(grid);
        vl.setExpandRatio(grid, 1f);
        setCompositionRoot(vl);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        getUI().getPage().setTitle(i18n.topList() + " | " + i18n.siteTitle());
        if (SpringSecurityHelper.getUser() != null) {
            server.setValue(SpringSecurityHelper.getUser().getEsoServer());
        } else {
            server.setValue(ESO_SERVER.EU);
        }
        loadGrid();
        localize(getUI().getLocale());
    }

    @Override
    public void attach() {
        super.attach();
        I18NHolder.get().addLocaleChangedListener(this);
    }

    @Override
    public void detach() {
        I18NHolder.get().removeLocaleChangedListener(this);
        super.detach();
    }

    private void loadGrid() {
        if (SpringSecurityHelper.getUser() != null && SpringSecurityHelper.getUser().getEsoId() != null) {
            esoid = SpringSecurityHelper.getUser().getEsoId();
        }
        service.loadTopList(this.topList, server.getValue(), esoid);
        grid.getDataProvider().refreshAll();
    }

    private void localize(Locale locale) {
        grid.getColumn("esoId").setCaption("ID");
        grid.getColumn("count").setCaption(i18n.knownCount());
        grid.getColumn("order").setCaption(i18n.rank());
        server.setCaption(i18n.server());
    }

    @Override
    public void localeChanged(LocaleChangedEvent lce) {
        localize(lce.getNewLocale());
    }

    private class BoldCellStyleGenerator implements StyleGenerator<TopListItem> {

        @Override
        public String apply(TopListItem item) {
            if (item.getEsoId().equals("@" + esoid)) {
                return "v-label-bold";
            }
            return null;
        }

    }

}
