/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.model;

import java.util.List;
import java.util.Locale;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.scraelos.esofurnituremp.model.lib.DAO;

/**
 *
 * @author scraelos
 */
@Entity
@Table(indexes = {
    @Index(columnList = "recipe_id", unique = true)
})
public class FurnitureItem extends DAO {

    @Id
    @Basic(optional = false)
    private Long id;
    private String nameEn;
    private String nameDe;
    private String nameFr;
    private String nameRu;
    @ManyToOne
    private FurnitureCategory category;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private ITEM_QUALITY itemQuality;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private FURNITURE_THEME theme;
    @OneToOne
    private Recipe recipe;
    @OneToMany(mappedBy = "furnitureItem", cascade = CascadeType.ALL)
    private List<ItemScreenshot> itemScreenshots;
    private String itemLink;
    private String icon;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public FurnitureCategory getCategory() {
        return category;
    }

    public void setCategory(FurnitureCategory category) {
        this.category = category;
    }

    public ITEM_QUALITY getItemQuality() {
        return itemQuality;
    }

    public void setItemQuality(ITEM_QUALITY itemQuality) {
        this.itemQuality = itemQuality;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public String getNameDe() {
        return nameDe;
    }

    public void setNameDe(String nameDe) {
        this.nameDe = nameDe;
    }

    public String getNameFr() {
        return nameFr;
    }

    public void setNameFr(String nameFr) {
        this.nameFr = nameFr;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public List<ItemScreenshot> getItemScreenshots() {
        return itemScreenshots;
    }

    public void setItemScreenshots(List<ItemScreenshot> itemScreenshots) {
        this.itemScreenshots = itemScreenshots;
    }

    public String getItemLink() {
        return itemLink;
    }

    public void setItemLink(String itemLink) {
        this.itemLink = itemLink;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public FURNITURE_THEME getTheme() {
        return theme;
    }

    public void setTheme(FURNITURE_THEME theme) {
        this.theme = theme;
    }

    public String getLocalizedName(Locale l) {
        String lang = l.getLanguage();
        switch (lang) {
            case "en":
                return nameEn;
            case "de":
                return nameDe;
            case "fr":
                return nameFr;
            case "ru":
                return nameRu;
        }
        return nameEn;
    }

}
