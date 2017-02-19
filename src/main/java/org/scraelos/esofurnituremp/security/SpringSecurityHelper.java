/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/**
 * вспомогательный класс
 *
 * @author scraelos
 */
public class SpringSecurityHelper {

    /**
     * возвращает true, если пользователь в текущей сессии обладает указанной
     * ролью
     *
     * @param role
     * @return
     */
    public static boolean hasRole(String role) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getAuthorities().contains(new SimpleGrantedAuthority(role));
    }

    public static boolean isUserAnonymous() {
        boolean result=(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
        return result;
    }

}
