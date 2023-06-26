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
public enum ONLINE_STATUS {

    Online("Online", "Online", "Online", "В сети"),
    Offline("Offline", "Offline", "Offline", "Не в сети"),
    Invisible("Invisible", "Invisible", "Invisible", "Невидимка");

    private ONLINE_STATUS(String nameEn_, String nameDe_, String nameFr_, String nameRu_) {
        nameEn = nameEn_;
        nameDe = nameDe_;
        nameFr = nameFr_;
        nameRu = nameRu_;
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
