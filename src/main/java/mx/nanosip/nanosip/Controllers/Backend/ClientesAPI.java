package mx.nanosip.nanosip.Controllers.Backend;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.*;
import java.util.List;

public class ClientesAPI {

    private static final String BASE = "https://nanozip-api-paulo-bjgwaah5hgf9etc0.mexicocentral-01.azurewebsites.net/api/clientes";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    // ── GET todos ────────────────────────────────────────────
    public List<Clientes> obtenerTodos() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(res.body(), new TypeToken<List<Clientes>>(){}.getType());
    }

    // ── POST crear ───────────────────────────────────────────
    public void guardar(Clientes cli) throws Exception {
        String json = gson.toJson(cli);
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
    }

    // ── PUT editar ───────────────────────────────────────────
    public void actualizar(Clientes cli) throws Exception {
        String json = gson.toJson(cli);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/" + cli.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    // ── DELETE eliminar ──────────────────────────────────────
    public void eliminar(int id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/" + id))
                .DELETE()
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}