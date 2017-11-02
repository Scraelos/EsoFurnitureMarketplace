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
public enum FURNITURE_THEME {

    SI_FURNITURETHEMETYPE1("Other", "Other", "Other", "Other", 1),
    SI_FURNITURETHEMETYPE10("Redguard", "Redguard", "Redguard", "Redguard", 0),
    SI_FURNITURETHEMETYPE11("Imperial", "Imperial", "Imperial", "Imperial", 0),
    SI_FURNITURETHEMETYPE12("Dwarven", "Dwarven", "Dwarven", "Dwarven", 1),
    SI_FURNITURETHEMETYPE13("Daedric", "Daedric", "Daedric", "Daedric", 1),
    SI_FURNITURETHEMETYPE14("Ayleid", "Ayleid", "Ayleid", "Ayleid", 1),
    SI_FURNITURETHEMETYPE15("Primal", "Primal", "Primal", "Primal", 1),
    SI_FURNITURETHEMETYPE16("Clockwork", "Clockwork", "Clockwork", "Clockwork", 1),
    //SI_FURNITURETHEMETYPE17("Unused 6", 0),
    //SI_FURNITURETHEMETYPE18("Unused 7", 0),
    //SI_FURNITURETHEMETYPE19("Unused 8", 0),
    SI_FURNITURETHEMETYPE2("Breton", "Breton", "Breton", "Breton", 0),
    //SI_FURNITURETHEMETYPE20("Unused 9", 0),
    //    SI_FURNITURETHEMETYPE21("Unused 10", 0),
    //    SI_FURNITURETHEMETYPE22("Unused 11", 0),
    //    SI_FURNITURETHEMETYPE23("Unused 12", 0),
    //    SI_FURNITURETHEMETYPE24("Unused 13", 0),
    //    SI_FURNITURETHEMETYPE25("Unused 14", 0),
    //    SI_FURNITURETHEMETYPE26("Unused 15", 0),
    //    SI_FURNITURETHEMETYPE27("Unused 16", 0),
    //    SI_FURNITURETHEMETYPE28("Unused 17", 0),
    //    SI_FURNITURETHEMETYPE29("Unused 18", 0),
    SI_FURNITURETHEMETYPE3("High Elf", "High Elf", "High Elf", "High Elf", 1),
    //    SI_FURNITURETHEMETYPE30("Unused 19", 0),
    //    SI_FURNITURETHEMETYPE31("Unused 20", 0),
    SI_FURNITURETHEMETYPE4("Argonian", "Argonian", "Argonian", "Argonian", 0),
    SI_FURNITURETHEMETYPE5("Wood Elf", "Wood Elf", "Wood Elf", "Wood Elf", 1),
    SI_FURNITURETHEMETYPE6("Dark Elf", "Dark Elf", "Dark Elf", "Dark Elf", 1),
    SI_FURNITURETHEMETYPE7("Khajiit", "Khajiit", "Khajiit", "Khajiit", 0),
    SI_FURNITURETHEMETYPE8("Nord", "Nord", "Nord", "Nord", 0),
    SI_FURNITURETHEMETYPE9("Orc", "Orc", "Orc", "Orc", 0);

    private FURNITURE_THEME(String nameEn_, String nameDe_, String nameFr_, String nameRu_, int code_) {
        nameEn = nameEn_;
        code = code_;
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
    private final int code;

    @Override
    public String toString() {
        return nameEn;
    }

}
