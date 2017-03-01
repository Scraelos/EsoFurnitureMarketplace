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
public enum RECIPE_TYPE {

    Diagram("Diagram", "Skizze", "Diagramme", "диаграмма"),
    Design("Design", "Entwurf", "Croquis", "проект"),
    Pattern("Pattern", "Vorlage", "Préparation", "шаблон"),
    Blueprint("Blueprint", "Blaupause", "Plan", "чертеж"),
    Praxis("Praxis", "Anleitung", "Praxis", "схема"),
    Formula("Formula", "Formel", "Formule", "формула");

    private RECIPE_TYPE(String nameEn_, String nameDe_, String nameFr_, String nameRu_) {
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
}
