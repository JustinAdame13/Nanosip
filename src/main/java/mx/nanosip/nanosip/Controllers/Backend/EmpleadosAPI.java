package mx.nanosip.nanosip.Controllers.Backend;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import mx.nanosip.nanosip.Controllers.ConexionApi;

import java.net.URI;
import java.net.http.*;
import java.util.List;

public class EmpleadosAPI {

    private static final String BASE = "https://nanozip-api-paulo-bjgwaah5hgf9etc0.mexicocentral-01.azurewebsites.net/api/empleados";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    // ── GET todos ────────────────────────────────────────────
    public List<Empleados> obtenerTodos() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(res.body(), new TypeToken<List<Empleados>>(){}.getType());
    }

    // ── POST crear ───────────────────────────────────────────
    public void guardar(Empleados emp) throws Exception {
        String json = gson.toJson(emp);
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
    public void actualizar(Empleados emp) throws Exception {
        String json = gson.toJson(emp);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/" + emp.getId()))
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

    public Empleados login(String usuario, String contrasena) throws Exception {
        Empleados credenciales = new Empleados();
        credenciales.setNombre(usuario); // Enviamos el nombre
        credenciales.setContrasena(contrasena);

        String json = gson.toJson(credenciales);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/login"))
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), Empleados.class);
        } else if (response.statusCode() == 401 || response.statusCode() == 404) {
            throw new Exception("Usuario o contraseña incorrectos.");
        } else {
            throw new Exception("Error del servidor: " + response.statusCode());
        }
    }
}