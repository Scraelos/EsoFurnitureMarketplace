package org.scraelos.esofurnituremp.security;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.ErrorEvent;
import com.vaadin.server.ErrorHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scraelos.esofurnituremp.view.AccessDeniedView;
import org.scraelos.esofurnituremp.view.security.LoginView;

import org.springframework.security.access.AccessDeniedException;

/**
 * @author Roland Krüger
 */
public class SecurityErrorHandler implements ErrorHandler {

    private static final Logger LOG = Logger.getLogger(SecurityErrorHandler.class.getName());

    private final Navigator navigator;
    private final ErrorHandler defaultErrorHandler;

    public SecurityErrorHandler(Navigator navigator,ErrorHandler defaultErrorHandler) {
        this.navigator = navigator;
        this.defaultErrorHandler=defaultErrorHandler;
    }

    @Override
    public void error(ErrorEvent event) {
        
        Throwable rootCause=event.getThrowable();
        while(rootCause.getCause()!=null||!(rootCause instanceof AccessDeniedException)) {
            rootCause=rootCause.getCause();
        }
        if (rootCause instanceof AccessDeniedException) {
            LOG.log(Level.INFO, "Error handler caught exception", event.getThrowable());
            if (SpringSecurityHelper.isUserAnonymous() && !navigator.getState().startsWith(LoginView.NAME)) {
                navigator.navigateTo(LoginView.loginPathForRequestedView(navigator.getState()));
            } else if (!SpringSecurityHelper.isUserAnonymous()) {
                navigator.navigateTo("");
            }
        } else {
            defaultErrorHandler.error(event);
        }
    }
}
