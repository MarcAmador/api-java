package com.app;

import com.app.controller.ArticuloController;
import com.app.model.Articulo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArticuloControllerTest {

    // Prueba de conversión de datos de json a objeto para la tabla
    @Test
    void debeParsearJsonCorrectamente() {
        String jsonPrueba = "[" +
                "{\"id\": 1, \"nombre\": \"Teclado Mecánico RGB\", \"categoria\": \"Periféricos\", \"precio\": 79.99, \"stock\": 15}," +
                "{\"id\": 2, \"nombre\": \"Monitor 27 IPS\", \"categoria\": \"Pantallas\", \"precio\": 229.00, \"stock\": 8}" +
                "]";

        List<Articulo> resultado = ArticuloController.parsearJsonAArticulos(jsonPrueba);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        Articulo primero = resultado.get(0);
        assertEquals(1, primero.getId());
        assertEquals("Teclado Mecánico RGB", primero.getNombre());
        assertEquals(79.99, primero.getPrecio());
    }

    // Prueba cuando no vienen datos
    @Test
    void debeRetornarListaVaciaSiJsonEstaVacio() {
        String jsonVacio = "[]";

        List<Articulo> resultado = ArticuloController.parsearJsonAArticulos(jsonVacio);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}