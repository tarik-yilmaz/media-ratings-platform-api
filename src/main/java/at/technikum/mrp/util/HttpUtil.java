package at.technikum.mrp.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HttpUtil {

    public static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] data = JsonUtil.MAPPER.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, data.length);
        exchange.getResponseBody().write(data);
        exchange.close();
    }

    public static void sendEmpty(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, -1);
        exchange.close();
    }
}
