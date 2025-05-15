
package com.vivero.viveroApp.Repository;

import com.vivero.viveroApp.model.ProductoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoCompraRepository extends JpaRepository<ProductoCompra, Long>{

}
