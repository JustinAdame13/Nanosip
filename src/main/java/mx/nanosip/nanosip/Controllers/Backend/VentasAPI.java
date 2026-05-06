package mx.nanosip.nanosip.Controllers.Backend;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.*;
import java.time.LocalDateTime;
import java.util.List;

public class VentasAPI {

    private static final String BASE = "https://nanozip-api-paulo-bjgwaah5hgf9etc0.mexicocentral-01.azurewebsites.net/api/ventas";
    private static final HttpClient client = HttpClient.newHttpClient();

    private static final Gson gson = new GsonBuilder()
            // ✅ DESERIALIZADOR CORREGIDO
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, ctx) -> {
                        if (json.isJsonArray()) {
                            JsonArray a = json.getAsJsonArray();
                            return LocalDateTime.of(
                                    a.get(0).getAsInt(),
                                    a.get(1).getAsInt(),
                                    a.get(2).getAsInt(),
                                    a.get(3).getAsInt(),
                                    a.get(4).getAsInt()
                            );
                        }
                        return LocalDateTime.parse(json.getAsString());
                    })
            // ✅ SERIALIZADOR (se deja igual)
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) ->
                            new JsonPrimitive(src.toString()))
            .create();

    // ── GET todos ────────────────────────────────────────────
    public List<Ventas> obtenerTodos() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(res.body(), new TypeToken<List<Ventas>>(){}.getType());
    }

    // ── POST crear ───────────────────────────────────────────
    public Ventas guardar(Ventas venta) throws Exception {
        String json = gson.toJson(venta);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200 && res.statusCode() != 201) {
            throw new RuntimeException("Error al guardar: " + res.body());
        }

        return gson.fromJson(res.body(), Ventas.class);
    }

    // ── PUT editar ───────────────────────────────────────────
    public void actualizar(Ventas venta) throws Exception {
        String json = gson.toJson(venta);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/" + venta.getNumero()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    // ── DELETE eliminar ──────────────────────────────────────
    public void eliminar(int numero) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/" + numero))
                .DELETE()
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    // ── Guardar detalle ──────────────────────────────────────
    public void guardarDetalle(VentasProductos detalle) throws Exception {
        String json = gson.toJson(detalle);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://nanozip-api-paulo-bjgwaah5hgf9etc0.mexicocentral-01.azurewebsites.net/api/ventas-productos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    // ── Eliminar detalles ────────────────────────────────────
    public void eliminarDetalles(int numeroVenta) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://nanozip-api-paulo-bjgwaah5hgf9etc0.mexicocentral-01.azurewebsites.net/api/ventas-productos/venta/" + numeroVenta))
                .DELETE()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200 && res.statusCode() != 204) {
            throw new RuntimeException("Error al limpiar productos anteriores: " + res.body());
        }
    }

    // ── Obtener detalles ─────────────────────────────────────
    public List<VentasProductos> obtenerDetalles(int numeroVenta) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://nanozip-api-paulo-bjgwaah5hgf9etc0.mexicocentral-01.azurewebsites.net/api/ventas-productos/venta/" + numeroVenta))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        return gson.fromJson(res.body(),
                new TypeToken<List<VentasProductos>>(){}.getType());
    }
}