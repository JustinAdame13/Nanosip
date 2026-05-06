package mx.nanosip.nanosip.Controllers.Backend;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.net.URI;
import java.net.http.*;
import java.util.List;

public class ProductoProveedorAPI {

    private static final String BASE = "https://nanozip-api-paulo-bjgwaah5hgf9etc0.mexicocentral-01.azurewebsites.net/api/productos-proveedores";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson         = new Gson();

    public List<ProductosProveedores> obtenerTodas() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE))
                .GET()
                .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(res.body(),
                new TypeToken<List<ProductosProveedores>>(){}.getType());
    }
}