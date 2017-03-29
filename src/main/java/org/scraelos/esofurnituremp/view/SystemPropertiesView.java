/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.scraelos.esofurnituremp.data.SystemPropertyRepository;
import org.scraelos.esofurnituremp.model.SystemProperty;
import static org.scraelos.esofurnituremp.view.KnownRecipesView.PAGESIZE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import org.vaadin.viritin.grid.MGrid;
import ru.xpoft.vaadin.VaadinView;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@VaadinView(SystemPropertiesView.NAME)
@Secured({"ROLE_ADMIN"})
public class SystemPropertiesView extends CustomComponent implements View {

    public static final String NAME = "sysprops";
    private Header header;
    private static final String[] columnHeaders_ = {"Свойство", "Значение"};
    private static final Object[] visibleColumns_ = {"propertyDisplayName", "propertyValue"};
    private final TextField propertyDisplayName = new TextField("Свойство");
    private final TextField propertyValue = new TextField("Значение");

    @Autowired
    private SystemPropertyRepository systemPropertyRepository;
    private MGrid<SystemProperty> mGrid;

    public SystemPropertiesView() {
        this.setSizeFull();
        header = new Header();
        mGrid = new MGrid<>();
        mGrid.setSizeFull();
        VerticalLayout vl = new VerticalLayout(header, mGrid);
        vl.setExpandRatio(mGrid, 1f);
        vl.setSizeFull();
        setCompositionRoot(vl);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        header.build();
        mGrid.lazyLoadFrom((int firstRow, boolean sortAscending, String property) -> systemPropertyRepository.findAll(new PageRequest(
                firstRow / PAGESIZE,
                PAGESIZE,
                sortAscending ? Sort.Direction.ASC : Sort.Direction.DESC,
                property == null ? "id" : property
        )).getContent(),
                () -> (int) systemPropertyRepository.count(),
                40);

    }

}
