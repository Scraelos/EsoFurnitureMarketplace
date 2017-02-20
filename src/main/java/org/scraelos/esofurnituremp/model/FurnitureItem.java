/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import org.scraelos.esofurnituremp.model.lib.DAO;

/**
 *
 * @author scraelos
 */
@Entity
public class FurnitureItem extends DAO {

    @Id
    @Basic(optional = false)
    private Long id;
    private String nameEn;
    @ManyToOne
    private ItemSubCategory subCategory;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private ITEM_QUALITY itemQuality;
    @OneToOne
    private Recipe recipe;

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

    public ItemSubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(ItemSubCategory subCategory) {
        this.subCategory = subCategory;
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

}