/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view;

import com.github.peholmst.i18n4vaadin.LocaleChangedEvent;
import com.github.peholmst.i18n4vaadin.LocaleChangedListener;
import com.github.peholmst.i18n4vaadin.util.I18NHolder;
import com.vaadin.data.Binder;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import com.vaadin.data.ValueContext;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.VerticalLayout;
import java.util.Locale;
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
    private ComboBox<ESO_SERVER> serverBox;
    private ComboBox<USER_LANGUAGE> languageBox;
    private CheckBox useEnItemNamesBox;
    private Button saveSettings;
    private Binder<ChangePassword> binder;
    private PasswordField oldPasswordField;
    private PasswordField passwordField;
    private PasswordField passwordRepeatField;
    private Button changePassword;
    private Button cleanUsersRecipes;
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
        binder = new Binder<>(ChangePassword.class);
        oldPasswordField = new PasswordField();
        binder.forField(oldPasswordField).withValidator(new OldPasswordValidator(oldPasswordField)).bind(ChangePassword::getOldPassword, ChangePassword::setOldPassword);
        passwordField = new PasswordField();
        passwordRepeatField = new PasswordField();
        binder.forField(passwordRepeatField).withValidator(new DoublePasswordValidator(passwordField, passwordRepeatField)).bind(ChangePassword::getPasswordRepeat, ChangePassword::setPasswordRepeat);
        changePassword = new Button();
        changePassword.addClickListener(event -> changePasswordAction());
        serverBox = new ComboBox<>();
        languageBox = new ComboBox<>();
        useEnItemNamesBox = new CheckBox();
        saveSettings = new Button();
        saveSettings.addClickListener(event -> saveSettingsAction());
        cleanUsersRecipes = new Button();
        cleanUsersRecipes.addClickListener(event -> service.cleanUsersKnownRecipes(SpringSecurityHelper.getUser()));
        FormLayout userSettingsForm = new FormLayout(serverBox, languageBox, useEnItemNamesBox, saveSettings, oldPasswordField, passwordField, passwordRepeatField, changePassword, cleanUsersRecipes);
        userSettingsForm.setSpacing(true);
        userSettingsForm.setMargin(true);
        vl.addComponent(header);
        vl.addComponent(userSettingsForm);
        vl.setComponentAlignment(userSettingsForm, Alignment.TOP_LEFT);
        vl.setExpandRatio(userSettingsForm, 1f);
        vl.setMargin(false);
        setCompositionRoot(vl);
    }

    private void saveSettingsAction() {
        SpringSecurityHelper.getUser().setUserLanguage(languageBox.getValue());
        SpringSecurityHelper.getUser().setEsoServer(serverBox.getValue());
        SpringSecurityHelper.getUser().setUseEnItemNames(useEnItemNamesBox.getValue());
        getUI().getSession().setAttribute("useEnglishNames", useEnItemNamesBox.getValue());
        service.saveUserProfile(SpringSecurityHelper.getUser());
        getUI().setLocale(((USER_LANGUAGE) languageBox.getValue()).getLocale());
        getUI().getNavigator().navigateTo(UserProfileView.NAME);
    }

    private void changePasswordAction() {
        if (binder.isValid()) {
            service.updateUserPassword(SpringSecurityHelper.getUser(), passwordField.getValue());
            Notification n = new Notification("Password Update", "Password was successfully updated", Notification.Type.HUMANIZED_MESSAGE);
            n.setDelayMsec(2000);
            n.show(getUI().getPage());
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        i18n = new Bundle();
        header.build();
        user = SpringSecurityHelper.getUser();
        serverBox.setItems(ESO_SERVER.values());
        serverBox.setEmptySelectionAllowed(false);
        serverBox.setValue(user.getEsoServer());
        languageBox.setEmptySelectionAllowed(false);
        languageBox.setItems(USER_LANGUAGE.values());
        languageBox.setValue(user.getUserLanguage() != null ? user.getUserLanguage() : getPageLanguage());
        useEnItemNamesBox.setValue(user.getUseEnItemNames());
        localize();

    }

    private USER_LANGUAGE getPageLanguage() {
        String languageString = getLocale().getLanguage();
        if (!languageString.equals("en") && !languageString.equals("de") && !languageString.equals("fr") && !languageString.equals("ru")) {
            languageString = "en";
        }
        Locale lc = new Locale(languageString);
        return USER_LANGUAGE.getLanguageByLocale(lc);
    }

    public void localize() {
        getUI().getPage().setTitle(i18n.userProfileMenuItemCaption() + " | " + i18n.siteTitle());
        oldPasswordField.setCaption(i18n.oldPassword());
        passwordField.setCaption(i18n.newPassword());
        passwordRepeatField.setCaption(i18n.newPasswordConfirm());
        changePassword.setCaption(i18n.changePassword());
        serverBox.setCaption(i18n.server());
        languageBox.setCaption(i18n.languageCaption());
        useEnItemNamesBox.setCaption(i18n.useEnglishItemNames());
        saveSettings.setCaption(i18n.saveProfileCaption());
        cleanUsersRecipes.setCaption(i18n.clearKnownRecipes());
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

    private class DoublePasswordValidator implements Validator<String> {

        private final PasswordField password1;
        private final PasswordField password2;

        public DoublePasswordValidator(PasswordField password1, PasswordField password2) {
            this.password1 = password1;
            this.password2 = password2;
        }

        @Override
        public ValidationResult apply(String value, ValueContext context) {
            if (!password1.getValue().equals(password2.getValue())) {
                return ValidationResult.error(i18n.passwordsDoNotMatch());
            }
            return ValidationResult.ok();
        }

    }

    private class OldPasswordValidator implements Validator<String> {

        private final PasswordField oldPassword;

        public OldPasswordValidator(PasswordField password1) {
            this.oldPassword = password1;
        }

        @Override
        public ValidationResult apply(String value, ValueContext context) {
            SysAccount sysAccount = SpringSecurityHelper.getUser();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            if (!passwordEncoder.matches(oldPassword.getValue(), sysAccount.getPassword())) {
                return ValidationResult.error(i18n.wrongCurrentPassword());
            }
            return ValidationResult.ok();
        }

    }

    public class ChangePassword {

        private String oldPassword;
        private String password;
        private String passwordRepeat;

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

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
