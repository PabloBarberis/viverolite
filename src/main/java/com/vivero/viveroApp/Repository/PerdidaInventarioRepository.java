
package com.vivero.viveroApp.Repository;

import com.vivero.viveroApp.model.PerdidaInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerdidaInventarioRepository extends JpaRepository<PerdidaInventario, Long>{

}
