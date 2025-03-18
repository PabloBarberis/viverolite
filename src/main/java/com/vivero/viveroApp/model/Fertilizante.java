
package com.vivero.viveroApp.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("Fertilizante")
@Getter
@Setter
@NoArgsConstructor
public class Fertilizante extends Producto{

}
