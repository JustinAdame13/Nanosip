package mx.nanosip.nanosip.Controllers.Backend;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.*;
import java.util.List;

public class ClientesAPI {

    private static final String BASE = "http://localhost:8080/api/clientes";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    public List<Clientes> obtenerTodos() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(res.body(), new TypeToken<List<Clientes>>(){}.getType());
    }

    public void guardar(Clientes c) throws Exception {
        String json = gson.toJson(c);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    public void actualizar(Clientes c) throws Exception {
        String json = gson.toJson(c);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/" + c.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    public void eliminar(int id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/" + id))
                .DELETE()
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}
