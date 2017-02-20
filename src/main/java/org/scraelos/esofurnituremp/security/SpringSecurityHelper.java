/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.security;

import org.scraelos.esofurnituremp.model.SysAccount;
import org.scraelos.esofurnituremp.model.SysAccountRole;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * вспомогательный класс
 *
 * @author scraelos
 */
public class SpringSecurityHelper {

    public static SysAccount getUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof SysAccount) {
            return (SysAccount) principal;
        } else {
            return null;
        }
    }

    /**
     * возвращает true, если пользователь в текущей сессии обладает указанной
     * ролью
     *
     * @param role
     * @return
     */
    public static boolean hasRole(String role) {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof SysAccount) {
            return ((SysAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getAuthorities().contains(new SysAccountRole(role));
        } else {
            return false;
        }

    }

    public static boolean isUserAnonymous() {
        boolean result = (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
        return result;
    }

}
