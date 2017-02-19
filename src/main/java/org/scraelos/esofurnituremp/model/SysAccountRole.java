/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.model;

import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import org.scraelos.esofurnituremp.model.lib.DAO;
import org.springframework.security.core.GrantedAuthority;

/**
 *
 * @author scraelos
 */
@Entity
public class SysAccountRole extends DAO implements GrantedAuthority {

    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    private String nic;
    private String name;
    @ManyToMany(mappedBy = "roles")
    private List<SysAccount> sysAccounts;

    public SysAccountRole() {
    }

    public SysAccountRole(Long id, String nic, String name) {
        this.id = id;
        this.nic = nic;
        this.name = name;
    }

    public SysAccountRole(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getAuthority() {
        return nic;
    }

    public List<SysAccount> getSysAccounts() {
        return sysAccounts;
    }

    public void setSysAccounts(List<SysAccount> sysAccounts) {
        this.sysAccounts = sysAccounts;
    }

}
