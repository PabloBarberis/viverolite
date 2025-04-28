package com.vivero.viveroApp.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("Grow")
@Getter
@Setter
@NoArgsConstructor

public class Grow extends Producto {

}