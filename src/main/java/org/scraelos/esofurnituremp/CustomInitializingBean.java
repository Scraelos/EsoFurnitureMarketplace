/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp;

import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Scraelos Здесь можно вписать код, который будет выполняться при
 * развёртывании приложения
 */
public class CustomInitializingBean implements InitializingBean {

    @Autowired
    DBService service;

    @Override
    public void afterPropertiesSet() throws Exception {
        service.createRoles();
        service.createDefaultAdminUser();
    }

}
