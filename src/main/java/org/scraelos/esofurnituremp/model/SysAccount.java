/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.model;

import java.util.Collection;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import org.scraelos.esofurnituremp.model.lib.DAO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author scraelos
 */
@Entity
public class SysAccount extends DAO implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    private Long id;
    @Column(unique = true)
    private String username;
    private String password;
    @Column(unique = true)
    private String esoId;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private ESO_SERVER esoServer;
    private Boolean enabled;
    @ManyToMany
    private Set<SysAccountRole> roles;
    @OneToMany(mappedBy = "account")
    private Set<KnownRecipe> knownRecipes;
    @Transient
    private Set<Recipe> knownRecipesRaw;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private USER_LANGUAGE userLanguage;

    private Boolean useEnItemNames;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String getEsoId() {
        return esoId;
    }

    public void setEsoId(String esoId) {
        this.esoId = esoId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Set<SysAccountRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<SysAccountRole> roles) {
        this.roles = roles;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<KnownRecipe> getKnownRecipes() {
        return knownRecipes;
    }

    public void setKnownRecipes(Set<KnownRecipe> knownRecipes) {
        this.knownRecipes = knownRecipes;
    }

    public Set<Recipe> getKnownRecipesRaw() {
        return knownRecipesRaw;
    }

    public void setKnownRecipesRaw(Set<Recipe> knownRecipesRaw) {
        this.knownRecipesRaw = knownRecipesRaw;
    }

    public ESO_SERVER getEsoServer() {
        return esoServer;
    }

    public void setEsoServer(ESO_SERVER esoServer) {
        this.esoServer = esoServer;
    }

    public USER_LANGUAGE getUserLanguage() {
        return userLanguage;
    }

    public void setUserLanguage(USER_LANGUAGE userLanguage) {
        this.userLanguage = userLanguage;
    }

    public Boolean getUseEnItemNames() {
        return useEnItemNames;
    }

    public void setUseEnItemNames(Boolean useEnItemNames) {
        this.useEnItemNames = useEnItemNames;
    }

}
