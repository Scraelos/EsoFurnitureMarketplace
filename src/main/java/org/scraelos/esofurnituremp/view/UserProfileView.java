/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.github.peholmst.i18n4vaadin.LocaleChangedEvent;
import com.github.peholmst.i18n4vaadin.LocaleChangedListener;
import com.github.peholmst.i18n4vaadin.util.I18NHolder;
import com.vaadin.v7.data.Validator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.v7.ui.PasswordField;
import com.vaadin.v7.ui.VerticalLayout;
import java.util.Arrays;
import org.scraelos.esofurnituremp.Bundle;
import org.scraelos.esofurnituremp.model.ESO_SERVER;
import org.scraelos.esofurnituremp.model.SysAccount;
import org.scraelos.esofurnituremp.model.USER_LANGUAGE;
import org.scraelos.esofurnituremp.security.SpringSecurityHelper;
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
@SpringView(name = UserProfileView.NAME)
@Secured({"ROLE_USER"})
public class UserProfileView extends CustomComponent implements View, LocaleChangedListener {

    public static final String NAME = "profile";
    private ComboBox serverBox;
    private ComboBox languageBox;
    private CheckBox useEnItemNamesBox;
    private Button saveSettings;
    private PasswordField oldPassword;
    private PasswordField password;
    private PasswordField passwordRepeat;
    private Button changePassword;
    private Header header = new Header();

    private Bundle i18n;

    private SysAccount user;

    @Autowired
    private DBService service;

    public UserProfileView() {
        setSizeFull();
        VerticalLayout vl = new VerticalLayout();
        vl.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        vl.setSizeFull();
        vl.setSpacing(true);
        oldPassword = new PasswordField();
        oldPassword.setImmediate(true);
        oldPassword.setRequired(true);
        oldPassword.addValidator(new OldPasswordValidator(oldPassword));
        password = new PasswordField();
        password.setImmediate(true);
        password.setRequired(true);
        passwordRepeat = new PasswordField();
        passwordRepeat.setImmediate(true);
        passwordRepeat.setRequired(true);
        passwordRepeat.addValidator(new DoublePasswordValidator(password, passwordRepeat));
        passwordRepeat.setValidationVisible(false);
        password.addValidator(new DoublePasswordValidator(password, passwordRepeat));
        password.setValidationVisible(false);
        changePassword = new Button();
        changePassword.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (oldPassword.isValid() && password.isValid() && passwordRepeat.isValid()) {
                    changePasswordAction();
                }
            }
        });

        serverBox = new ComboBox();
        languageBox = new ComboBox();
        useEnItemNamesBox = new CheckBox();
        saveSettings = new Button();
        saveSettings.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                SpringSecurityHelper.getUser().setUserLanguage((USER_LANGUAGE) languageBox.getValue());
                SpringSecurityHelper.getUser().setEsoServer((ESO_SERVER) serverBox.getValue());
                SpringSecurityHelper.getUser().setUseEnItemNames(useEnItemNamesBox.getValue());
                getUI().getSession().setAttribute("useEnglishNames", useEnItemNamesBox.getValue());
                service.saveUserProfile(SpringSecurityHelper.getUser());
                getUI().setLocale(((USER_LANGUAGE) languageBox.getValue()).getLocale());
                getUI().getNavigator().navigateTo(UserProfileView.NAME);
            }
        });
        FormLayout userSettingsForm = new FormLayout(serverBox, languageBox, useEnItemNamesBox, saveSettings, oldPassword, password, passwordRepeat, changePassword);
        userSettingsForm.setSpacing(true);
        userSettingsForm.setMargin(true);
        vl.addComponent(header);
        vl.addComponent(userSettingsForm);
        vl.setComponentAlignment(userSettingsForm, Alignment.TOP_LEFT);
        vl.setExpandRatio(userSettingsForm, 1f);

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
        user = SpringSecurityHelper.getUser();
        i18n = new Bundle();
        serverBox.addItems(Arrays.asList(ESO_SERVER.values()));
        serverBox.setNullSelectionAllowed(false);
        serverBox.setValue(user.getEsoServer());
        languageBox.setNullSelectionAllowed(false);
        languageBox.addItems(Arrays.asList(USER_LANGUAGE.values()));
        languageBox.setValue(user.getUserLanguage());
        useEnItemNamesBox.setValue(user.getUseEnItemNames());
        localize();

    }

    public void localize() {
        oldPassword.setCaption(i18n.oldPassword());
        password.setCaption(i18n.newPassword());
        passwordRepeat.setCaption(i18n.newPasswordConfirm());
        changePassword.setCaption(i18n.changePassword());
        serverBox.setCaption(i18n.server());
        languageBox.setCaption(i18n.languageCaption());
        useEnItemNamesBox.setCaption(i18n.useEnglishItemNames());
        saveSettings.setCaption(i18n.saveProfileCaption());
    }

    @Override
    public void localeChanged(LocaleChangedEvent lce) {
        localize();
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
                throw new Validator.InvalidValueException(i18n.passwordsDoNotMatch());
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
                throw new Validator.InvalidValueException(i18n.wrongCurrentPassword());
            }
        }

    }
}
