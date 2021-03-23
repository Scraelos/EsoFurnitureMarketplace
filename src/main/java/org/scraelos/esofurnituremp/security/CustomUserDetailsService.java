/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.security;

import org.scraelos.esofurnituremp.model.SysAccount;
import org.scraelos.esofurnituremp.service.DBService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 *
 * @author scraelos
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    DBService service;

    @Override
    public UserDetails loadUserByUsername(String string) throws UsernameNotFoundException {
        SysAccount account = service.getAccount(string);
        UserDetails result = account;
        return result;
    }

}
