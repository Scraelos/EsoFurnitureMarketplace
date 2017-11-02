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

//    SI_FURNITURETHEMETYPE0("All Themes", "Alle", "Tous les thèmes", "Все стили", 0),
    SI_FURNITURETHEMETYPE1("Other", "Andere", "Autre", "Другой", 1),
    SI_FURNITURETHEMETYPE10("Redguard", "Rothwarden", "Rougegarde", "Редгардский", 0),
    SI_FURNITURETHEMETYPE11("Imperial", "Kaiserliche", "Impérial", "Имперский", 0),
    SI_FURNITURETHEMETYPE12("Dwarven", "Dwemer", "Dwemer", "Двемерский", 1),
    SI_FURNITURETHEMETYPE13("Daedric", "Daedra", "Daedra", "Даэдрический", 1),
    SI_FURNITURETHEMETYPE14("Ayleid", "Ayleïden", "Ayléïde", "Айлейдский", 1),
    SI_FURNITURETHEMETYPE15("Primal", "Wilde", "Primal", "Первобытный", 1),
    SI_FURNITURETHEMETYPE16("Clockwork", "Uhrwerk", "Mécanique", "Заводной", 1),
    //    SI_FURNITURETHEMETYPE17("Unused 6", "Unused 6", "Inutilisé 6", "Unused 6", 0),
    //    SI_FURNITURETHEMETYPE18("Unused 7", "Unused 7", "Inutilisé 7", "Unused 7", 0),
    //    SI_FURNITURETHEMETYPE19("Unused 8", "Unused 8", "Inutilisé 8", "Unused 8", 0),
    SI_FURNITURETHEMETYPE2("Breton", "Bretonen", "Bréton", "Бретонский", 0),
    //    SI_FURNITURETHEMETYPE20("Unused 9", "Unused 9", "Inutilisé 9", "Unused 9", 0),
    //    SI_FURNITURETHEMETYPE21("Unused 10", "Unused 10", "Inutilisé 10", "Unused 10", 0),
    //    SI_FURNITURETHEMETYPE22("Unused 11", "Unused 11", "Inutilisé 11", "Unused 11", 0),
    //    SI_FURNITURETHEMETYPE23("Unused 12", "Unused 12", "Inutilisé 12", "Unused 12", 0),
    //    SI_FURNITURETHEMETYPE24("Unused 13", "Unused 13", "Inutilisé 13", "Unused 13", 0),
    //    SI_FURNITURETHEMETYPE25("Unused 14", "Unused 14", "Inutilisé 14", "Unused 14", 0),
    //    SI_FURNITURETHEMETYPE26("Unused 15", "Unused 15", "Inutilisé 15", "Unused 15", 0),
    //    SI_FURNITURETHEMETYPE27("Unused 16", "Unused 16", "Inutilisé 16", "Unused 16", 0),
    //    SI_FURNITURETHEMETYPE28("Unused 17", "Unused 17", "Inutilisé 17", "Unused 17", 0),
    //    SI_FURNITURETHEMETYPE29("Unused 18", "Unused 18", "Inutilisé 18", "Unused 18", 0),
    SI_FURNITURETHEMETYPE3("High Elf", "Hochelfen", "Haut-elfe", "Альтмерский", 1),
    //    SI_FURNITURETHEMETYPE30("Unused 19", "Unused 19", "Inutilisé 19", "Unused 19", 0),
    //    SI_FURNITURETHEMETYPE31("Unused 20", "Unused 20", "Inutilisé 20", "Unused 20", 0),
    SI_FURNITURETHEMETYPE4("Argonian", "Argonier", "Argonien", "Аргонианский", 0),
    SI_FURNITURETHEMETYPE5("Wood Elf", "Waldelfen", "Elfe des bois", "Босмерский", 1),
    SI_FURNITURETHEMETYPE6("Dark Elf", "Dunkelelfen", "Elfe noir", "Данмерский", 1),
    SI_FURNITURETHEMETYPE7("Khajiit", "Khajiit", "Khajiit", "Каджитский", 0),
    SI_FURNITURETHEMETYPE8("Nord", "Nord", "Nordique", "Нордский", 0),
    SI_FURNITURETHEMETYPE9("Orc", "Orks", "Orque", "Орочий", 0);

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
