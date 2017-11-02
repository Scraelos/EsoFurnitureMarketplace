/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.model;

/**
 *
 * @author scraelos
 */
public enum ITEM_QUALITY {

    Legendary("Legendary", "Legendary", "Legendary", "Легендарное"),
    Epic("Epic", "Epic", "Epic", "Эпическое"),
    Superior("Superior", "Superior", "Superior", "Превосходное"),
    Fine("Fine", "Fine", "Fine", "Хорошее"),
    Standard("Standard", "Standard", "Standard", "Обычное");

    private ITEM_QUALITY(String nameEn_, String nameDe_, String nameFr_, String nameRu_) {
        nameEn = nameEn_;
        nameDe = nameDe_;
        nameFr = nameFr_;
        nameRu = nameRu_;
    }

    public static ITEM_QUALITY valueOf(int code) {
        switch (code) {
            case 5:
                return Legendary;
            case 4:
                return Epic;
            case 3:
                return Superior;
            case 2:
                return Fine;
            case 1:
                return Standard;
            default:
                return null;
        }
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNameDe() {
        return nameDe;
    }

    public String getNameFr() {
        return nameFr;
    }

    public String getNameRu() {
        return nameRu;
    }

    private final String nameEn;
    private final String nameDe;
    private final String nameFr;
    private final String nameRu;

    @Override
    public String toString() {
        return nameEn;
    }

}
