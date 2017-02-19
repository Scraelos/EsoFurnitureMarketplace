/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.model;

import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.scraelos.esofurnituremp.model.lib.DAO;

/**
 *
 * @author scraelos
 */
@Entity
public class Recipe extends DAO {

    @OneToOne(mappedBy = "recipe")
    private FurnitureItem furnitureItem;

    @Id
    @Basic(optional = false)
    private Long id;
    private String nameEn;
    private String nameFr;
    private String nameDe;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private RECIPE_TYPE recipeType;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private ITEM_QUALITY itemQuality;
    @OneToMany(mappedBy = "recipe")
    private Set<RecipeIngredient> recipeIngredients;

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

    public RECIPE_TYPE getRecipeType() {
        return recipeType;
    }

    public void setRecipeType(RECIPE_TYPE recipeType) {
        this.recipeType = recipeType;
    }

    public ITEM_QUALITY getItemQuality() {
        return itemQuality;
    }

    public void setItemQuality(ITEM_QUALITY itemQuality) {
        this.itemQuality = itemQuality;
    }

    public Set<RecipeIngredient> getRecipeIngredients() {
        return recipeIngredients;
    }

    public void setRecipeIngredients(Set<RecipeIngredient> recipeIngredients) {
        this.recipeIngredients = recipeIngredients;
    }

    public FurnitureItem getFurnitureItem() {
        return furnitureItem;
    }

    public void setFurnitureItem(FurnitureItem furnitureItem) {
        this.furnitureItem = furnitureItem;
    }

    public String getNameFr() {
        return nameFr;
    }

    public void setNameFr(String nameFr) {
        this.nameFr = nameFr;
    }

    public String getNameDe() {
        return nameDe;
    }

    public void setNameDe(String nameDe) {
        this.nameDe = nameDe;
    }

}
