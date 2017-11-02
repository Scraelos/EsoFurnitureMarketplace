/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.v7.data.Validator;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.data.util.BeanItem;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.PasswordField;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.TwinColSelect;
import com.vaadin.v7.ui.VerticalLayout;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scraelos.esofurnituremp.Bundle;
import org.scraelos.esofurnituremp.model.ESO_SERVER;
import org.scraelos.esofurnituremp.model.SysAccount;
import org.scraelos.esofurnituremp.model.SysAccountRole;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@SpringView(name = UsersView.NAME)
@Secured({"ROLE_ADMIN"})
public class UsersView extends CustomComponent implements View {

    public static final String NAME = "users";
    private Bundle i18n = new Bundle();
    private HorizontalLayout actions;
    private Button refreshButton;
    private Button addButton;
    private HorizontalLayout tableAndForm;
    private Table table;
    private BeanItemContainer<SysAccount> container;
    private FormLayout form;
    private FieldGroup fieldGroup;
    private TextField username;
    private TextField esoId;
    private ComboBox esoServer;
    private PasswordField password;
    private PasswordField passwordRepeat;
    private TwinColSelect roles;
    private CheckBox enabled;
    private Button saveButton;

    private BeanItem currentUserItem;
    private Header header;

    @Autowired
    private DBService service;

    public UsersView() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        header = new Header();
        vl.addComponent(header);
        actions = new HorizontalLayout();
        refreshButton = new Button("Refresh");
        refreshButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                LoadTable();
            }
        });
        actions.addComponent(refreshButton);
        addButton = new Button("New");
        addButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                AddUser();
            }
        });
        actions.addComponent(addButton);
        vl.addComponent(actions);
        tableAndForm = new HorizontalLayout();
        tableAndForm.setSizeFull();
        table = new Table();
        table.setSizeFull();
        container = new BeanItemContainer<>(SysAccount.class);
        table.setContainerDataSource(container);
        table.setVisibleColumns(new Object[]{"username", "esoId", "roles"});
        table.addItemClickListener(new TableClickListener());

        tableAndForm.addComponent(table);
        tableAndForm.setExpandRatio(table, 0.5f);
        form = new FormLayout();
        form.setSizeFull();
        username = new TextField("Login");
        username.setNullRepresentation("");
        username.setRequired(true);
        form.addComponent(username);
        esoId = new TextField("esoId");
        esoId.setNullRepresentation("");
        esoId.setRequired(true);
        form.addComponent(esoId);
        esoServer = new ComboBox(i18n.activeServer(), Arrays.asList(ESO_SERVER.values()));
        esoServer.setNullSelectionAllowed(false);
        form.addComponent(esoServer);
        password = new PasswordField("Password");
        form.addComponent(password);
        passwordRepeat = new PasswordField("Repeat Password");
        passwordRepeat.addValidator(new PasswordValidator(password, passwordRepeat));
        form.addComponent(passwordRepeat);
        roles = new TwinColSelect("Roles");

        roles.setWidth(900f, Unit.PIXELS);
        Page.Styles styles = Page.getCurrent().getStyles();
        styles.add(".v-font-size {\n"
                + "    font-size: 11px;\n"
                + "}");
        roles.addStyleName("v-font-size");
        form.addComponent(roles);
        enabled = new CheckBox("Enabled");
        form.addComponent(enabled);
        saveButton = new Button("Save");
        saveButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                SaveForm();
            }
        });
        form.addComponent(saveButton);
        form.setVisible(false);
        tableAndForm.addComponent(form);
        tableAndForm.setExpandRatio(form, 0.5f);
        vl.addComponent(tableAndForm);
        setCompositionRoot(vl);
    }

    private void LoadTable() {
        container = service.loadBeanItems(container);
    }

    private void AddUser() {
        SysAccount sysAccount = new SysAccount();
        sysAccount.setEnabled(Boolean.TRUE);
        BeanItem<SysAccount> beanItem = new BeanItem<>(sysAccount);
        currentUserItem = beanItem;
        OpenForm();
    }

    private void OpenForm() {
        form.setVisible(true);
        fieldGroup = new FieldGroup(currentUserItem);
        fieldGroup.bind(username, "username");
        fieldGroup.bind(esoId, "esoId");
        fieldGroup.bind(esoServer, "esoServer");
        fieldGroup.bind(roles, "roles");
        fieldGroup.bind(enabled, "enabled");

    }

    private void CloseForm() {
        form.setVisible(false);
    }

    private void SaveForm() {
        try {
            fieldGroup.commit();
            SysAccount sysAccount = (SysAccount) currentUserItem.getBean();
            if (password.getValue() != null && !password.getValue().isEmpty() && passwordRepeat.isValid()) {
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                String hashedPassword = passwordEncoder.encode(password.getValue());
                sysAccount.setPassword(hashedPassword);
            }
            if (!sysAccount.getPassword().isEmpty()) {
                service.saveEntity(sysAccount);
            }
            CloseForm();
            LoadTable();
        } catch (FieldGroup.CommitException ex) {
            Logger.getLogger(UsersView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        header.build();
        BeanItemContainer<SysAccountRole> rolesContainer = new BeanItemContainer<>(SysAccountRole.class);
        rolesContainer = service.loadBeanItems(rolesContainer);
        roles.setContainerDataSource(rolesContainer);
        LoadTable();
    }

    private class TableClickListener implements ItemClickEvent.ItemClickListener {

        @Override
        public void itemClick(ItemClickEvent event) {
            currentUserItem = (BeanItem) event.getItem();
            OpenForm();
        }

    }

    private class PasswordValidator implements Validator {

        private final PasswordField password1;
        private final PasswordField password2;

        public PasswordValidator(PasswordField password1, PasswordField password2) {
            this.password1 = password1;
            this.password2 = password2;
        }

        @Override
        public void validate(Object value) throws InvalidValueException {
            if (!password1.getValue().equals(password2.getValue())) {
                throw new InvalidValueException("Passwords does not match");
            }
        }

    }

}
