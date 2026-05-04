package mx.nanosip.nanosip.Controllers.Backend;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.util.stream.Collectors;
import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;

public class ProductosAPI {

    private static final String BASE = "http://localhost:8080/api/productos";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    // ── GET todos ────────────────────────────────────────────

    // ── POST crear ───────────────────────────────────────────
    public Productos guardar(Productos prod) throws Exception {
        String json = gson.toJson(prod);
        System.out.println("JSON enviado: " + json);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status: " + res.statusCode());
        System.out.println("Response: " + res.body());

        if (res.statusCode() != 200 && res.statusCode() != 201) {
            throw new RuntimeException("Error al guardar: " + res.body());
        }

        // Parseamos la respuesta para obtener la clave generada por la BD
        return gson.fromJson(res.body(), Productos.class);
    }

    // ── PUT editar ───────────────────────────────────────────
    public void actualizar(Productos prod) throws Exception {
        String json = gson.toJson(prod);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/" + prod.getClave()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    // ── DELETE eliminar ──────────────────────────────────────
    public void eliminar(int clave) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/" + clave))
                .DELETE()
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    public static List<Productos> obtenerTodos() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/productos"))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new RuntimeException("Error al obtener catálogo de productos: " + res.body());
        }

        return gson.fromJson(res.body(), new TypeToken<List<Productos>>(){}.getType());
    }

    public void guardarProveedorProducto(int claveProducto, int idProveedor) throws Exception {
        String json = String.format("{\"claveProducto\":%d, \"idProveedor\":%d}", claveProducto, idProveedor);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/productos-proveedores"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    public void eliminarProveedoresDeProducto(int claveProducto) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/productos-proveedores/" + claveProducto))
                .DELETE()
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    public List<Integer> obtenerIdsProveedoresPorProducto(int claveProducto) throws Exception {
        // CAMBIO: Quitar el "?claveProducto=" y usar "/" para que coincida con @PathVariable
        String url = "http://localhost:8080/api/productos-proveedores/producto/" + claveProducto;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            System.err.println("Error en la API: " + res.statusCode());
            return new ArrayList<>();
        }

        List <ProductosProveedores> relaciones = gson.fromJson(
                res.body(),
                new TypeToken<List<ProductosProveedores>>() {}.getType()
        );

        return relaciones.stream()
                .map(ProductosProveedores::getIdProveedor)
                .toList();
    }
}