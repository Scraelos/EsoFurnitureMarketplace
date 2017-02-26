/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.config;

import org.scraelos.esofurnituremp.service.DBService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author scraelos
 */
@Configuration
public class DataContext {

    @Bean
    public DBService dBService() {
        return new DBService();
    }
    
}
