/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.data;

import org.scraelos.esofurnituremp.model.KnownRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 *
 * @author scraelos
 */
public interface KnownRecipeRepository extends JpaRepository<KnownRecipe, Long>, JpaSpecificationExecutor<KnownRecipe> {

}
