package mx.nanosip.nanosip.Controllers.Backend;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.*;
import java.util.List;

public class VentasProductosAPI {

    private static final String BASE = "http://localhost:8080/api/ventas-productos";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    // ── Guardar lista ─────────────────────────
    public void guardarLista(List<VentasProductos> lista) throws Exception {
        String json = gson.toJson(lista);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/lote"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    // ── Obtener por venta ─────────────────────
    public List<VentasProductos> obtenerPorVenta(int numeroVenta) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/venta/" + numeroVenta))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        return gson.fromJson(res.body(),
                new TypeToken<List<VentasProductos>>(){}.getType());
    }

    // ── Eliminar por venta ────────────────────
    public void eliminarPorVenta(int numeroVenta) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/venta/" + numeroVenta))
                .DELETE()
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}