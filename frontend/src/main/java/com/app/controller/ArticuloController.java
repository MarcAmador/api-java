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
                .connectTimeout(Duration.ofSeconds(6))
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

    private HttpRequest.Builder prepararPeticion(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("User-Agent", "GestorArticulos-Desktop/1.0")
                .header("ngrok-skip-browser-warning", "true")
                .header("Bypass-Tunnel-Reminder", "true")
                .timeout(Duration.ofSeconds(8));
    }

    // si falla el tunel probamos con localhost a ver si conecta
    private HttpResponse<String> ejecutarConRespaldo(HttpRequest reqOriginal, String urlObjetivo) throws Exception {
        try {
            HttpResponse<String> resp = httpClient.send(reqOriginal, HttpResponse.BodyHandlers.ofString());
            // si dio error 400 o mas intentamos localhost
            if (resp.statusCode() >= 400 && urlObjetivo.contains("loca.lt")) {
                try {
                    String urlLocal = urlObjetivo.replace("https://gestor-articulos.loca.lt", "http://localhost:3000");
                    HttpRequest reqLocal = HttpRequest.newBuilder(reqOriginal, (k, v) -> true)
                            .uri(URI.create(urlLocal))
                            .build();
                    HttpResponse<String> respLocal = httpClient.send(reqLocal, HttpResponse.BodyHandlers.ofString());
                    if (respLocal.statusCode() < 400) {
                        return respLocal;
                    }
                } catch (Exception ignored) {}
            }
            return resp;
        } catch (Exception ex) {
            // por si dio timeout o error de conexion
            if (urlObjetivo.contains("loca.lt")) {
                try {
                    String urlLocal = urlObjetivo.replace("https://gestor-articulos.loca.lt", "http://localhost:3000");
                    HttpRequest reqLocal = HttpRequest.newBuilder(reqOriginal, (k, v) -> true)
                            .uri(URI.create(urlLocal))
                            .build();
                    return httpClient.send(reqLocal, HttpResponse.BodyHandlers.ofString());
                } catch (Exception ignored) {}
            }
            throw ex;
        }
    }

    public static List<Articulo> parsearJsonAArticulos(String json) {
        JSONArray arrayJson = new JSONArray(json);
        List<Articulo> lista = new ArrayList<>();

        for (int i = 0; i < arrayJson.length(); i++) {
            JSONObject obj = arrayJson.getJSONObject(i);
            int id = obj.getInt("id");
            String nombre = obj.getString("nombre");
            String categoria = obj.getString("categoria");
            double precio = obj.getDouble("precio");
            int stock = obj.getInt("stock");

            lista.add(new Articulo(id, nombre, categoria, precio, stock));
        }

        return lista;
    }

    // peticion get a la api
    public List<Articulo> obtenerArticulosDeApi() throws Exception {
        HttpRequest request = prepararPeticion(apiUrl).GET().build();
        HttpResponse<String> response = ejecutarConRespaldo(request, apiUrl);

        if (response.statusCode() != 200) {
            throw new Exception("HTTP " + response.statusCode() + ": " + response.body());
        }

        return parsearJsonAArticulos(response.body());
    }

    // post para registrar producto nuevo
    public Articulo crearArticulo(Articulo a) throws Exception {
        JSONObject body = new JSONObject();
        body.put("nombre", a.getNombre());
        body.put("categoria", a.getCategoria());
        body.put("precio", a.getPrecio());
        body.put("stock", a.getStock());

        HttpRequest request = prepararPeticion(apiUrl)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = ejecutarConRespaldo(request, apiUrl);

        if (response.statusCode() != 201) {
            JSONObject err = new JSONObject(response.body());
            throw new Exception(err.optString("error", "Error al crear (HTTP " + response.statusCode() + ")"));
        }

        JSONObject creado = new JSONObject(response.body());
        return new Articulo(
                creado.getInt("id"),
                creado.getString("nombre"),
                creado.getString("categoria"),
                creado.getDouble("precio"),
                creado.getInt("stock")
        );
    }

    // put para actualizar los datos
    public void actualizarArticulo(Articulo a) throws Exception {
        JSONObject body = new JSONObject();
        body.put("nombre", a.getNombre());
        body.put("categoria", a.getCategoria());
        body.put("precio", a.getPrecio());
        body.put("stock", a.getStock());

        String urlConId = apiUrl + "/" + a.getId();
        HttpRequest request = prepararPeticion(urlConId)
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = ejecutarConRespaldo(request, urlConId);

        if (response.statusCode() != 200) {
            JSONObject err = new JSONObject(response.body());
            throw new Exception(err.optString("error", "Error al actualizar (HTTP " + response.statusCode() + ")"));
        }
    }

    // delete para borrar el articulo
    public void eliminarArticulo(int id) throws Exception {
        String urlConId = apiUrl + "/" + id;
        HttpRequest request = prepararPeticion(urlConId)
                .DELETE()
                .build();

        HttpResponse<String> response = ejecutarConRespaldo(request, urlConId);

        if (response.statusCode() != 200) {
            JSONObject err = new JSONObject(response.body());
            throw new Exception(err.optString("error", "Error al eliminar (HTTP " + response.statusCode() + ")"));
        }
    }
}
