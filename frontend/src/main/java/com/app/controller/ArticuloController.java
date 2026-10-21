package com.app.controller;

import com.app.model.Articulo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ArticuloController {

    private static final String DEFAULT_API_URL = "https://gestor-articulos.loca.lt/api/articulos";
    private final String apiUrl;
    private final HttpClient httpClient;

    // lee config.properties si existe, o usa el túnel
    public ArticuloController() {
        this(cargarUrlConfigurada());
    }

    public ArticuloController(String apiUrl) {
        this.apiUrl = (apiUrl != null && !apiUrl.trim().isEmpty()) ? apiUrl.trim() : DEFAULT_API_URL;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    // Busca config.properties generado por el instalador
    private static String cargarUrlConfigurada() {
        File archivo = new File("config.properties");
        if (archivo.exists()) {
            try (InputStream in = new FileInputStream(archivo)) {
                Properties props = new Properties();
                props.load(in);
                String url = props.getProperty("api.url");
                if (url != null && !url.trim().isEmpty()) {
                    return url.trim();
                }
            } catch (Exception e) {
                System.err.println("No se pudo leer config.properties: " + e.getMessage());
            }
        }
        return DEFAULT_API_URL;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    // Petición GET a la API
    public List<Articulo> obtenerArticulosDeApi() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", "GestorArticulos-Desktop/1.0")
                .header("ngrok-skip-browser-warning", "true")
                .header("Bypass-Tunnel-Reminder", "true")
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
