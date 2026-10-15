package com.app.controller;

import com.app.model.Articulo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ArticuloController {

    private static final String API_URL = "http://localhost:3000/api/articulos";
    private final HttpClient httpClient;

    public ArticuloController() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    // Petición GET al endpoint de API
    public List<Articulo> obtenerArticulosDeApi() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error en la API. Código HTTP: " + response.statusCode());
        }

        return parsearJsonAArticulos(response.body());
    }

    // conversión de la respuesta usando org.json
    public static List<Articulo> parsearJsonAArticulos(String jsonTexto) {
        List<Articulo> lista = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonTexto);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);

            int id = obj.getInt("id");
            String nombre = obj.getString("nombre");
            String categoria = obj.getString("categoria");
            double precio = obj.getDouble("precio");
            int stock = obj.getInt("stock");

            lista.add(new Articulo(id, nombre, categoria, precio, stock));
        }

        return lista;
    }
}