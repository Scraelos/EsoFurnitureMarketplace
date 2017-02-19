/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.view.security;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.xpoft.vaadin.VaadinView;

/**
 *
 * @author scraelos
 */
@Component
@Scope("prototype")
@VaadinView(LoginView.NAME)
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


    private String forwardTo;

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired DBService dBService;

    private static final Logger LOG = Logger.getLogger(LoginView.class.getName());

    public LoginView() {
        setSizeFull();

        user = new TextField("E-mail:");
        user.setWidth("300px");
        user.setRequired(true);
        user.setImmediate(true);
        user.setInputPrompt("Your username (eg. joe@email.com)");
        user.addValidator(new EmailValidator("Username must be an email address"));
        user.setInvalidAllowed(false);

        password = new PasswordField("Password:");
        password.setWidth("300px");
        password.setRequired(true);
        password.setImmediate(true);
        password.setNullRepresentation("");

        loginButton = new Button("Login", this);

        
        // Add both to a panel
        loginFields = new FormLayout(user, password, loginButton);
        loginFields.setCaption("Please login to access the application. (test@test.com/passw0rd)");
        loginFields.setSpacing(true);
        loginFields.setMargin(new MarginInfo(true, true, true, false));
        loginFields.setSizeUndefined();

        newUser = new TextField("E-mail:");
        newUser.setWidth("300px");
        newUser.setRequired(true);
        newUser.setImmediate(true);
        newUser.setInputPrompt("Your E-mail (eg. joe@email.com)");
        newUser.addValidator(new EmailValidator("Username must be an email address"));
        newUser.setInvalidAllowed(false);

        newUserRepeat = new TextField("Repeat E-mail:");
        newUserRepeat.setWidth("300px");
        newUserRepeat.setRequired(true);
        newUserRepeat.setImmediate(true);
        newUserRepeat.setInputPrompt("Repeat your E-mail");
        newUserRepeat.setInvalidAllowed(false);

        newUserId = new TextField("Ingame id without @:");
        newUserId.setWidth("300px");
        newUserId.setRequired(true);
        newUserId.setImmediate(true);
        newUserId.setInputPrompt("Your ingame id without @");
        newUserId.setInvalidAllowed(false);

        newPassword = new PasswordField("Password:");
        newPassword.setWidth("300px");
        newPassword.setRequired(true);
        newPassword.setImmediate(true);
        newPassword.setNullRepresentation("");

        newPasswordRepeat = new PasswordField("Repeat Password:");
        newPasswordRepeat.setWidth("300px");
        newPasswordRepeat.setRequired(true);
        newPasswordRepeat.setImmediate(true);
        newPasswordRepeat.setNullRepresentation("");

        registerButton = new Button("Register", this);
        registerFields = new FormLayout(newUser, newUserRepeat, newUserId, newPassword, newPasswordRepeat, registerButton);
        registerFields.setCaption("Please login to access the application. (test@test.com/passw0rd)");
        registerFields.setSpacing(true);
        registerFields.setMargin(new MarginInfo(true, true, true, false));
        registerFields.setSizeUndefined();

        TabSheet sheet = new TabSheet();
        sheet.setWidth(550f, Unit.PIXELS);
        sheet.setHeight(500f, Unit.PIXELS);
        sheet.addStyleName(ValoTheme.TABSHEET_CENTERED_TABS);
        sheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        sheet.addTab(loginFields, "Login", FontAwesome.SIGN_IN, 0);
        sheet.addTab(registerFields, "Register", FontAwesome.USER_PLUS, 1);

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
        if (event.getButton() == loginButton && user.isValid()&&password.isValid()) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.getValue(), password.getValue());
            try {
                Authentication authenticate = authenticationManager.authenticate(authentication);
                SecurityContextHolder.getContext().setAuthentication(authenticate);
                LOG.log(Level.INFO, "{0} {1}", new Object[]{authenticate.getPrincipal(), authenticate.isAuthenticated()});
                getUI().getNavigator().navigateTo(forwardTo);
            } catch (Exception ex) {
                password.setValue("");
                Notification.show("Authentication error", "Could not authenticate", Notification.Type.ERROR_MESSAGE);

            }

        } else if(event.getButton()==registerButton&&newUser.isValid()&&newUserRepeat.isValid()&&newPassword.isValid()&&newPasswordRepeat.isValid()&&newUserId.isValid()) {
            try {
                dBService.registerUser(newUser.getValue(), newPassword.getValue(), newUserId.getValue());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(newUser.getValue(), newPassword.getValue());
                Authentication authenticate = authenticationManager.authenticate(authentication);
                SecurityContextHolder.getContext().setAuthentication(authenticate);
                LOG.log(Level.INFO, "{0} {1}", new Object[]{authenticate.getPrincipal(), authenticate.isAuthenticated()});
                getUI().getNavigator().navigateTo(forwardTo);
            } catch (Exception ex) {
                Logger.getLogger(LoginView.class.getName()).log(Level.SEVERE, null, ex);
                Notification.show("Registration error", ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        }
    }

    public static String loginPathForRequestedView(String requestedViewName) {
        return NAME + "/" + requestedViewName;
    }

}
