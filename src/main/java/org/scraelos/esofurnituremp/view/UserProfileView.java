/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.scraelos.esofurnituremp.model.SysAccount;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import ru.xpoft.vaadin.VaadinView;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@VaadinView(UserProfileView.NAME)
public class UserProfileView extends CustomComponent implements View {

    public static final String NAME = "profile";
    private PasswordField oldPassword;
    private PasswordField password;
    private PasswordField passwordRepeat;
    private Button changePassword;
    private Header header = new Header();

    @Autowired
    private DBService service;

    public UserProfileView() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        oldPassword = new PasswordField("Old Password");
        oldPassword.setImmediate(true);
        oldPassword.setRequired(true);
        oldPassword.addValidator(new OldPasswordValidator(oldPassword));
        password = new PasswordField("New Password");
        password.setImmediate(true);
        password.setRequired(true);
        passwordRepeat = new PasswordField("Confirm New Password");
        passwordRepeat.setImmediate(true);
        passwordRepeat.setRequired(true);
        passwordRepeat.addValidator(new DoublePasswordValidator(password, passwordRepeat));
        passwordRepeat.setValidationVisible(false);
        password.addValidator(new DoublePasswordValidator(password, passwordRepeat));
        password.setValidationVisible(false);
        changePassword = new Button("Change Password");
        changePassword.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (oldPassword.isValid() && password.isValid() && passwordRepeat.isValid()) {
                    changePasswordAction();
                }
            }
        });

        FormLayout fl = new FormLayout();
        fl.setSizeFull();
        fl.addComponent(oldPassword);
        fl.addComponent(password);
        fl.addComponent(passwordRepeat);
        fl.addComponent(changePassword);
        vl.addComponent(header);
        vl.addComponent(fl);

        setCompositionRoot(vl);
    }

    private void changePasswordAction() {
        service.updateUserPassword(SpringSecurityHelper.getUser(), password.getValue());
        Notification n = new Notification("Passworpd Update", "Password was successfully updated", Notification.Type.HUMANIZED_MESSAGE);
        n.setDelayMsec(2000);
        n.show(getUI().getPage());
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        header.build();
    }

    private class DoublePasswordValidator implements Validator {

        private final PasswordField password1;
        private final PasswordField password2;

        public DoublePasswordValidator(PasswordField password1, PasswordField password2) {
            this.password1 = password1;
            this.password2 = password2;
        }

        @Override
        public void validate(Object value) throws Validator.InvalidValueException {
            if (!password1.getValue().equals(password2.getValue())) {
                throw new Validator.InvalidValueException("Пароли не совпадают");
            }
        }

    }

    private class OldPasswordValidator implements Validator {

        private final PasswordField oldPassword;

        public OldPasswordValidator(PasswordField password1) {
            this.oldPassword = password1;
        }

        @Override
        public void validate(Object value) throws Validator.InvalidValueException {
            SysAccount sysAccount = SpringSecurityHelper.getUser();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            if (!passwordEncoder.matches(oldPassword.getValue(), sysAccount.getPassword())) {
                throw new Validator.InvalidValueException("Старый пароль введён неверно");
            }
        }

    }
}
