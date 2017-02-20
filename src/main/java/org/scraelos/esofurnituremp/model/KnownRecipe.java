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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import org.scraelos.esofurnituremp.model.lib.DAO;

/**
 *
 * @author scraelos
 */
@Entity
public class KnownRecipe extends DAO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    private Long id;
    @ManyToOne
    private SysAccount account;
    @ManyToOne
    private Recipe recipe;
    private String characterName;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private ESO_SERVER esoServer;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public SysAccount getAccount() {
        return account;
    }

    public void setAccount(SysAccount account) {
        this.account = account;
    }

    public ESO_SERVER getEsoServer() {
        return esoServer;
    }

    public void setEsoServer(ESO_SERVER esoServer) {
        this.esoServer = esoServer;
    }

}
