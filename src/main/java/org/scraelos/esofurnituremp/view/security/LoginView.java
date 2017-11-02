/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view.security;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.v7.ui.PasswordField;
import com.vaadin.ui.TabSheet;
import com.vaadin.v7.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.validator.EmailValidator;
import com.wcs.wcslib.vaadin.widget.recaptcha.ReCaptcha;
import com.wcs.wcslib.vaadin.widget.recaptcha.shared.ReCaptchaOptions;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scraelos.esofurnituremp.Bundle;
import org.scraelos.esofurnituremp.model.ESO_SERVER;
import org.scraelos.esofurnituremp.model.SysAccount;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@SpringView(name = LoginView.NAME)
public class LoginView extends CustomComponent implements View,
        Button.ClickListener {

    public static final String NAME = "login";

    private final TextField user;

    private final PasswordField password;

    private final Button loginButton;

    private final TextField newUser;
    private final TextField newUserRepeat;
    private final TextField newUserId;

    private final PasswordField newPassword;
    private final PasswordField newPasswordRepeat;
    private final Button registerButton;

    private final FormLayout loginFields;
    private final FormLayout registerFields;

    private final ComboBox activeServer;
    private final Label activeServerlabel;
    private ReCaptcha captcha;

    private Bundle i18n;

    private String forwardTo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    DBService dBService;

    private static final Logger LOG = Logger.getLogger(LoginView.class.getName());

    public LoginView() {
        setSizeFull();
        i18n = new Bundle();
        user = new TextField(i18n.email());
        user.setWidth(100f, Unit.PERCENTAGE);
        user.setRequired(true);
        user.setImmediate(true);
        user.setInputPrompt(i18n.emailPromt());
        user.addValidator(new EmailValidator(i18n.invalidUsername()));
        user.setInvalidAllowed(false);
        
        password = new PasswordField(i18n.password());
        password.setWidth(100f, Unit.PERCENTAGE);
        password.setRequired(true);
        password.setImmediate(true);
        password.setNullRepresentation("");

        loginButton = new Button(i18n.loginMenuItemCaption(), this);

        // Add both to a panel
        loginFields = new FormLayout(user, password, loginButton);
        loginFields.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        loginFields.setSpacing(true);
        loginFields.setMargin(new MarginInfo(true, true, true, false));
        loginFields.setComponentAlignment(user, Alignment.TOP_CENTER);
        loginFields.setComponentAlignment(password, Alignment.TOP_CENTER);
        loginFields.setComponentAlignment(loginButton, Alignment.TOP_CENTER);
        loginFields.setSizeFull();

        newUser = new TextField(i18n.email());
        newUser.setWidth(100f, Unit.PERCENTAGE);
        newUser.setRequired(true);
        newUser.setImmediate(true);
        newUser.setInputPrompt(i18n.emailPromt());
        newUser.addValidator(new EmailValidator(i18n.invalidUsername()));
        newUser.setInvalidAllowed(false);

        newUserRepeat = new TextField(i18n.emailRepeat());
        newUserRepeat.setWidth(100f, Unit.PERCENTAGE);
        newUserRepeat.setRequired(true);
        newUserRepeat.setImmediate(true);
        newUserRepeat.setInputPrompt(i18n.emailRepeatPromt());
        newUserRepeat.setInvalidAllowed(false);

        activeServer = new ComboBox(i18n.activeServer(), Arrays.asList(ESO_SERVER.values()));
        activeServer.setValue(ESO_SERVER.EU);
        activeServerlabel = new Label(i18n.activeServerNotice());

        newUserId = new TextField(i18n.ingameId());
        newUserId.setWidth(100f, Unit.PERCENTAGE);
        newUserId.setRequired(true);
        newUserId.setImmediate(true);
        newUserId.setInputPrompt(i18n.ingameIdPromt());
        newUserId.setInvalidAllowed(false);

        newPassword = new PasswordField(i18n.password());
        newPassword.setWidth(100f, Unit.PERCENTAGE);
        newPassword.setRequired(true);
        newPassword.setImmediate(true);
        newPassword.setNullRepresentation("");

        newPasswordRepeat = new PasswordField(i18n.passwordRepeat());
        newPasswordRepeat.setWidth(100f, Unit.PERCENTAGE);
        newPasswordRepeat.setRequired(true);
        newPasswordRepeat.setImmediate(true);
        newPasswordRepeat.setNullRepresentation("");
        captcha = new ReCaptcha(
                "6LfQMRkUAAAAAHVI39ktj2xc1ZpTMYvLKKnjrZ_z",
                new ReCaptchaOptions() {
                    {
                        theme = "light";
                        sitekey = "6LfQMRkUAAAAAJG4zPW6yD4vJJLE1CjkCh53qGwH";
                    }
                }
        );
        registerButton = new Button(i18n.registerAndLogin(), this);
        registerFields = new FormLayout(newUser, newUserRepeat, newUserId, activeServer, activeServerlabel, newPassword, newPasswordRepeat, captcha, registerButton);
        registerFields.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        registerFields.setSpacing(true);
        registerFields.setMargin(new MarginInfo(true, true, true, false));
        registerFields.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setWidth(750f, Unit.PIXELS);
        sheet.setHeight(500f, Unit.PIXELS);
        sheet.addStyleName(ValoTheme.TABSHEET_CENTERED_TABS);
        sheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        sheet.addTab(loginFields, i18n.loginTab(), FontAwesome.SIGN_IN, 0);
        sheet.addTab(registerFields, i18n.registerTab(), FontAwesome.USER_PLUS, 1);

        // The view root layout
        VerticalLayout viewLayout = new VerticalLayout(sheet);
        viewLayout.setSizeFull();
        viewLayout.setComponentAlignment(sheet, Alignment.MIDDLE_CENTER);
        setCompositionRoot(viewLayout);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        forwardTo = event.getParameters();
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        if (event.getButton() == loginButton && user.isValid() && password.isValid()) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.getValue(), password.getValue());
            try {
                Authentication authenticate = authenticationManager.authenticate(authentication);
                SecurityContextHolder.getContext().setAuthentication(authenticate);
                LOG.log(Level.INFO, "{0} {1}", new Object[]{authenticate.getPrincipal(), authenticate.isAuthenticated()});
                if (authenticate.getPrincipal() instanceof SysAccount && ((SysAccount) authenticate.getPrincipal()).getUserLanguage() != null) {
                    Locale lc = ((SysAccount) authenticate.getPrincipal()).getUserLanguage().getLocale();
                    getUI().setLocale(lc);
                    getUI().getSession().setAttribute("useEnglishNames", ((SysAccount) authenticate.getPrincipal()).getUseEnItemNames());
                }
                getUI().getNavigator().navigateTo(forwardTo);
            } catch (AuthenticationException ex) {
                password.setValue("");
                Notification.show(i18n.authErrorCaption(), i18n.authErrorDescription(), Notification.Type.ERROR_MESSAGE);
                Logger.getLogger(LoginView.class.getName()).log(Level.INFO, null, ex);
            }

        } else if (event.getButton() == registerButton) {
            if (captcha.validate()) {
                if (newUser.isValid() && newUserRepeat.isValid() && newPassword.isValid() && newPasswordRepeat.isValid() && newUserId.isValid()) {
                    try {
                        dBService.registerUser(newUser.getValue(), newPassword.getValue(), newUserId.getValue(), (ESO_SERVER) activeServer.getValue());
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(newUser.getValue(), newPassword.getValue());
                        Authentication authenticate = authenticationManager.authenticate(authentication);
                        SecurityContextHolder.getContext().setAuthentication(authenticate);
                        LOG.log(Level.INFO, "{0} {1}", new Object[]{authenticate.getPrincipal(), authenticate.isAuthenticated()});
                        if (authentication.getPrincipal() instanceof SysAccount && ((SysAccount) authentication.getPrincipal()).getUserLanguage() != null) {
                            getUI().setLocale(((SysAccount) authentication.getPrincipal()).getUserLanguage().getLocale());
                        }
                        getUI().getNavigator().navigateTo(forwardTo);
                    } catch (Exception ex) {
                        Logger.getLogger(LoginView.class.getName()).log(Level.SEVERE, null, ex);
                        Notification.show(i18n.registrationErrorCaption(), ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                    }
                }
            } else {
                captcha.reload();
            }

        }
    }

    public static String loginPathForRequestedView(String requestedViewName) {
        return NAME + "/" + requestedViewName;
    }

}
