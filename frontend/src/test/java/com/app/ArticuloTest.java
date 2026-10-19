package com.app;

import com.app.model.Articulo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArticuloTest {

    // Prueba del constructor con parametros y los getters
    @Test
    void debeCrearArticuloConConstructorCompleto() {
        Articulo art = new Articulo(10, "Mouse Gamer", "Perifericos", 35.50, 20);

        assertEquals(10, art.getId());
        assertEquals("Mouse Gamer", art.getNombre());
        assertEquals("Perifericos", art.getCategoria());
        assertEquals(35.50, art.getPrecio());
        assertEquals(20, art.getStock());
    }

    // Prueba de getters y setters y el  constructor vacio
    @Test
    void debeModificarPropiedadesConSetters() {
        Articulo art = new Articulo();
        art.setId(5);
        art.setNombre("Monitor 24");
        art.setPrecio(150.00);

        assertEquals(5, art.getId());
        assertEquals("Monitor 24", art.getNombre());
        assertEquals(150.00, art.getPrecio());
    }
}