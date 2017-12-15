/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.data;

import java.util.List;
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 *
 * @author scraelos
 */
public interface FurnitureItemRepository extends JpaRepository<FurnitureItem, Long>, JpaSpecificationExecutor<FurnitureItem> {

    List<FurnitureItem> findAllBy(Pageable pageable);
    
}
