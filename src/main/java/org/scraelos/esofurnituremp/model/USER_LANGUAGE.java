/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.model;

import java.util.Locale;

/**
 *
 * @author scraelos
 */
public enum USER_LANGUAGE {

    EN(new Locale("en"), "English"),
    DE(new Locale("de"), "Deutsch"),
    FR(new Locale("fr"), "Französisch"),
    RU(new Locale("ru"), "Русский");

    private USER_LANGUAGE(Locale locale_, String description_) {
        this.locale = locale_;
        this.description = description_;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public String toString() {
        return description;
    }
    
    public static USER_LANGUAGE getLanguageByLocale(Locale l) {
        USER_LANGUAGE lang=null;
        for(USER_LANGUAGE item:values()) {
            if(item.name().toLowerCase().equals(l.getLanguage().toLowerCase())) {
                return item;
            }
        }
        return lang;
    }

    private final Locale locale;
    private final String description;
}
