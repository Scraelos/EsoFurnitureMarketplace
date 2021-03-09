/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import com.vaadin.data.ValueContext;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import java.util.Arrays;
import java.util.List;
import org.scraelos.esofurnituremp.Bundle;
import org.scraelos.esofurnituremp.data.SysAccountRepository;
import org.scraelos.esofurnituremp.data.SysAccountRoleRepository;
import org.scraelos.esofurnituremp.model.ESO_SERVER;
import org.scraelos.esofurnituremp.model.SysAccount;
import org.scraelos.esofurnituremp.model.SysAccountRole;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private Grid<SysAccount> grid;
    private FormLayout form;
    private Binder<SysAccount> binder;
    private Binder<DoublePassword> passwordBinder;
    private TextField username;
    private TextField esoId;
    private ComboBox<ESO_SERVER> esoServer;
    private PasswordField password;
    private PasswordField passwordRepeat;
    private TwinColSelect<SysAccountRole> roles;
    private CheckBox enabled;
    private Button saveButton;
    private SysAccount currentUser;
    private Header header;

    @Autowired
    private DBService service;
    @Autowired
    private SysAccountRoleRepository sysAccountRoleRepo;
    @Autowired
    private SysAccountRepository repo;

    public UsersView() {
        setSizeFull();
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setSpacing(false);
        vl.setMargin(false);
        header = new Header();
        vl.addComponent(header);
        actions = new HorizontalLayout();
        refreshButton = new Button("Refresh");
        refreshButton.addClickListener(event -> grid.getDataProvider().refreshAll());
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
        grid = new Grid<>();
        grid.setSizeFull();
        grid.setDataProvider(
                (sortOrder, offset, limit) -> {
                    final List<SysAccount> page = repo.findAll(
                            new PageRequest(
                                    offset / limit,
                                    limit,
                                    sortOrder.isEmpty() || sortOrder.get(0).getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC,
                                    sortOrder.isEmpty() ? "id" : sortOrder.get(0).getSorted()
                            )
                    ).getContent();
                    return page.subList(offset % limit, page.size()).stream();
                },
                () -> (int) repo.count()
        );
        grid.addItemClickListener(event -> selectUser(event.getItem()));
        grid.addColumn(SysAccount::getUsername).setCaption("User");
        grid.addColumn(SysAccount::getEsoId).setCaption("ESO Id");
        grid.addColumn(SysAccount::getRoles).setCaption("Roles");
        tableAndForm.addComponent(grid);
        tableAndForm.setExpandRatio(grid, 0.5f);
        form = new FormLayout();
        form.setSizeFull();
        username = new TextField("Login");
        form.addComponent(username);
        esoId = new TextField("esoId");
        form.addComponent(esoId);
        esoServer = new ComboBox<>(i18n.activeServer(), Arrays.asList(ESO_SERVER.values()));
        esoServer.setEmptySelectionAllowed(false);
        form.addComponent(esoServer);
        password = new PasswordField("Password");
        form.addComponent(password);
        passwordRepeat = new PasswordField("Repeat Password");
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
        binder = new Binder(SysAccount.class);
        binder.forField(username).asRequired().withValidator(new EmailValidator(i18n.invalidUsername())).bind("username");
        binder.forField(esoId).asRequired().bind("esoId");
        binder.forField(esoServer).asRequired().bind("esoServer");
        binder.forField(roles).bind("roles");
        binder.forField(enabled).bind("enabled");
        passwordBinder = new Binder<>(DoublePassword.class);
        passwordBinder.forField(password).bind(DoublePassword::getPassword, DoublePassword::setPassword);
        passwordBinder.forField(passwordRepeat).withValidator(new PasswordValidator(password, passwordRepeat)).bind(DoublePassword::getPasswordRepeat, DoublePassword::setPasswordRepeat);
        form.setVisible(false);
        tableAndForm.addComponent(form);
        tableAndForm.setExpandRatio(form, 0.5f);
        vl.addComponent(tableAndForm);
        vl.setExpandRatio(tableAndForm, 1f);
        setCompositionRoot(vl);
    }

    private void AddUser() {
        currentUser = new SysAccount();
        currentUser.setEnabled(Boolean.TRUE);
        OpenForm();
    }

    private void OpenForm() {
        form.setVisible(true);
        binder.setBean(currentUser);
        passwordBinder.setBean(new DoublePassword());
    }

    private void CloseForm() {
        form.setVisible(false);
    }

    private void SaveForm() {
        if (binder.isValid()) {
            SysAccount sysAccount = binder.getBean();
            if (password.getValue() != null && !password.getValue().isEmpty() && passwordBinder.isValid()) {
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                String hashedPassword = passwordEncoder.encode(password.getValue());
                sysAccount.setPassword(hashedPassword);
            }
            if (!sysAccount.getPassword().isEmpty()) {
                service.saveEntity(sysAccount);
            }
            CloseForm();
            grid.getDataProvider().refreshAll();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        header.build();
        roles.setItems(sysAccountRoleRepo.findAll());
    }

    private void selectUser(SysAccount user) {
        currentUser = user;
        OpenForm();
    }

    private class PasswordValidator implements Validator<String> {

        private final PasswordField password1;
        private final PasswordField password2;

        public PasswordValidator(PasswordField password1, PasswordField password2) {
            this.password1 = password1;
            this.password2 = password2;
        }

        @Override
        public ValidationResult apply(String value, ValueContext context) {
            if (!password1.getValue().equals(password2.getValue())) {
                return ValidationResult.error("Passwords does not match");
            }
            return ValidationResult.ok();
        }

    }

    public class DoublePassword {

        private String password;
        private String passwordRepeat;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPasswordRepeat() {
            return passwordRepeat;
        }

        public void setPasswordRepeat(String passwordRepeat) {
            this.passwordRepeat = passwordRepeat;
        }

    }

}
